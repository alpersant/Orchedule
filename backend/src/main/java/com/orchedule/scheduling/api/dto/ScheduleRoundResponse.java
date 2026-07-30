package com.orchedule.scheduling.api.dto;

import com.orchedule.scheduling.domain.ScheduleRound;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record ScheduleRoundResponse(
        UUID id,
        UUID seasonId,
        int weekNumber,
        String status,
        List<TeamAssignmentResponse> assignments,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static ScheduleRoundResponse from(ScheduleRound round) {
        return new ScheduleRoundResponse(
                round.getId(), round.getSeasonId(), round.getWeekNumber(), round.getStatus().name(),
                round.getAssignments().stream().map(TeamAssignmentResponse::from).toList(),
                round.getCreatedAt(), round.getUpdatedAt());
    }
}
