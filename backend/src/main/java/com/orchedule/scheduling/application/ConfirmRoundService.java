package com.orchedule.scheduling.application;

import com.orchedule.scheduling.application.exception.ScheduleGenerationException;
import com.orchedule.scheduling.application.exception.ScheduleRoundNotFoundException;
import com.orchedule.scheduling.domain.ScheduleRound;
import com.orchedule.scheduling.domain.ScheduleRoundRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Confirming a round publishes it as the official calendar for that week —
 * same authorization boundary as generation. Previously this service had
 * NO authorization check at all, meaning any authenticated caller (or an
 * unauthenticated one, depending on filter chain config) could confirm or
 * lock in a round. Fixed by requiring ADMIN/ORGANIZER, consistent with
 * GenerateScheduleForSeasonService.
 */
@Service
public class ConfirmRoundService {

    private final ScheduleRoundRepository scheduleRoundRepository;

    public ConfirmRoundService(ScheduleRoundRepository scheduleRoundRepository) {
        this.scheduleRoundRepository = scheduleRoundRepository;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    @Transactional
    public ScheduleRound confirm(UUID roundId) {
        ScheduleRound round = scheduleRoundRepository.findById(roundId)
                .orElseThrow(() -> new ScheduleRoundNotFoundException(roundId));

        try {
            round.confirm();
        } catch (IllegalStateException ex) {
            throw new ScheduleGenerationException(ex.getMessage());
        }

        return scheduleRoundRepository.save(round);
    }
}
