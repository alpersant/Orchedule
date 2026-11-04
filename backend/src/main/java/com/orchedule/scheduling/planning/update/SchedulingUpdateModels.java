package com.orchedule.scheduling.planning.update;

import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.UUID;

public final class SchedulingUpdateModels {
    private SchedulingUpdateModels() {
    }

    public record SeasonRules(
        UUID seasonId,
        int defaultMinRestHours,
        int defaultMaxMatchesPerWeek
    ) {}

    public record TeamRule(
        UUID seasonId,
        UUID teamId,
        int minRestHours,
        int maxMatchesPerWeek
    ) {}

    public record UpdateSeasonRulesRequest(
        Integer defaultMinRestHours,
        Integer defaultMaxMatchesPerWeek
    ) {}

    public record UpdateTeamRuleRequest(
        Integer minRestHours,
        Integer maxMatchesPerWeek
    ) {}

    public record GameConfirmation(
        UUID gameId,
        String confirmationStatus,
        OffsetDateTime confirmedAt,
        String confirmedBy
    ) {}

    public record Alternative(
        UUID id,
        UUID gameId,
        int alternativeWeek,
        String proposedDay,
        LocalTime proposedTime,
        String status,
        String reason,
        String createdBy,
        OffsetDateTime createdAt,
        String decidedBy,
        OffsetDateTime decidedAt
    ) {}

    public record CreateAlternativeRequest(
        int alternativeWeek,
        String proposedDay,
        LocalTime proposedTime,
        String reason
    ) {}

    public record HistoryEntry(
        UUID id,
        UUID seasonId,
        UUID gameId,
        String action,
        String operation,
        String result,
        String actor,
        OffsetDateTime createdAt,
        String detail
    ) {}
}
