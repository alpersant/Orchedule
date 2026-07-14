package com.orchedule.identity.infrastructure.persistence;

import com.orchedule.identity.domain.RegisterUserCommand;
import com.orchedule.identity.domain.User;
import com.orchedule.identity.domain.UserRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class UserRepositoryAdapter implements UserRepository {

    private final SpringDataUserRepository repository;

    public UserRepositoryAdapter(SpringDataUserRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<User> findById(UUID id) {
        return repository.findById(id).map(UserPersistenceMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return repository.findByEmail(email).map(UserPersistenceMapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }

    @Override
    public UUID register(RegisterUserCommand command) {
        UserJpaEntity entity = UserPersistenceMapper.toEntity(command);
        UserJpaEntity saved = repository.save(entity);
        return saved.getId();
    }
}