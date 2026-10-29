package com.orchedule.scheduling.application;

import com.orchedule.scheduling.domain.RoundReadiness;

import java.util.UUID;

public record RoundReadinessResult(
        UUID seasonId,
        int weekNumber,
        RoundReadiness readiness,
        RoundReadinessSummary summary
) {
    public RoundReadinessResult {
        if (seasonId == null) throw new IllegalArgumentException("Season id is required");
        if (readiness == null) throw new IllegalArgumentException("Readiness is required");
        if (summary == null) throw new IllegalArgumentException("Summary is required");
    }
}
