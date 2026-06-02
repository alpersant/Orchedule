package com.orchedule.identity.application;

import java.util.UUID;

public record AuthenticatedUserData(
        UUID id,
        String email,
        String fullName,
        String role,
        String passwordHash,
        boolean active
) {
}