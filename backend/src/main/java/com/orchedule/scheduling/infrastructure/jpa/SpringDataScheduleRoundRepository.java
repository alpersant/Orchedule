package com.orchedule.scheduling.infrastructure.jpa;

import com.orchedule.scheduling.domain.ScheduleRoundStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataScheduleRoundRepository extends JpaRepository<ScheduleRoundEntity, UUID> {

    Optional<ScheduleRoundEntity> findBySeasonIdAndWeekNumber(UUID seasonId, int weekNumber);

    List<ScheduleRoundEntity> findBySeasonId(UUID seasonId);

    List<ScheduleRoundEntity> findBySeasonIdAndStatus(UUID seasonId, ScheduleRoundStatus status);

    boolean existsBySeasonIdAndWeekNumber(UUID seasonId, int weekNumber);
}
