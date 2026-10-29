package com.orchedule.scheduling.api.dto;

import com.orchedule.scheduling.application.RoundReadinessResult;

import java.util.List;
import java.util.UUID;

public record RoundReadinessResponse(
        UUID seasonId,
        int weekNumber,
        boolean ready,
        boolean canAttemptGeneration,
        RoundReadinessSummaryResponse summary,
        List<RoundReadinessIssueResponse> issues
) {
    public static RoundReadinessResponse from(RoundReadinessResult result) {
        return new RoundReadinessResponse(
                result.seasonId(),
                result.weekNumber(),
                result.readiness().ready(),
                result.readiness().canAttemptGeneration(),
                RoundReadinessSummaryResponse.from(result.summary()),
                result.readiness().issues().stream()
                        .map(RoundReadinessIssueResponse::from)
                        .toList()
        );
    }
}
