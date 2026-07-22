package com.orchedule.match.api.dto;

import com.orchedule.match.domain.MatchStatus;
import java.time.OffsetDateTime;
import java.util.UUID;

public record MatchResponse(
        UUID id,
        UUID seasonId,
        UUID homeTeamId,
        UUID awayTeamId,
        UUID venueId,
        OffsetDateTime scheduledAt,
        MatchStatus status,
        Integer round,
        String notes,
        boolean active
) {}
