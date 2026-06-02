package com.orchedule.identity.infrastructure.persistence;

import com.orchedule.identity.domain.RefreshToken;
import com.orchedule.identity.domain.RefreshTokenRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepository {

    private final SpringDataRefreshTokenRepository repository;

    public RefreshTokenRepositoryAdapter(SpringDataRefreshTokenRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<RefreshToken> findByTokenHash(String tokenHash) {
        return repository.findByTokenHash(tokenHash).map(RefreshTokenPersistenceMapper::toDomain);
    }

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        RefreshTokenJpaEntity saved = repository.save(RefreshTokenPersistenceMapper.toEntity(refreshToken));
        return RefreshTokenPersistenceMapper.toDomain(saved);
    }

    @Override
    public void revokeAllByUserId(UUID userId) {
        repository.deleteByUserId(userId);
    }
}