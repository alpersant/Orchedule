package com.orchedule.identity.api;

import java.util.UUID;

public record UserAuthView(
        UUID id,
        String email,
        String fullName,
        String role,
        String passwordHash,
        boolean active
) {
}