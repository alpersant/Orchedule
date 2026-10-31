package com.orchedule.scheduling.application;

import com.orchedule.scheduling.application.port.FieldScheduleSlot;
import com.orchedule.scheduling.domain.DayOption;
import com.orchedule.scheduling.domain.HourOption;
import com.orchedule.scheduling.domain.ReadinessCode;
import com.orchedule.scheduling.domain.ReadinessSeverity;
import com.orchedule.scheduling.domain.RoundReadinessIssue;
import com.orchedule.scheduling.domain.SlotPriority;
import com.orchedule.scheduling.domain.TeamScheduleProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ValidateRoundReadinessServiceTest {

    private CapacityReadinessRule capacityReadinessRule;

    @BeforeEach
    void setUp() {
        capacityReadinessRule = new CapacityReadinessRule();
    }

    @Test
    void reportsInsufficientCapacity() {
        UUID teamAId = UUID.randomUUID();
        UUID teamBId = UUID.randomUUID();
        UUID fieldId = UUID.randomUUID();

        TeamScheduleProfile teamA = new TeamScheduleProfile(
                teamAId,
                List.of(new DayOption(DayOfWeek.MONDAY, SlotPriority.FIRST)),
                List.of(new HourOption(LocalTime.of(18, 0), SlotPriority.FIRST))
        );

        TeamScheduleProfile teamB = new TeamScheduleProfile(
                teamBId,
                List.of(new DayOption(DayOfWeek.MONDAY, SlotPriority.FIRST)),
                List.of(new HourOption(LocalTime.of(18, 0), SlotPriority.FIRST))
        );

        FieldScheduleSlot onlyAvailableSlot = new FieldScheduleSlot(
                fieldId,
                DayOfWeek.MONDAY,
                LocalTime.of(18, 0)
        );

        RoundReadinessContext context = new RoundReadinessContext(
                List.of(teamA, teamB),
                List.of(onlyAvailableSlot),
                null
        );

        List<RoundReadinessIssue> issues =
                capacityReadinessRule.validate(context, 1);

        assertThat(issues)
                .extracting(RoundReadinessIssue::code)
                .containsExactly(ReadinessCode.INSUFFICIENT_CAPACITY);

        assertThat(issues.getFirst().severity())
                .isEqualTo(ReadinessSeverity.BLOCKER);
    }

    @Test
    void doesNotReportInsufficientCapacityWhenEveryTeamHasAUniqueSlot() {
        UUID teamAId = UUID.randomUUID();
        UUID teamBId = UUID.randomUUID();
        UUID fieldAId = UUID.randomUUID();
        UUID fieldBId = UUID.randomUUID();

        TeamScheduleProfile teamA = new TeamScheduleProfile(
                teamAId,
                List.of(new DayOption(DayOfWeek.MONDAY, SlotPriority.FIRST)),
                List.of(new HourOption(LocalTime.of(18, 0), SlotPriority.FIRST))
        );

        TeamScheduleProfile teamB = new TeamScheduleProfile(
                teamBId,
                List.of(new DayOption(DayOfWeek.MONDAY, SlotPriority.FIRST)),
                List.of(new HourOption(LocalTime.of(18, 0), SlotPriority.FIRST))
        );

        FieldScheduleSlot slotA = new FieldScheduleSlot(
                fieldAId,
                DayOfWeek.MONDAY,
                LocalTime.of(18, 0)
        );

        FieldScheduleSlot slotB = new FieldScheduleSlot(
                fieldBId,
                DayOfWeek.MONDAY,
                LocalTime.of(18, 0)
        );

        RoundReadinessContext context = new RoundReadinessContext(
                List.of(teamA, teamB),
                List.of(slotA, slotB),
                null
        );

        List<RoundReadinessIssue> issues =
                capacityReadinessRule.validate(context, 1);

        assertThat(issues).isEmpty();
    }
}