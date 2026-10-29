package com.orchedule.scheduling.application;

import com.orchedule.scheduling.application.port.FieldScheduleSlot;
import com.orchedule.scheduling.application.port.RoundReadinessContextPort;
import com.orchedule.scheduling.domain.ReadinessCode;
import com.orchedule.scheduling.domain.RoundReadiness;
import com.orchedule.scheduling.domain.TeamScheduleProfile;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ValidateRoundReadinessServiceTest {

    private final UUID seasonId = UUID.randomUUID();
    private final RoundReadinessContextPort contextPort = mock(RoundReadinessContextPort.class);

    @Test
    void reportsBlockersWhenThereAreNoTeamsOrFields() {
        when(contextPort.loadForWeek(seasonId, 1))
                .thenReturn(new RoundReadinessContext(List.of(), List.of(), null));

        var service = new ValidateRoundReadinessService(
                contextPort,
                List.of(new TeamReadinessRule(), new FieldReadinessRule(), new CapacityReadinessRule())
        );

        RoundReadinessResult result = service.validate(seasonId, 1);

        assertThat(result.readiness().ready()).isFalse();
        assertThat(result.readiness().issues())
                .extracting(issue -> issue.code())
                .contains(ReadinessCode.NO_TEAMS, ReadinessCode.NO_AVAILABLE_FIELDS);
    }

    @Test
    void reportsInsufficientCapacity() {
        var team = new TeamScheduleProfile(
                UUID.randomUUID(),
                List.of(new com.orchedule.scheduling.domain.DayOption(DayOfWeek.MONDAY, com.orchedule.scheduling.domain.SlotPriority.FIRST)),
                List.of(new com.orchedule.scheduling.domain.HourOption(LocalTime.of(18, 0), com.orchedule.scheduling.domain.SlotPriority.FIRST))
        );
        var slot = new FieldScheduleSlot(UUID.randomUUID(), DayOfWeek.MONDAY, LocalTime.of(18, 0));

        when(contextPort.loadForWeek(seasonId, 2))
                .thenReturn(new RoundReadinessContext(List.of(team, team), List.of(slot), null));

        var service = new ValidateRoundReadinessService(
                contextPort,
                List.of(new TeamReadinessRule(), new FieldReadinessRule(), new CapacityReadinessRule())
        );

        RoundReadinessResult result = service.validate(seasonId, 2);

        assertThat(result.readiness().issues())
                .extracting(issue -> issue.code())
                .contains(ReadinessCode.INSUFFICIENT_CAPACITY);
        assertThat(result.summary().eligibleTeams()).isEqualTo(2);
        assertThat(result.summary().compatibleSlots()).isEqualTo(1);
    }
}
