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

    public SeasonRepositoryJpaAdapter(SpringDataSeasonRepository springDataSeasonRepository) {
        this.springDataSeasonRepository = springDataSeasonRepository;
    }

    @Override
    public UUID create(String name,
                       LocalDate startDate,
                       LocalDate endDate,
                       SeasonStatus status,
                       boolean active) {
        SeasonEntity entity = SeasonEntity.create(
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
}