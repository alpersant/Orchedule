package com.orchedule.field.infrastructure.jpa;

import com.orchedule.field.domain.FieldWeeklyAvailability;
import com.orchedule.field.domain.FieldWeeklyAvailabilityRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class FieldWeeklyAvailabilityRepositoryJpaAdapter implements FieldWeeklyAvailabilityRepository {

    private final SpringDataFieldWeeklyAvailabilityRepository springDataRepository;

    public FieldWeeklyAvailabilityRepositoryJpaAdapter(SpringDataFieldWeeklyAvailabilityRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public FieldWeeklyAvailability save(FieldWeeklyAvailability availability) {
        FieldWeeklyAvailabilityEntity entity = new FieldWeeklyAvailabilityEntity(
                availability.getId(), availability.getSeasonId(), availability.getWeekNumber(),
                availability.getFieldId(), availability.isEnabled());
        FieldWeeklyAvailabilityEntity saved = springDataRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public List<FieldWeeklyAvailability> findBySeasonIdAndWeekNumber(UUID seasonId, int weekNumber) {
        return springDataRepository.findBySeasonIdAndWeekNumber(seasonId, weekNumber).stream()
                .map(this::toDomain).toList();
    }

    @Override
    public List<FieldWeeklyAvailability> findBySeasonId(UUID seasonId) {
        return springDataRepository.findBySeasonId(seasonId).stream()
                .map(this::toDomain).toList();
    }

    @Override
    public Optional<FieldWeeklyAvailability> findBySeasonIdAndWeekNumberAndFieldId(
            UUID seasonId, int weekNumber, UUID fieldId) {
        return springDataRepository.findBySeasonIdAndWeekNumberAndFieldId(seasonId, weekNumber, fieldId)
                .map(this::toDomain);
    }

    private FieldWeeklyAvailability toDomain(FieldWeeklyAvailabilityEntity entity) {
        return new FieldWeeklyAvailability(entity.getId(), entity.getSeasonId(),
                entity.getWeekNumber(), entity.getFieldId(), entity.isEnabled());
    }
}
