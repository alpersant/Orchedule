package com.orchedule.identity.infrastructure.persistence;

import com.orchedule.identity.api.UserAuthQuery;
import com.orchedule.identity.api.UserAuthView;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserAuthQueryAdapter implements UserAuthQuery {

    private final SpringDataUserRepository repository;

    public UserAuthQueryAdapter(SpringDataUserRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<UserAuthView> findByEmail(String email) {
        return repository.findByEmail(email.trim().toLowerCase())
                .map(this::toView);
    }

    @Override
    public Optional<UserAuthView> findActiveById(UUID id) {
        return repository.findById(id)
                .filter(UserJpaEntity::isActive)
                .map(this::toView);
    }

    private UserAuthView toView(UserJpaEntity entity) {
        return new UserAuthView(
                entity.getId(),
                entity.getEmail(),
                entity.getFullName(),
                entity.getRole().name(),
                entity.getPasswordHash(),
                entity.isActive()
        );
    }
}