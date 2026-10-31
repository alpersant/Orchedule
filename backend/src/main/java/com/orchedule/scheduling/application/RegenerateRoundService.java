package com.orchedule.scheduling.application;

import com.orchedule.scheduling.application.exception.ScheduleGenerationException;
import com.orchedule.scheduling.application.exception.ScheduleRoundNotFoundException;
import com.orchedule.scheduling.application.port.FieldAvailabilityPort;
import com.orchedule.scheduling.application.port.FieldSchedulingContext;
import com.orchedule.scheduling.application.port.SeasonContextPort;
import com.orchedule.scheduling.application.port.TeamPreferencesPort;
import com.orchedule.scheduling.domain.ScheduleRound;
import com.orchedule.scheduling.domain.ScheduleRoundRepository;
import com.orchedule.scheduling.domain.ScheduleRoundStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Regenerates an existing cancelled schedule round using the current season context.
 *
 * <p>This use case does not accept team profiles, field identifiers, days or hours
 * from the client. All scheduling inputs are resolved through the ACL to prevent
 * callers from injecting inconsistent or unauthorized data.</p>
 */
@Service
public class RegenerateRoundService {

    private final ScheduleRoundRepository scheduleRoundRepository;
    private final SeasonContextPort seasonContextPort;
    private final TeamPreferencesPort teamPreferencesPort;
    private final FieldAvailabilityPort fieldAvailabilityPort;
    private final GenerateScheduleForSeasonService generateScheduleForSeasonService;

    public RegenerateRoundService(
            ScheduleRoundRepository scheduleRoundRepository,
            SeasonContextPort seasonContextPort,
            @Qualifier("schedulingTeamPreferencesAdapter")
            TeamPreferencesPort teamPreferencesPort,
            FieldAvailabilityPort fieldAvailabilityPort,
            GenerateScheduleForSeasonService generateScheduleForSeasonService
    ) {
        this.scheduleRoundRepository = scheduleRoundRepository;
        this.seasonContextPort = seasonContextPort;
        this.teamPreferencesPort = teamPreferencesPort;
        this.fieldAvailabilityPort = fieldAvailabilityPort;
        this.generateScheduleForSeasonService = generateScheduleForSeasonService;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    @Transactional
    public ScheduleRound regenerate(UUID roundId) {
        ScheduleRound round = scheduleRoundRepository.findById(roundId)
                .orElseThrow(() -> new ScheduleRoundNotFoundException(roundId));

        if (round.getStatus() != ScheduleRoundStatus.CANCELLED) {
            throw new ScheduleGenerationException(
                    "Only a cancelled round can be regenerated"
            );
        }

        UUID seasonId = round.getSeasonId();
        int weekNumber = round.getWeekNumber();

        seasonContextPort.assertSeasonIsSchedulable(seasonId);
        UUID competitionId = seasonContextPort.getCompetitionIdForSeason(seasonId);

        var teamProfiles = teamPreferencesPort.getProfilesForSeason(seasonId);
        FieldSchedulingContext fieldContext = fieldAvailabilityPort.getSchedulingContext(
                competitionId,
                seasonId,
                weekNumber
        );

        return generateScheduleForSeasonService.generate(
                new GenerateRoundCommand(
                        seasonId,
                        weekNumber,
                        teamProfiles,
                        fieldContext.slots()
                )
        );
    }
}