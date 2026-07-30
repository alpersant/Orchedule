package com.orchedule.scheduling.application;

import com.orchedule.scheduling.application.exception.ScheduleRoundNotFoundException;
import com.orchedule.scheduling.domain.ScheduleRound;
import com.orchedule.scheduling.domain.ScheduleRoundRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Same fix as ConfirmRoundService: cancellation now requires ADMIN/ORGANIZER.
 * Note the internal, event-driven cancellation performed by
 * TeamPreferencesChangedListener does NOT go through this service — it
 * calls ScheduleRound.cancel() + repository.save() directly, since that
 * path is a trusted system reaction, not an end-user request, and must not
 * be blocked by a caller-role check that has no HTTP principal to evaluate.
 */
@Service
public class CancelRoundService {

    private final ScheduleRoundRepository scheduleRoundRepository;

    public CancelRoundService(ScheduleRoundRepository scheduleRoundRepository) {
        this.scheduleRoundRepository = scheduleRoundRepository;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    @Transactional
    public ScheduleRound cancel(UUID roundId) {
        ScheduleRound round = scheduleRoundRepository.findById(roundId)
                .orElseThrow(() -> new ScheduleRoundNotFoundException(roundId));

        round.cancel();
        return scheduleRoundRepository.save(round);
    }
}
