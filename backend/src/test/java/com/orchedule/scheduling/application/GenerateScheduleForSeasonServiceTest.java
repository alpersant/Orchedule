package com.orchedule.scheduling.application;

import com.orchedule.scheduling.application.exception.ScheduleRoundAlreadyExistsException;
import com.orchedule.scheduling.application.port.FieldScheduleSlot;
import com.orchedule.scheduling.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GenerateScheduleForSeasonServiceTest {

    @Mock
    private ScheduleRoundRepository scheduleRoundRepository;

    @Mock
    private TeamRotationHistoryStore rotationHistoryStore;

    private GenerateScheduleForSeasonService service;

    private final UUID seasonId = UUID.randomUUID();
    private final UUID teamId = UUID.randomUUID();
    private final UUID fieldId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new GenerateScheduleForSeasonService(
                scheduleRoundRepository,
                new RotationEngine(),
                rotationHistoryStore
        );
    }

    @Test
    void generatesDraftRoundWithOneAssignmentPerTeam() {
        GenerateRoundCommand command = command();

        when(scheduleRoundRepository.findBySeasonIdAndWeekNumber(seasonId, 1))
                .thenReturn(Optional.empty());
        when(rotationHistoryStore.getOrCreate(teamId))
                .thenReturn(new TeamRotationHistory(teamId));
        when(scheduleRoundRepository.save(any(ScheduleRound.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ScheduleRound round = service.generate(command);

        assertThat(round.getStatus()).isEqualTo(ScheduleRoundStatus.DRAFT);
        assertThat(round.getAssignments()).hasSize(1);
        assertThat(round.getAssignments().getFirst().teamId()).isEqualTo(teamId);
        assertThat(round.getAssignments().getFirst().slot().day())
                .isEqualTo(DayOfWeek.MONDAY);
        assertThat(round.getAssignments().getFirst().slot().hour())
                .isEqualTo(LocalTime.of(18, 0));
        assertThat(round.getAssignments().getFirst().slot().fieldId())
                .isEqualTo(fieldId);
    }

    @Test
    void rejectsGenerationWhenActiveRoundAlreadyExists() {
        GenerateRoundCommand command = command();

        when(scheduleRoundRepository.findBySeasonIdAndWeekNumber(seasonId, 1))
                .thenReturn(Optional.of(existingDraftRound()));

        assertThatThrownBy(() -> service.generate(command))
                .isInstanceOf(ScheduleRoundAlreadyExistsException.class);
    }

    private GenerateRoundCommand command() {
        TeamScheduleProfile profile = new TeamScheduleProfile(
                teamId,
                List.of(new DayOption(DayOfWeek.MONDAY, SlotPriority.FIRST)),
                List.of(new HourOption(LocalTime.of(18, 0), SlotPriority.FIRST))
        );

        FieldScheduleSlot slot = new FieldScheduleSlot(
                fieldId,
                DayOfWeek.MONDAY,
                LocalTime.of(18, 0)
        );

        return new GenerateRoundCommand(
                seasonId,
                1,
                List.of(profile),
                List.of(slot)
        );
    }

    private ScheduleRound existingDraftRound() {
        TeamAssignment assignment = new TeamAssignment(
                teamId,
                new com.orchedule.scheduling.domain.ScheduleSlot(
                        DayOfWeek.MONDAY,
                        LocalTime.of(18, 0),
                        fieldId
                )
        );

        return ScheduleRound.createDraft(
                seasonId,
                1,
                List.of(assignment)
        );
    }
}
