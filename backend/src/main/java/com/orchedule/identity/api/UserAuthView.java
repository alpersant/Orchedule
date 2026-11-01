package com.orchedule.identity.api;

import java.security.Principal;
import java.util.UUID;

/**
 * Internal authentication query view. Constructor and record accessors remain
 * compatible with UserAuthQuery, AuthService and the existing JWT filter.
 * The stable principal name is the user id, never the record's string form.
 */
public record UserAuthView(
        UUID id,
        String email,
        String fullName,
        String role,
        String passwordHash,
        boolean active
) implements Principal {

    @Override
    public String getName() {
        if (id == null) {
            throw new IllegalStateException("An authenticated user must have an id");
        }
        return id.toString();
    }

    /** Never include credential hashes or personal profile fields in log output. */
    @Override
    public String toString() {
        return "UserAuthView[id=" + id + ", role=" + role + ", active=" + active + "]";
    }
}
