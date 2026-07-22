package com.orchedule.match.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CreateMatchRequest(
        @NotNull UUID seasonId,
        @NotNull UUID homeTeamId,
        @NotNull UUID awayTeamId,
        @NotNull UUID venueId,
        @NotNull OffsetDateTime scheduledAt,
        @NotNull String status,
        Integer round,
        @Size(max = 500) String notes
) {}
