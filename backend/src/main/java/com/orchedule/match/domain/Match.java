package com.orchedule.match.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

public record Match(
        UUID id,
        UUID seasonId,
        UUID homeTeamId,
        UUID awayTeamId,
        UUID venueId,
        OffsetDateTime scheduledAt,
        MatchStatus status,
        Integer round,
        String notes,
        boolean active,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
