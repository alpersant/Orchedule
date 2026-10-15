package com.orchedule.season.infrastructure.jpa;

import com.orchedule.season.domain.Season;
import com.orchedule.season.domain.SeasonRepository;
import com.orchedule.season.domain.SeasonStatus;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class SeasonRepositoryJpaAdapter implements SeasonRepository {

    private final SpringDataSeasonRepository springDataSeasonRepository;
    private final org.springframework.jdbc.core.JdbcTemplate jdbc;

    public SeasonRepositoryJpaAdapter(SpringDataSeasonRepository springDataSeasonRepository, org.springframework.jdbc.core.JdbcTemplate jdbc) {
        this.springDataSeasonRepository = springDataSeasonRepository;
        this.jdbc = jdbc;
    }

    @Override
    public UUID create(UUID competitionId, String name,
                       LocalDate startDate,
                       LocalDate endDate,
                       SeasonStatus status,
                       boolean active) {
        SeasonEntity entity = SeasonEntity.create(
                competitionId,
                name,
                startDate,
                endDate,
                status,
                active
        );

        return springDataSeasonRepository.save(entity).getId();
    }

    @Override
    public Season update(UUID id,
                         String name,
                         LocalDate startDate,
                         LocalDate endDate,
                         SeasonStatus status,
                         boolean active) {
        SeasonEntity entity = springDataSeasonRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Season persistence inconsistency for id: " + id));

        entity.update(name, startDate, endDate, status, active);

        return springDataSeasonRepository.save(entity).toDomain();
    }

    @Override
    public Optional<Season> findById(UUID id) {
        return springDataSeasonRepository.findById(id)
                .map(SeasonEntity::toDomain);
    }

    @Override
    public List<Season> findAll() {
        return springDataSeasonRepository.findAllByOrderByStartDateAsc()
                .stream()
                .map(SeasonEntity::toDomain)
                .toList();
    }

    @Override
    public boolean existsByNameIgnoreCase(String name) {
        return springDataSeasonRepository.existsByNameIgnoreCase(name);
    }
    @Override
    public boolean existsByCompetitionIdAndNameIgnoreCase(UUID competitionId, String name) {
        return springDataSeasonRepository.existsByCompetitionIdAndNameIgnoreCase(competitionId, name);
    }

    @Override
    public org.springframework.data.domain.Page<Season> search(UUID competitionId, String search,
            SeasonStatus status, org.springframework.data.domain.Pageable pageable) {
        org.springframework.data.jpa.domain.Specification<SeasonEntity> spec = (root, query, cb) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            if (competitionId != null) predicates.add(cb.equal(root.get("competitionId"), competitionId));
            if (status != null) predicates.add(cb.equal(root.get("status"), status));
            if (search != null && !search.isBlank()) {
                String escaped = search.trim().toLowerCase(java.util.Locale.ROOT)
                        .replace("!", "!!").replace("%", "!%").replace("_", "!_");
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + escaped + "%", '!'));
            }
            return cb.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
        return springDataSeasonRepository.findAll(spec, pageable).map(SeasonEntity::toDomain);
    }

    @Override
    public boolean deleteById(UUID id) {
        if (!springDataSeasonRepository.existsById(id)) return false;
        Boolean unclassifiedReferences = jdbc.queryForObject(
            "select exists(select 1 from availability_rule where reference_id = ?)", Boolean.class, id);
        if (Boolean.TRUE.equals(unclassifiedReferences)) {
            throw new com.orchedule.season.application.exception.InvalidSeasonException(
                "Season has availability references; classify their scope before deletion");
        }
        return springDataSeasonRepository.deleteRow(id) == 1;
    }
}
