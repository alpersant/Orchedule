package com.orchedule.identity.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

public class User {

    private final UUID id;
    private final String email;
    private final String passwordHash;
    private final String fullName;
    private final Role role;
    private final boolean active;
    private final boolean emailVerified;
    private final OffsetDateTime createdAt;
    private final OffsetDateTime updatedAt;

    public User(
            UUID id,
            String email,
            String passwordHash,
            String fullName,
            Role role,
            boolean active,
            boolean emailVerified,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.role = role;
        this.active = active;
        this.emailVerified = emailVerified;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public User withPasswordHash(String newPasswordHash) {
        return new User(id, email, newPasswordHash, fullName, role, active, emailVerified, createdAt, updatedAt);
    }

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public Role getRole() {
        return role;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}