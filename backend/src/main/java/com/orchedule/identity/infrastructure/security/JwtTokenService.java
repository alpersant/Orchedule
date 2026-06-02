package com.orchedule.identity.infrastructure.security;

import com.orchedule.identity.application.AuthService.TokenUser;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenService {

    public String generateAccessToken(TokenUser user) {
        return "";
    }

    public String generateRefreshToken(TokenUser user) {
        return "";
    }

    public boolean isValid(String token) {
        return true;
    }

    public String getSubject(String token) {
        return "";
    }
}