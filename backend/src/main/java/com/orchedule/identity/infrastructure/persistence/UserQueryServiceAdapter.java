package com.orchedule.identity.infrastructure.persistence;

import com.orchedule.identity.application.UserQueryService;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserQueryServiceAdapter implements UserQueryService {

    private final SpringDataUserRepository repository;

    public UserQueryServiceAdapter(SpringDataUserRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<UserPrincipal> findActiveById(UUID id) {
        return repository.findById(id)
                .filter(UserJpaEntity::isActive)
                .map(user -> new UserPrincipal(
                        user.getId(),
                        user.getEmail(),
                        user.getFullName(),
                        user.getRole().name()
                ));
    }
}