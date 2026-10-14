package com.orchedule.competition.api.dto;

import java.util.List;
import java.util.UUID;

public record BatchDeleteCompetitionsResponse(
        List<UUID> deletedIds,
        List<BatchDeleteFailure> failures
) {
    public record BatchDeleteFailure(UUID competitionId, String reason) {}
}
