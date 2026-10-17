package com.orchedule.scheduling.application;

import com.orchedule.scheduling.application.port.FieldAvailabilityPort;
import com.orchedule.scheduling.application.port.FieldSchedulingContext;
import com.orchedule.scheduling.application.port.SeasonContextPort;
import com.orchedule.scheduling.application.port.TeamPreferencesPort;
import com.orchedule.scheduling.domain.TeamScheduleProfile;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Assembles scheduling inputs through narrow ACL ports. */
@Component
public class ScheduleInputAssembler {

    private final TeamPreferencesPort teamPreferencesPort;
    private final FieldAvailabilityPort fieldAvailabilityPort;
    private final SeasonContextPort seasonContextPort;

    public ScheduleInputAssembler(
            @Qualifier("schedulingTeamPreferencesAdapter") TeamPreferencesPort teamPreferencesPort,
            FieldAvailabilityPort fieldAvailabilityPort,
            SeasonContextPort seasonContextPort) {
        this.teamPreferencesPort = teamPreferencesPort;
        this.fieldAvailabilityPort = fieldAvailabilityPort;
        this.seasonContextPort = seasonContextPort;
    }

    public GenerateRoundCommand assembleFor(UUID seasonId, int weekNumber) {
        seasonContextPort.assertSeasonIsSchedulable(seasonId);
        UUID competitionId = seasonContextPort.getCompetitionIdForSeason(seasonId);

        List<TeamScheduleProfile> profiles = teamPreferencesPort.getProfilesForSeason(seasonId);
        FieldSchedulingContext context =
                fieldAvailabilityPort.getSchedulingContext(competitionId, seasonId, weekNumber);

        return new GenerateRoundCommand(seasonId, weekNumber, profiles, context.slots());
    }
}
