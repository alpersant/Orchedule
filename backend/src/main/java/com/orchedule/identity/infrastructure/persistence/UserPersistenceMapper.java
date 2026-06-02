package com.orchedule.identity.infrastructure.persistence;

import com.orchedule.identity.domain.User;

public final class UserPersistenceMapper {

    private UserPersistenceMapper() {
    }

    public static User toDomain(UserJpaEntity entity) {
        if (entity == null) return null;
        return new User(
                entity.getId(),
                entity.getEmail(),
                entity.getPasswordHash(),
                entity.getFullName(),
                entity.getRole(),
                entity.isActive(),
                entity.isEmailVerified(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static UserJpaEntity toEntity(User user) {
        if (user == null) return null;

        UserJpaEntity entity = new UserJpaEntity();
        entity.setId(user.getId());
        entity.setEmail(user.getEmail());
        entity.setPasswordHash(user.getPasswordHash());
        entity.setFullName(user.getFullName());
        entity.setRole(user.getRole());
        entity.setActive(user.isActive());
        entity.setEmailVerified(user.isEmailVerified());
        entity.setCreatedAt(user.getCreatedAt());
        entity.setUpdatedAt(user.getUpdatedAt());
        return entity;
    }
}