package com.orchedule.field.infrastructure.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataFieldWeeklyAvailabilityRepository extends JpaRepository<FieldWeeklyAvailabilityEntity, UUID> {

    List<FieldWeeklyAvailabilityEntity> findBySeasonIdAndWeekNumber(UUID seasonId, int weekNumber);

    List<FieldWeeklyAvailabilityEntity> findBySeasonId(UUID seasonId);

    Optional<FieldWeeklyAvailabilityEntity> findBySeasonIdAndWeekNumberAndFieldId(
            UUID seasonId, int weekNumber, UUID fieldId);
}
