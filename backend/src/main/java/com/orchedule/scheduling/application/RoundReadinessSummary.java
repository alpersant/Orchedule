package com.orchedule.scheduling.application;

public record RoundReadinessSummary(
        int eligibleTeams,
        int availableFields,
        int compatibleSlots,
        int estimatedCapacity,
        String existingRoundStatus
) {
    public RoundReadinessSummary {
        eligibleTeams = Math.max(0, eligibleTeams);
        availableFields = Math.max(0, availableFields);
        compatibleSlots = Math.max(0, compatibleSlots);
        estimatedCapacity = Math.max(0, estimatedCapacity);
        existingRoundStatus = existingRoundStatus == null || existingRoundStatus.isBlank() ? null : existingRoundStatus;
    }
}
