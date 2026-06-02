package com.orchedule.identity.infrastructure.persistence;

import com.orchedule.identity.application.AuthenticatedUserPort;
import com.orchedule.identity.application.AuthenticatedUserData;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class AuthenticatedUserPortAdapter implements AuthenticatedUserPort {

    private final SpringDataUserRepository repository;

    public AuthenticatedUserPortAdapter(SpringDataUserRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<AuthenticatedUserData> findByEmail(String email) {
        return repository.findByEmail(email.trim().toLowerCase())
                .map(this::toData);
    }

    @Override
    public Optional<AuthenticatedUserData> findActiveById(UUID id) {
        return repository.findById(id)
                .filter(UserJpaEntity::isActive)
                .map(this::toData);
    }

    private AuthenticatedUserData toData(UserJpaEntity entity) {
        return new AuthenticatedUserData(
                entity.getId(),
                entity.getEmail(),
                entity.getFullName(),
                entity.getRole().name(),
                entity.getPasswordHash(),
                entity.isActive()
        );
    }
}