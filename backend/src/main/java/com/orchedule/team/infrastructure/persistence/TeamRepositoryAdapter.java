package com.orchedule.team.infrastructure.persistence;

import com.orchedule.team.application.exception.TeamNotFoundException;
import com.orchedule.team.domain.Team;
import com.orchedule.team.domain.TeamRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class TeamRepositoryAdapter implements TeamRepository {

    private final SpringDataTeamRepository repository;

    public TeamRepositoryAdapter(SpringDataTeamRepository repository) {
        this.repository = repository;
    }

    @Override
    public UUID create(String name) {
        TeamJpaEntity saved = repository.save(TeamPersistenceMapper.toNewEntity(name));
        return saved.getId();
    }

    @Override
    public Optional<Team> findById(UUID id) {
        return repository.findById(id).map(TeamPersistenceMapper::toDomain);
    }

    @Override
    public List<Team> findAll() {
        return repository.findAll().stream()
                .map(TeamPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsByNameIgnoreCase(String name) {
        return repository.existsByNameIgnoreCase(name);
    }

    @Override
    public Team update(UUID id, String name, boolean active) {
        TeamJpaEntity entity = repository.findById(id)
                .orElseThrow(() -> new TeamNotFoundException("Team not found: " + id));

        entity.setName(name);
        entity.setActive(active);
        entity.setUpdatedAt(OffsetDateTime.now());

        return TeamPersistenceMapper.toDomain(repository.save(entity));
    }
}
