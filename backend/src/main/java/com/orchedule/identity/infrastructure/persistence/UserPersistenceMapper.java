package com.orchedule.identity.infrastructure.persistence;

import com.orchedule.identity.domain.RegisterUserCommand;
import com.orchedule.identity.domain.User;

import java.time.OffsetDateTime;
import java.util.UUID;

public final class UserPersistenceMapper {

    private UserPersistenceMapper() {
    }

    public static UserJpaEntity toEntity(RegisterUserCommand command) {
        OffsetDateTime now = OffsetDateTime.now();

        UserJpaEntity entity = new UserJpaEntity();
        entity.setId(UUID.randomUUID());
        entity.setEmail(command.email().trim().toLowerCase());
        entity.setFullName(command.fullName().trim());
        entity.setPasswordHash(command.passwordHash());
        entity.setRole(command.role());
        entity.setActive(command.active());
        entity.setEmailVerified(command.emailVerified());
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        return entity;
    }

    public static User toDomain(UserJpaEntity entity) {
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
}