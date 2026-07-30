package com.orchedule.scheduling.application;

import com.orchedule.scheduling.application.exception.ScheduleGenerationException;
import com.orchedule.scheduling.application.exception.ScheduleRoundAlreadyExistsException;
import com.orchedule.scheduling.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;

/**
 * Orchestrates generation of a single round (jornada), delegating the
 * actual assignment logic to the pure {@link RotationEngine}.
 *
 * This service has ZERO dependency on team/field/competition modules —
 * all cross-module data arrives pre-assembled via {@link GenerateRoundCommand}
 * (built by {@link ScheduleInputAssembler} through ACL ports). This keeps
 * the Spring Modulith module boundary clean and this class trivially
 * unit-testable.
 */
@Service
public class GenerateScheduleForSeasonService {

    private final ScheduleRoundRepository scheduleRoundRepository;
    private final RotationEngine rotationEngine;
    private final TeamRotationHistoryStore rotationHistoryStore;

    public GenerateScheduleForSeasonService(ScheduleRoundRepository scheduleRoundRepository,
                                             RotationEngine rotationEngine,
                                             TeamRotationHistoryStore rotationHistoryStore) {
        this.scheduleRoundRepository = scheduleRoundRepository;
        this.rotationEngine = rotationEngine;
        this.rotationHistoryStore = rotationHistoryStore;
    }

    /**
     * Only competition organizers/admins may trigger schedule generation —
     * this mutates the official calendar seen by every team.
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    @Transactional
    public ScheduleRound generate(GenerateRoundCommand command) {
        return generate(command.seasonId(), command.weekNumber(), command.teamProfiles(),
                command.openDays(), command.openHours(), command.availableFieldIds());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    @Transactional
    public ScheduleRound generate(UUID seasonId, int weekNumber, List<TeamScheduleProfile> teamProfiles,
                                   Set<DayOfWeek> openDaysThisRound, Set<LocalTime> openHoursThisRound,
                                   List<UUID> availableFieldIds) {
        ScheduleValidator.validateWeekNumber(weekNumber);
        ScheduleValidator.validateTeamProfiles(teamProfiles);
        ScheduleValidator.validateOpenDays(openDaysThisRound);
        ScheduleValidator.validateOpenHours(openHoursThisRound);
        ScheduleValidator.validateFieldIds(availableFieldIds);

        if (scheduleRoundRepository.existsBySeasonIdAndWeekNumber(seasonId, weekNumber)) {
            throw new ScheduleRoundAlreadyExistsException(seasonId, weekNumber);
        }

        Map<UUID, TeamRotationHistory> historiesByTeam = new LinkedHashMap<>();
        for (TeamScheduleProfile profile : teamProfiles) {
            historiesByTeam.put(profile.getTeamId(), rotationHistoryStore.getOrCreate(profile.getTeamId()));
        }

        List<TeamAssignment> assignments = assignTeams(
                teamProfiles, historiesByTeam, openDaysThisRound, openHoursThisRound, availableFieldIds);

        ScheduleRound round = ScheduleRound.createDraft(seasonId, weekNumber, assignments);
        ScheduleRound saved = scheduleRoundRepository.save(round);

        for (TeamAssignment assignment : assignments) {
            TeamRotationHistory history = historiesByTeam.get(assignment.teamId());
            history.record(assignment.slot());
            rotationHistoryStore.save(history);
        }

        return saved;
    }

    private List<TeamAssignment> assignTeams(List<TeamScheduleProfile> teamProfiles,
                                              Map<UUID, TeamRotationHistory> historiesByTeam,
                                              Set<DayOfWeek> openDays, Set<LocalTime> openHours,
                                              List<UUID> fieldIds) {
        List<TeamAssignment> assignments = new ArrayList<>();
        Set<String> occupiedSlots = new HashSet<>();
        int fieldIndex = 0;

        for (TeamScheduleProfile profile : teamProfiles) {
            TeamRotationHistory history = historiesByTeam.get(profile.getTeamId());

            DayOfWeek day;
            LocalTime hour;
            try {
                day = rotationEngine.selectDay(profile, history, openDays);
                hour = rotationEngine.selectHour(profile, history, openHours);
            } catch (IllegalStateException ex) {
                throw new ScheduleGenerationException(
                        "Could not assign a slot for team " + profile.getTeamId() + ": " + ex.getMessage());
            }

            UUID fieldId = findAvailableField(fieldIds, fieldIndex, day, hour, occupiedSlots, profile.getTeamId());
            fieldIndex++;

            occupiedSlots.add(slotKey(day, hour, fieldId));
            assignments.add(new TeamAssignment(profile.getTeamId(), new ScheduleSlot(day, hour, fieldId)));
        }

        return assignments;
    }

    private UUID findAvailableField(List<UUID> fieldIds, int startIndex, DayOfWeek day, LocalTime hour,
                                     Set<String> occupiedSlots, UUID teamId) {
        for (int offset = 0; offset < fieldIds.size(); offset++) {
            UUID candidate = fieldIds.get((startIndex + offset) % fieldIds.size());
            if (!occupiedSlots.contains(slotKey(day, hour, candidate))) {
                return candidate;
            }
        }
        throw new ScheduleGenerationException(
                "No available field left for team " + teamId + " at " + day + " " + hour
                        + " — all fields are already occupied for this slot");
    }

    private String slotKey(DayOfWeek day, LocalTime hour, UUID fieldId) {
        return day.name() + "|" + hour + "|" + fieldId;
    }
}
