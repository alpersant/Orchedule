package com.orchedule.identity.domain;

public record RegisterUserCommand(
        String email,
        String fullName,
        String passwordHash,
        Role role,
        boolean active,
        boolean emailVerified
) {}