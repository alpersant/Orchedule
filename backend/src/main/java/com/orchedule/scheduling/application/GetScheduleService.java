package com.orchedule.scheduling.application;

import com.orchedule.scheduling.application.exception.ScheduleRoundNotFoundException;
import com.orchedule.scheduling.domain.ScheduleRound;
import com.orchedule.scheduling.domain.ScheduleRoundRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Read access requires an authenticated principal (teams/organizers/admins)
 * but no specific role — the calendar is meant to be visible to every
 * registered participant, just not to anonymous callers. Previously this
 * service had no authorization annotation at all.
 */
@Service
@Transactional(readOnly = true)
public class GetScheduleService {

    private final ScheduleRoundRepository scheduleRoundRepository;

    public GetScheduleService(ScheduleRoundRepository scheduleRoundRepository) {
        this.scheduleRoundRepository = scheduleRoundRepository;
    }

    @PreAuthorize("isAuthenticated()")
    public ScheduleRound getById(UUID id) {
        return scheduleRoundRepository.findById(id)
                .orElseThrow(() -> new ScheduleRoundNotFoundException(id));
    }

    @PreAuthorize("isAuthenticated()")
    public ScheduleRound getBySeasonAndWeek(UUID seasonId, int weekNumber) {
        return scheduleRoundRepository.findBySeasonIdAndWeekNumber(seasonId, weekNumber)
                .orElseThrow(() -> new ScheduleRoundNotFoundException(seasonId, weekNumber));
    }

    @PreAuthorize("isAuthenticated()")
    public List<ScheduleRound> getBySeason(UUID seasonId) {
        return scheduleRoundRepository.findBySeasonId(seasonId);
    }
}
