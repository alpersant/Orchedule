package com.orchedule.identity.application;

import com.orchedule.identity.api.AuthResponse;
import com.orchedule.identity.api.UserAuthQuery;
import com.orchedule.identity.api.UserAuthView;
import com.orchedule.identity.api.dto.RegisterRequest;
import com.orchedule.identity.application.exception.AlreadyExistsException;
import com.orchedule.identity.domain.PasswordHasher;
import com.orchedule.identity.domain.RefreshToken;
import com.orchedule.identity.domain.RefreshTokenHasher;
import com.orchedule.identity.domain.RefreshTokenRepository;
import com.orchedule.identity.domain.RegisterUserCommand;
import com.orchedule.identity.domain.Role;
import com.orchedule.identity.domain.TokenService;
import com.orchedule.identity.domain.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.UUID;

@Service
public class AuthService {

    public static final String INVALID_CREDENTIALS = "Invalid credentials";

    private final UserAuthQuery userAuthQuery;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordHasher passwordHasher;
    private final RefreshTokenHasher refreshTokenHasher;
    private final TokenService tokenService;
    private final UserRepository userRepository;

    public AuthService(
            UserAuthQuery userAuthQuery,
            RefreshTokenRepository refreshTokenRepository,
            PasswordHasher passwordHasher,
            RefreshTokenHasher refreshTokenHasher,
            TokenService tokenService,
            UserRepository userRepository
    ) {
        this.userAuthQuery = userAuthQuery;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordHasher = passwordHasher;
        this.refreshTokenHasher = refreshTokenHasher;
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
                passwordHasher.hash(request.password()),
                Role.USER,
                true,
                false
        ));

        UserAuthView user = userAuthQuery.findActiveById(userId)
                .orElseThrow(() -> new IllegalStateException("User created but not found"));

        return issueTokens(user);
    }

    @Transactional
    public AuthResponse login(String email, String rawPassword) {
        UserAuthView user = userAuthQuery.findByEmail(normalizeEmail(email))
                .filter(UserAuthView::active)
                .orElseThrow(() -> new IllegalArgumentException(INVALID_CREDENTIALS));

        if (!passwordHasher.matches(rawPassword, user.passwordHash())) {
            throw new IllegalArgumentException(INVALID_CREDENTIALS);
        }

        return issueTokens(user);
    }

    @Transactional
    public AuthResponse refresh(String refreshToken) {
        if (!tokenService.isValid(refreshToken) || !tokenService.isRefreshToken(refreshToken)) {
            throw new IllegalArgumentException(INVALID_CREDENTIALS);
        }

        UUID userId = UUID.fromString(tokenService.getSubject(refreshToken));

        UserAuthView user = userAuthQuery.findActiveById(userId)
                .orElseThrow(() -> new IllegalArgumentException(INVALID_CREDENTIALS));

        String refreshTokenHash = refreshTokenHasher.hash(refreshToken);

        refreshTokenRepository.findByTokenHash(refreshTokenHash)
                .filter(RefreshToken::isValid)
                .orElseThrow(() -> new IllegalArgumentException(INVALID_CREDENTIALS));

        refreshTokenRepository.revokeAllByUserId(user.id());

        return issueTokens(user);
    }

    @Transactional
    public void logout(String refreshToken) {
        if (!tokenService.isValid(refreshToken) || !tokenService.isRefreshToken(refreshToken)) {
            return;
        }

        String hash = refreshTokenHasher.hash(refreshToken);
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
                refreshTokenHasher.hash(refreshToken),
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
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
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