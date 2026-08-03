package com.orchedule.scheduling.application;

import com.orchedule.scheduling.application.exception.ScheduleGenerationException;
import com.orchedule.scheduling.application.exception.ScheduleRoundNotFoundException;
import com.orchedule.scheduling.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;


@Service
public class RegenerateRoundService {

    private final ScheduleRoundRepository scheduleRoundRepository;
    private final RotationEngine rotationEngine;
    private final TeamRotationHistoryStore rotationHistoryStore;

    public RegenerateRoundService(ScheduleRoundRepository scheduleRoundRepository,
                                   RotationEngine rotationEngine,
                                   TeamRotationHistoryStore rotationHistoryStore) {
        this.scheduleRoundRepository = scheduleRoundRepository;
        this.rotationEngine = rotationEngine;
        this.rotationHistoryStore = rotationHistoryStore;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    @Transactional
    public ScheduleRound regenerate(UUID roundId, List<TeamScheduleProfile> teamProfiles,
                                     Set<DayOfWeek> openDaysThisRound, Set<LocalTime> openHoursThisRound,
                                     List<UUID> availableFieldIds) {
        ScheduleValidator.validateTeamProfiles(teamProfiles);
        ScheduleValidator.validateOpenDays(openDaysThisRound);
        ScheduleValidator.validateOpenHours(openHoursThisRound);
        ScheduleValidator.validateFieldIds(availableFieldIds);

        ScheduleRound round = scheduleRoundRepository.findById(roundId)
                .orElseThrow(() -> new ScheduleRoundNotFoundException(roundId));

        List<TeamAssignment> newAssignments = new ArrayList<>();
        Set<String> occupiedSlots = new HashSet<>();
        int fieldIndex = 0;

        for (TeamScheduleProfile profile : teamProfiles) {
            TeamRotationHistory history = rotationHistoryStore.getOrCreate(profile.getTeamId());

            DayOfWeek day;
            LocalTime hour;
            try {
                day = rotationEngine.selectDay(profile, history, openDaysThisRound);
                hour = rotationEngine.selectHour(profile, history, openHoursThisRound);
            } catch (IllegalStateException ex) {
                throw new ScheduleGenerationException(
                        "Could not reassign a slot for team " + profile.getTeamId() + ": " + ex.getMessage());
            }

            UUID fieldId = findAvailableField(
                    availableFieldIds, fieldIndex, day, hour, occupiedSlots, profile.getTeamId());
            fieldIndex++;

            occupiedSlots.add(slotKey(day, hour, fieldId));
            newAssignments.add(new TeamAssignment(profile.getTeamId(), new ScheduleSlot(day, hour, fieldId)));
        }

        round.replaceAssignments(newAssignments);
        return scheduleRoundRepository.save(round);
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
