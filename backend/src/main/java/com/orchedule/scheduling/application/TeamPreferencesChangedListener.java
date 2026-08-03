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
