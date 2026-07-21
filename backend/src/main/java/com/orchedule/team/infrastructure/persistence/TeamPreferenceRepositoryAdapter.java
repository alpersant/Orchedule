package com.orchedule.team.infrastructure.persistence;

import com.orchedule.team.domain.TeamPreference;
import com.orchedule.team.domain.TeamPreferenceRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class TeamPreferenceRepositoryAdapter implements TeamPreferenceRepository {

    private final SpringDataTeamPreferenceRepository repository;

    public TeamPreferenceRepositoryAdapter(SpringDataTeamPreferenceRepository repository) {
        this.repository = repository;
    }

    @Override
    public TeamPreference save(TeamPreference preference) {
        TeamPreferenceJpaEntity saved = repository.save(TeamPersistenceMapper.toEntity(preference));
        return TeamPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<TeamPreference> findByTeamId(UUID teamId) {
        return repository.findByTeamId(teamId).map(TeamPersistenceMapper::toDomain);
    }
}