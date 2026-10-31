package com.orchedule.scheduling.application;

import com.orchedule.scheduling.application.exception.InvalidScheduleException;
import com.orchedule.scheduling.application.exception.ScheduleGenerationException;
import com.orchedule.scheduling.application.exception.ScheduleRoundAlreadyExistsException;
import com.orchedule.scheduling.application.port.FieldScheduleSlot;
import com.orchedule.scheduling.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Generates a draft schedule round from an already-assembled scheduling command.
 *
 * <p>This service owns the transactional boundary of round generation. External
 * data must arrive through {@link GenerateRoundCommand}, assembled by
 * {@link ScheduleInputAssembler}; this service never reads cross-module
 * aggregates directly.</p>
 */
@Service
public class GenerateScheduleForSeasonService {

    private final ScheduleRoundRepository scheduleRoundRepository;
    private final RotationEngine rotationEngine;
    private final TeamRotationHistoryStore rotationHistoryStore;

    public GenerateScheduleForSeasonService(
            ScheduleRoundRepository scheduleRoundRepository,
            RotationEngine rotationEngine,
            TeamRotationHistoryStore rotationHistoryStore
    ) {
        this.scheduleRoundRepository = scheduleRoundRepository;
        this.rotationEngine = rotationEngine;
        this.rotationHistoryStore = rotationHistoryStore;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    @Transactional
    public ScheduleRound generate(GenerateRoundCommand command) {
        validateCommand(command);

        var existingRound = scheduleRoundRepository.findBySeasonIdAndWeekNumber(
                command.seasonId(),
                command.weekNumber()
        );

        if (existingRound.isPresent()
                && existingRound.get().getStatus() != ScheduleRoundStatus.CANCELLED) {
            throw new ScheduleRoundAlreadyExistsException(
                    command.seasonId(),
                    command.weekNumber()
            );
        }

        Map<UUID, TeamRotationHistory> histories =
                loadRotationHistories(command.teamProfiles());
        List<TeamAssignment> assignments = assignTeams(command, histories);

        ScheduleRound round = existingRound
                .filter(current -> current.getStatus() == ScheduleRoundStatus.CANCELLED)
                .map(current -> current.replaceAssignmentsAndReactivate(assignments))
                .orElseGet(() -> ScheduleRound.createDraft(
                        command.seasonId(),
                        command.weekNumber(),
                        assignments
                ));

        ScheduleRound savedRound = scheduleRoundRepository.save(round);
        recordRotationHistory(assignments, histories);

        return savedRound;
    }

    private void validateCommand(GenerateRoundCommand command) {
        ScheduleValidator.validateWeekNumber(command.weekNumber());
        ScheduleValidator.validateTeamProfiles(command.teamProfiles());

        if (command.fieldSlots() == null || command.fieldSlots().isEmpty()) {
            throw new InvalidScheduleException(
                    "At least one field slot must be available for this round"
            );
        }

        validateUniqueTeams(command.teamProfiles());
        validateCapacity(command);
    }

    private void validateUniqueTeams(List<TeamScheduleProfile> profiles) {
        long distinctTeams = profiles.stream()
                .map(TeamScheduleProfile::getTeamId)
                .distinct()
                .count();

        if (distinctTeams != profiles.size()) {
            throw new InvalidScheduleException(
                    "A team schedule profile must appear only once per round"
            );
        }
    }

    private void validateCapacity(GenerateRoundCommand command) {
        long uniqueCompatibleSlots = command.fieldSlots().stream()
                .map(this::slotKey)
                .distinct()
                .count();

        if (uniqueCompatibleSlots < command.teamProfiles().size()) {
            throw new ScheduleGenerationException(
                    "Not enough compatible field slots to assign every team"
            );
        }
    }

    private Map<UUID, TeamRotationHistory> loadRotationHistories(
            List<TeamScheduleProfile> profiles
    ) {
        Map<UUID, TeamRotationHistory> histories = new LinkedHashMap<>();

        for (TeamScheduleProfile profile : profiles) {
            histories.put(
                    profile.getTeamId(),
                    rotationHistoryStore.getOrCreate(profile.getTeamId())
            );
        }

        return histories;
    }

    private List<TeamAssignment> assignTeams(
            GenerateRoundCommand command,
            Map<UUID, TeamRotationHistory> histories
    ) {
        List<TeamAssignment> assignments = new ArrayList<>();
        Set<String> occupiedSlots = new HashSet<>();
        int rotationIndex = 0;

        for (TeamScheduleProfile profile : command.teamProfiles()) {
            TeamRotationHistory history = histories.get(profile.getTeamId());
            List<FieldScheduleSlot> candidates =
                    compatibleSlots(command.fieldSlots(), profile);

            if (candidates.isEmpty()) {
                throw new ScheduleGenerationException(
                        "No compatible field slot for team " + profile.getTeamId()
                );
            }

            FieldScheduleSlot slot = selectSlot(
                    profile,
                    history,
                    candidates,
                    occupiedSlots,
                    rotationIndex
            );

            rotationIndex++;
            occupiedSlots.add(slotKey(slot));
            assignments.add(new TeamAssignment(
                    profile.getTeamId(),
                    new com.orchedule.scheduling.domain.ScheduleSlot(
                            slot.day(),
                            slot.hour(),
                            slot.fieldId()
                    )
            ));
        }

        return List.copyOf(assignments);
    }

    private List<FieldScheduleSlot> compatibleSlots(
            List<FieldScheduleSlot> slots,
            TeamScheduleProfile profile
    ) {
        var allowedDays = new HashSet<>(
                profile.daysWithPriority(SlotPriority.FIRST)
        );
        allowedDays.addAll(profile.daysWithPriority(SlotPriority.SECOND));

        var allowedHours = new HashSet<>(
                profile.hoursWithPriority(SlotPriority.FIRST)
        );
        allowedHours.addAll(profile.hoursWithPriority(SlotPriority.SECOND));

        return slots.stream()
                .filter(slot -> allowedDays.contains(slot.day()))
                .filter(slot -> allowedHours.contains(slot.hour()))
                .toList();
    }

    private FieldScheduleSlot selectSlot(
            TeamScheduleProfile profile,
            TeamRotationHistory history,
            List<FieldScheduleSlot> candidates,
            Set<String> occupiedSlots,
            int rotationIndex
    ) {
        List<FieldScheduleSlot> preferred = candidates.stream()
                .filter(slot -> !history.isDayOverused(slot.day()))
                .filter(slot -> !history.isHourOverused(slot.hour()))
                .toList();

        List<FieldScheduleSlot> ordered = preferred.isEmpty()
                ? candidates
                : preferred;

        for (int offset = 0; offset < ordered.size(); offset++) {
            FieldScheduleSlot candidate =
                    ordered.get((rotationIndex + offset) % ordered.size());

            if (!occupiedSlots.contains(slotKey(candidate))) {
                return candidate;
            }
        }

        throw new ScheduleGenerationException(
                "No available field slot for team " + profile.getTeamId()
        );
    }

    private void recordRotationHistory(
            List<TeamAssignment> assignments,
            Map<UUID, TeamRotationHistory> histories
    ) {
        for (TeamAssignment assignment : assignments) {
            TeamRotationHistory history = histories.get(assignment.teamId());
            history.record(assignment.slot());
            rotationHistoryStore.save(history);
        }
    }

    private String slotKey(FieldScheduleSlot slot) {
        return slot.day().name() + "|" + slot.hour() + "|" + slot.fieldId();
    }
}
