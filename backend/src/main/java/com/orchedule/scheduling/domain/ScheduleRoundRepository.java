package com.orchedule.scheduling.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ScheduleRoundRepository {

    ScheduleRound save(ScheduleRound round);

    Optional<ScheduleRound> findById(UUID id);

    Optional<ScheduleRound> findBySeasonIdAndWeekNumber(UUID seasonId, int weekNumber);

    List<ScheduleRound> findBySeasonId(UUID seasonId);

    /**
     * Targeted query used by TeamPreferencesChangedListener — avoids
     * loading every round of the season (including CONFIRMED/CANCELLED
     * ones) just to filter them in application code.
     */
    List<ScheduleRound> findBySeasonIdAndStatus(UUID seasonId, ScheduleRoundStatus status);

    boolean existsBySeasonIdAndWeekNumber(UUID seasonId, int weekNumber);
}
