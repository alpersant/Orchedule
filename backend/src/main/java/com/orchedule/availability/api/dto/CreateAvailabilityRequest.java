package com.orchedule.availability.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CreateAvailabilityRequest(
        @NotBlank String scope,
        @NotNull UUID referenceId,
        @NotNull OffsetDateTime startAt,
        @NotNull OffsetDateTime endAt,
        @NotBlank String status,
        @Size(max = 255) String reason
) {}
