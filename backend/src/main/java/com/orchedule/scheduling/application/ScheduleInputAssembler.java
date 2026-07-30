package com.orchedule.scheduling.application;

import com.orchedule.scheduling.application.port.FieldAvailabilityPort;
import com.orchedule.scheduling.application.port.FieldSchedulingContext;
import com.orchedule.scheduling.application.port.SeasonContextPort;
import com.orchedule.scheduling.application.port.TeamPreferencesPort;
import com.orchedule.scheduling.domain.TeamScheduleProfile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Assembles the inputs required to generate a round by querying the ACL
 * ports for team, field and season data. This is the single seam where
 * scheduling talks to the outside world — GenerateScheduleForSeasonService
 * itself stays free of any cross-module dependency.
 */
@Component
public class ScheduleInputAssembler {

    private final TeamPreferencesPort teamPreferencesPort;
    private final FieldAvailabilityPort fieldAvailabilityPort;
    private final SeasonContextPort seasonContextPort;

    public ScheduleInputAssembler(TeamPreferencesPort teamPreferencesPort,
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
        FieldSchedulingContext fieldContext =
                fieldAvailabilityPort.getSchedulingContext(competitionId, seasonId, weekNumber);

        return new GenerateRoundCommand(seasonId, weekNumber, profiles,
                fieldContext.openDays(), fieldContext.openHours(), fieldContext.availableFieldIds());
    }
}
