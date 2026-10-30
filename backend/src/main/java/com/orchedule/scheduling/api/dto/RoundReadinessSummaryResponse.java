package com.orchedule.scheduling.api.dto;

import com.orchedule.scheduling.application.RoundReadinessSummary;

public record RoundReadinessSummaryResponse(
        int eligibleTeams,
        int availableFields,
        int compatibleSlots,
        int estimatedCapacity,
        String existingRoundStatus
) {
    public static RoundReadinessSummaryResponse from(RoundReadinessSummary summary) {
        return new RoundReadinessSummaryResponse(
                summary.eligibleTeams(),
                summary.availableFields(),
                summary.compatibleSlots(),
                summary.estimatedCapacity(),
                summary.existingRoundStatus()
        );
    }
}
