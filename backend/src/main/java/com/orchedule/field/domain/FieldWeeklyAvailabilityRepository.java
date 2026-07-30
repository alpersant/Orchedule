package com.orchedule.field.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FieldWeeklyAvailabilityRepository {

    FieldWeeklyAvailability save(FieldWeeklyAvailability availability);

    List<FieldWeeklyAvailability> findBySeasonIdAndWeekNumber(UUID seasonId, int weekNumber);

    List<FieldWeeklyAvailability> findBySeasonId(UUID seasonId);

    Optional<FieldWeeklyAvailability> findBySeasonIdAndWeekNumberAndFieldId(
            UUID seasonId, int weekNumber, UUID fieldId);
}
