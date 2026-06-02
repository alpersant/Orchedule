package com.orchedule.identity.domain;

import com.orchedule.identity.application.AuthService.TokenUser;
import com.orchedule.identity.infrastructure.security.JwtTokenService;
import org.springframework.stereotype.Service;

@Service
public class TokenService {

    private final JwtTokenService jwtTokenService;

    public TokenService(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    public String generateAccessToken(TokenUser user) {
        return jwtTokenService.generateAccessToken(user);
    }

    public String generateRefreshToken(TokenUser user) {
        return jwtTokenService.generateRefreshToken(user);
    }

    public boolean isValid(String token) {
        return jwtTokenService.isValid(token);
    }

    public String getSubject(String token) {
        return jwtTokenService.getSubject(token);
    }
}