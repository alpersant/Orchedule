package com.orchedule.identity.application;

import com.orchedule.identity.api.AuthResponse;
import com.orchedule.identity.api.UserAuthQuery;
import com.orchedule.identity.api.UserAuthView;
import com.orchedule.identity.api.dto.RegisterRequest;
import com.orchedule.identity.application.exception.AlreadyExistsException;
import com.orchedule.identity.domain.RefreshToken;
import com.orchedule.identity.domain.RefreshTokenRepository;
import com.orchedule.identity.domain.RegisterUserCommand;
import com.orchedule.identity.domain.Role;
import com.orchedule.identity.domain.TokenService;
import com.orchedule.identity.domain.UserRepository;
import com.orchedule.identity.infrastructure.security.PasswordService;
import com.orchedule.identity.infrastructure.security.RefreshTokenHashService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.UUID;

@Service
public class AuthService {

    private final UserAuthQuery userAuthQuery;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordService passwordService;
    private final RefreshTokenHashService refreshTokenHashService;
    private final TokenService tokenService;
    private final UserRepository userRepository;

    public AuthService(
            UserAuthQuery userAuthQuery,
            RefreshTokenRepository refreshTokenRepository,
            PasswordService passwordService,
            RefreshTokenHashService refreshTokenHashService,
            TokenService tokenService,
            UserRepository userRepository
    ) {
        this.userAuthQuery = userAuthQuery;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordService = passwordService;
        this.refreshTokenHashService = refreshTokenHashService;
        this.tokenService = tokenService;
        this.userRepository = userRepository;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());

        if (userRepository.existsByEmail(email)) {
            throw new AlreadyExistsException("Email already registered");
        }

        UUID userId = userRepository.register(new RegisterUserCommand(
                email,
                request.fullName().trim(),
                passwordService.hash(request.password()),
                Role.USER,
                true,
                false
        ));

        UserAuthView user = userAuthQuery.findActiveById(userId)
                .orElseThrow(() -> new IllegalStateException("User created but not found"));

        return issueTokens(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(String email, String rawPassword) {
        UserAuthView user = userAuthQuery.findByEmail(normalizeEmail(email))
                .filter(UserAuthView::active)
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!passwordService.matches(rawPassword, user.passwordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        return issueTokens(user);
    }

    @Transactional
    public AuthResponse refresh(String refreshToken) {
        if (!tokenService.isValid(refreshToken) || !tokenService.isRefreshToken(refreshToken)) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        UUID userId = UUID.fromString(tokenService.getSubject(refreshToken));

        UserAuthView user = userAuthQuery.findActiveById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        String refreshTokenHash = refreshTokenHashService.hash(refreshToken);

        refreshTokenRepository.findByTokenHash(refreshTokenHash)
                .filter(RefreshToken::isValid)
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        refreshTokenRepository.revokeAllByUserId(user.id());

        return issueTokens(user);
    }

    @Transactional
    public void logout(String refreshToken) {
        if (!tokenService.isValid(refreshToken) || !tokenService.isRefreshToken(refreshToken)) {
            return;
        }

        String hash = refreshTokenHashService.hash(refreshToken);
        refreshTokenRepository.findByTokenHash(hash)
                .ifPresent(token -> refreshTokenRepository.revokeAllByUserId(token.getUserId()));
    }

    private AuthResponse issueTokens(UserAuthView user) {
        Role role = toRole(user.role());
        TokenUser tokenUser = new TokenUser(user.id(), user.email(), role);

        String accessToken = tokenService.generateAccessToken(tokenUser);
        String refreshToken = tokenService.generateRefreshToken(tokenUser);

        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime expiresAt = now.plusSeconds(tokenService.getRefreshTokenExpirationSeconds());

        refreshTokenRepository.save(new RefreshToken(
                null,
                user.id(),
                refreshTokenHashService.hash(refreshToken),
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
                role.name()
        );
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    private Role toRole(String role) {
        if (role == null || role.isBlank()) {
            throw new IllegalStateException("User role is missing");
        }

        try {
            return Role.valueOf(role.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("Unsupported user role: " + role, ex);
        }
    }

    public record TokenUser(UUID id, String email, Role role) {}
}