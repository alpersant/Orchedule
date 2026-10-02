package com.orchedule.infrastructure.security;

import com.orchedule.identity.application.AuthService.TokenUser;
import com.orchedule.identity.domain.TokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtTokenService implements TokenService {

    private final JwtProperties properties;

    public JwtTokenService(JwtProperties properties) {
        this.properties = properties;
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(properties.secret().getBytes());
    }

    @Override
    public String generateAccessToken(TokenUser user) {
        return Jwts.builder()
                .subject(user.id().toString())
                .claim("email", user.email())
                .claim("role", user.role())
                .claim("type", "access")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + properties.accessTokenExpirationSeconds() * 1000))
                .signWith(getSigningKey())
                .compact();
    }

    @Override
    public String generateRefreshToken(TokenUser user) {
        return Jwts.builder()
                .subject(user.id().toString())
                .claim("type", "refresh")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + properties.refreshTokenExpirationSeconds() * 1000))
                .signWith(getSigningKey())
                .compact();
    }

    @Override
    public boolean isValid(String token) {
        try {
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean isRefreshToken(String token) {
        String type = extractClaim(token, claims -> claims.get("type", String.class));
        return "refresh".equals(type);
    }

    @Override
    public String getSubject(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    @Override
    public long getRefreshTokenExpirationSeconds() {
        return properties.refreshTokenExpirationSeconds();
    }

    private <T> T extractClaim(String token, java.util.function.Function<Claims, T> resolver) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return resolver.apply(claims);
    }
}