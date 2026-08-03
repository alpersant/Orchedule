package com.orchedule.identity.domain;

import com.orchedule.identity.application.AuthService.TokenUser;

public interface TokenService {

    String generateAccessToken(TokenUser user);

    String generateRefreshToken(TokenUser user);

    boolean isValid(String token);

    String getSubject(String token);

    boolean isRefreshToken(String token);

    long getRefreshTokenExpirationSeconds();
}