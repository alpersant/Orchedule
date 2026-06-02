package com.orchedule.identity.infrastructure.persistence;

import com.orchedule.identity.domain.RefreshToken;

public final class RefreshTokenPersistenceMapper {

    private RefreshTokenPersistenceMapper() {
    }

    public static RefreshToken toDomain(RefreshTokenJpaEntity entity) {
        if (entity == null) return null;
        return new RefreshToken(
                entity.getId(),
                entity.getUserId(),
                entity.getTokenHash(),
                entity.getExpiresAt(),
                entity.isRevoked(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static RefreshTokenJpaEntity toEntity(RefreshToken token) {
        if (token == null) return null;

        RefreshTokenJpaEntity entity = new RefreshTokenJpaEntity();
        entity.setId(token.getId());
        entity.setUserId(token.getUserId());
        entity.setTokenHash(token.getTokenHash());
        entity.setExpiresAt(token.getExpiresAt());
        entity.setRevoked(token.isRevoked());
        entity.setCreatedAt(token.getCreatedAt());
        entity.setUpdatedAt(token.getUpdatedAt());
        return entity;
    }
}