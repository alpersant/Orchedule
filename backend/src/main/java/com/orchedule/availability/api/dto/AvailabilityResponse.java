package com.orchedule.availability.api.dto;

import com.orchedule.availability.domain.AvailabilityScope;
import com.orchedule.availability.domain.AvailabilityStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AvailabilityResponse(
        UUID id,
        AvailabilityScope scope,
        UUID referenceId,
        OffsetDateTime startAt,
        OffsetDateTime endAt,
        AvailabilityStatus status,
        String reason,
        boolean active
) {}
