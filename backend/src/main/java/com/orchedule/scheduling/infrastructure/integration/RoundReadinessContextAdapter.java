package com.orchedule.scheduling.infrastructure.integration;

import com.orchedule.scheduling.application.RoundReadinessContext;
import com.orchedule.scheduling.application.port.FieldAvailabilityPort;
import com.orchedule.scheduling.application.port.FieldSchedulingContext;
import com.orchedule.scheduling.application.port.RoundReadinessContextPort;
import com.orchedule.scheduling.application.port.SeasonContextPort;
import com.orchedule.scheduling.application.port.TeamPreferencesPort;
import com.orchedule.scheduling.domain.ScheduleRoundRepository;
import com.orchedule.scheduling.domain.TeamScheduleProfile;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class RoundReadinessContextAdapter implements RoundReadinessContextPort {

    private final SeasonContextPort seasonContextPort;
    private final TeamPreferencesPort teamPreferencesPort;
    private final FieldAvailabilityPort fieldAvailabilityPort;
    private final ScheduleRoundRepository scheduleRoundRepository;

    public RoundReadinessContextAdapter(
            SeasonContextPort seasonContextPort,
            @Qualifier("schedulingTeamPreferencesAdapter") TeamPreferencesPort teamPreferencesPort,
            FieldAvailabilityPort fieldAvailabilityPort,
            ScheduleRoundRepository scheduleRoundRepository
    ) {
        this.seasonContextPort = seasonContextPort;
        this.teamPreferencesPort = teamPreferencesPort;
        this.fieldAvailabilityPort = fieldAvailabilityPort;
        this.scheduleRoundRepository = scheduleRoundRepository;
    }

    @Override
    public RoundReadinessContext loadForWeek(UUID seasonId, int weekNumber) {
        seasonContextPort.assertSeasonIsSchedulable(seasonId);

        UUID competitionId = seasonContextPort.getCompetitionIdForSeason(seasonId);

        List<TeamScheduleProfile> profiles =
                teamPreferencesPort.getProfilesForSeason(seasonId);

        FieldSchedulingContext fieldContext =
                fieldAvailabilityPort.getSchedulingContext(competitionId, seasonId, weekNumber);

        Optional<com.orchedule.scheduling.domain.ScheduleRound> existing =
                scheduleRoundRepository.findBySeasonIdAndWeekNumber(seasonId, weekNumber);

        String existingStatus = existing
                .map(round -> round.getStatus().name())
                .orElse(null);

        return new RoundReadinessContext(
                profiles,
                fieldContext.slots(),
                existingStatus
        );
    }
}