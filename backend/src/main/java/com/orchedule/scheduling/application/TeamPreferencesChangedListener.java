package com.orchedule.scheduling.application;

import com.orchedule.scheduling.domain.ScheduleRound;
import com.orchedule.scheduling.domain.ScheduleRoundRepository;
import com.orchedule.scheduling.domain.ScheduleRoundStatus;
import com.orchedule.team.domain.event.TeamPreferenceChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Reacts to team preference changes by cancelling affected DRAFT rounds so
 * an organizer knows to regenerate them. CONFIRMED rounds are left intact —
 * the calendar has already been published and a late preference change
 * must not silently reshuffle committed matches.
 *
 * Fixed: now queries only DRAFT rounds for the season directly at the
 * repository level (findBySeasonIdAndStatus) instead of loading every
 * round (including already CONFIRMED/CANCELLED ones) and filtering in
 * application code — avoids unnecessary data transfer as seasons grow.
 *
 * This intentionally bypasses CancelRoundService (which now enforces
 * @PreAuthorize hasAnyRole('ADMIN','ORGANIZER')): this is a trusted,
 * system-triggered side effect with no HTTP principal to authorize against,
 * not an end-user-initiated cancellation.
 */
@Component
public class TeamPreferencesChangedListener {

    private static final Logger log = LoggerFactory.getLogger(TeamPreferencesChangedListener.class);

    private final ScheduleRoundRepository scheduleRoundRepository;

    public TeamPreferencesChangedListener(ScheduleRoundRepository scheduleRoundRepository) {
        this.scheduleRoundRepository = scheduleRoundRepository;
    }

    @ApplicationModuleListener
    public void onTeamPreferencesChanged(TeamPreferenceChangedEvent event) {
        List<ScheduleRound> draftRounds =
                scheduleRoundRepository.findBySeasonIdAndStatus(event.seasonId(), ScheduleRoundStatus.DRAFT);

        for (ScheduleRound round : draftRounds) {
            boolean involvesTeam = round.getAssignments().stream()
                    .anyMatch(a -> a.teamId().equals(event.teamId()));
            if (involvesTeam) {
                log.info("Marking draft round {} (week {}) stale after preference change for team {}",
                        round.getId(), round.getWeekNumber(), event.teamId());
                round.cancel();
                scheduleRoundRepository.save(round);
            }
        }
    }
}
