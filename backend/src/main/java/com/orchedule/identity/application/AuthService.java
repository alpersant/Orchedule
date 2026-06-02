package com.orchedule.identity.application;

import com.orchedule.identity.api.AuthResponse;
import com.orchedule.identity.domain.RefreshToken;
import com.orchedule.identity.domain.RefreshTokenRepository;
import com.orchedule.identity.domain.TokenService;
import com.orchedule.identity.infrastructure.security.PasswordService;
import com.orchedule.identity.api.UserAuthQuery;
import com.orchedule.identity.api.UserAuthView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class AuthService {

    private final UserAuthQuery userAuthQuery;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordService passwordService;
    private final TokenService tokenService;

    public AuthService(
            UserAuthQuery userAuthQuery,
            RefreshTokenRepository refreshTokenRepository,
            PasswordService passwordService,
            TokenService tokenService
    ) {
        this.userAuthQuery = userAuthQuery;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordService = passwordService;
        this.tokenService = tokenService;
    }

    @Transactional(readOnly = true)
    public AuthResponse login(String email, String rawPassword) {
        UserAuthView user = userAuthQuery.findByEmail(email)
                .filter(UserAuthView::active)
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!passwordService.matches(rawPassword, user.passwordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        return issueTokens(user);
    }

    @Transactional
    public AuthResponse refresh(String refreshToken) {
        if (!tokenService.isValid(refreshToken)) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        UUID userId = UUID.fromString(tokenService.getSubject(refreshToken));

        UserAuthView user = userAuthQuery.findActiveById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        String refreshTokenHash = passwordService.hash(refreshToken);

        refreshTokenRepository.findByTokenHash(refreshTokenHash)
                .filter(RefreshToken::isValid)
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        refreshTokenRepository.revokeAllByUserId(user.id());

        return issueTokens(user);
    }

    @Transactional
    public void logout(String refreshToken) {
        if (!tokenService.isValid(refreshToken)) {
            return;
        }

        String hash = passwordService.hash(refreshToken);
        refreshTokenRepository.findByTokenHash(hash)
                .ifPresent(token -> refreshTokenRepository.revokeAllByUserId(token.getUserId()));
    }

    private AuthResponse issueTokens(UserAuthView user) {
        TokenUser tokenUser = new TokenUser(user.id(), user.email(), user.role());

        String accessToken = tokenService.generateAccessToken(tokenUser);
        String refreshToken = tokenService.generateRefreshToken(tokenUser);

        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime expiresAt = now.plusDays(30);

        refreshTokenRepository.save(new RefreshToken(
                null,
                user.id(),
                passwordService.hash(refreshToken),
                expiresAt,
                false,
                now,
                now
        ));

        return new AuthResponse(
                accessToken,
                refreshToken,
                user.email(),
                user.fullName(),
                user.role()
        );
    }

    public record TokenUser(UUID id, String email, String role) {
    }
}