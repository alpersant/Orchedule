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

    @Override
    public int deleteById(UUID teamId) {
        return repository.deleteTeamById(teamId);
    }

    @Override
    public com.orchedule.shared.pagination.PageResult<Team> search(String search, Boolean active, int page, int size) {
        if (page < 0 || size < 1 || size > 100 || (search != null && search.length() > 150))
            throw new IllegalArgumentException("Invalid pagination or search");
        org.springframework.data.jpa.domain.Specification<TeamJpaEntity> spec = (root, query, cb) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            if (active != null) predicates.add(cb.equal(root.get("active"), active));
            if (search != null && !search.isBlank()) {
                String escaped = search.trim().toLowerCase(java.util.Locale.ROOT)
                    .replace("!", "!!").replace("%", "!%").replace("_", "!_");
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + escaped + "%", '!'));
            }
            return cb.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
        var result = repository.findAll(spec, org.springframework.data.domain.PageRequest.of(page, size,
            org.springframework.data.domain.Sort.by("name").and(org.springframework.data.domain.Sort.by("id"))))
            .map(TeamPersistenceMapper::toDomain);
        return new com.orchedule.shared.pagination.PageResult<>(result.getContent(), result.getNumber(),
            result.getSize(), result.getTotalElements(), result.getTotalPages(), result.isLast());
    }

}

