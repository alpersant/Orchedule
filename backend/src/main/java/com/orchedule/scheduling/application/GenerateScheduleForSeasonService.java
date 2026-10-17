package com.orchedule.scheduling.application;

import com.orchedule.scheduling.application.exception.ScheduleGenerationException;
import com.orchedule.scheduling.application.exception.ScheduleRoundAlreadyExistsException;
import com.orchedule.scheduling.application.port.FieldScheduleSlot;
import com.orchedule.scheduling.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.util.*;

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

    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    @Transactional
    public ScheduleRound generate(GenerateRoundCommand command) {
        ScheduleValidator.validateWeekNumber(command.weekNumber());
        ScheduleValidator.validateTeamProfiles(command.teamProfiles());
        validateFieldSlots(command.fieldSlots());

        if (scheduleRoundRepository.existsBySeasonIdAndWeekNumber(command.seasonId(), command.weekNumber())) {
            throw new ScheduleRoundAlreadyExistsException(command.seasonId(), command.weekNumber());
        }

        Map<UUID, TeamRotationHistory> histories = new LinkedHashMap<>();
        for (TeamScheduleProfile profile : command.teamProfiles()) {
            histories.put(profile.getTeamId(), rotationHistoryStore.getOrCreate(profile.getTeamId()));
        }

        List<TeamAssignment> assignments = assignTeams(command, histories);
        ScheduleRound round = ScheduleRound.createDraft(command.seasonId(), command.weekNumber(), assignments);
        ScheduleRound saved = scheduleRoundRepository.save(round);

        for (TeamAssignment assignment : assignments) {
            TeamRotationHistory history = histories.get(assignment.teamId());
            history.record(assignment.slot());
            rotationHistoryStore.save(history);
        }

        return saved;
    }

    private void validateFieldSlots(List<FieldScheduleSlot> slots) {
        if (slots == null || slots.isEmpty()) {
            throw new com.orchedule.scheduling.application.exception.InvalidScheduleException(
                    "At least one field slot must be available for this round");
        }
    }

    private List<TeamAssignment> assignTeams(GenerateRoundCommand command,
                                             Map<UUID, TeamRotationHistory> histories) {
        List<TeamAssignment> assignments = new ArrayList<>();
        Set<String> occupied = new HashSet<>();
        int rotationIndex = 0;

        for (TeamScheduleProfile profile : command.teamProfiles()) {
            TeamRotationHistory history = histories.get(profile.getTeamId());
            List<FieldScheduleSlot> candidates = validSlots(command.fieldSlots(), profile);

            if (candidates.isEmpty()) {
                throw new ScheduleGenerationException(
                        "No compatible field slot for team " + profile.getTeamId());
            }

            FieldScheduleSlot slot = selectSlot(profile, history, candidates, occupied, rotationIndex);
            rotationIndex++;

            occupied.add(slotKey(slot));
            assignments.add(new TeamAssignment(
                    profile.getTeamId(),
                    new ScheduleSlot(slot.day(), slot.hour(), slot.fieldId())));
        }

        return assignments;
    }

    private List<FieldScheduleSlot> validSlots(List<FieldScheduleSlot> slots, TeamScheduleProfile profile) {
        Set<DayOfWeek> days = new HashSet<>(profile.daysWithPriority(SlotPriority.FIRST));
        days.addAll(profile.daysWithPriority(SlotPriority.SECOND));

        Set<java.time.LocalTime> hours = new HashSet<>(profile.hoursWithPriority(SlotPriority.FIRST));
        hours.addAll(profile.hoursWithPriority(SlotPriority.SECOND));

        return slots.stream()
                .filter(slot -> days.contains(slot.day()) && hours.contains(slot.hour()))
                .toList();
    }

    private FieldScheduleSlot selectSlot(TeamScheduleProfile profile,
                                         TeamRotationHistory history,
                                         List<FieldScheduleSlot> candidates,
                                         Set<String> occupied,
                                         int rotationIndex) {
        List<FieldScheduleSlot> preferred = candidates.stream()
                .filter(slot -> !history.isDayOverused(slot.day()) && !history.isHourOverused(slot.hour()))
                .toList();

        List<FieldScheduleSlot> ordered = preferred.isEmpty() ? candidates : preferred;

        for (int offset = 0; offset < ordered.size(); offset++) {
            FieldScheduleSlot candidate = ordered.get((rotationIndex + offset) % ordered.size());
            if (!occupied.contains(slotKey(candidate))) {
                return candidate;
            }
        }

        throw new ScheduleGenerationException(
                "No available field slot for team " + profile.getTeamId());
    }

    private String slotKey(FieldScheduleSlot slot) {
        return slot.day().name() + "|" + slot.hour() + "|" + slot.fieldId();
    }
}
