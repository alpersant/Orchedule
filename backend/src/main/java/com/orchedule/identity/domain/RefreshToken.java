package com.orchedule.identity.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

public class RefreshToken {

    private final UUID id;
    private final UUID userId;
    private final String tokenHash;
    private final OffsetDateTime expiresAt;
    private final boolean revoked;
    private final OffsetDateTime createdAt;
    private final OffsetDateTime updatedAt;

    public RefreshToken(
            UUID id,
            UUID userId,
            String tokenHash,
            OffsetDateTime expiresAt,
            boolean revoked,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {
        this.id = id;
        this.userId = userId;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.revoked = revoked;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public boolean isValid() {
        return !revoked && expiresAt.isAfter(OffsetDateTime.now());
    }

    public RefreshToken revoke() {
        return new RefreshToken(id, userId, tokenHash, expiresAt, true, createdAt, updatedAt);
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public OffsetDateTime getExpiresAt() {
        return expiresAt;
    }

    public boolean isRevoked() {
        return revoked;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}