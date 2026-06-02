package com.orchedule.identity.api;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String email,
        String fullName,
        String role
) {
}