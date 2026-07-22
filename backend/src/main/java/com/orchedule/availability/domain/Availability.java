package com.orchedule.availability.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

public record Availability(
        UUID id,
        AvailabilityScope scope,
        UUID referenceId,
        OffsetDateTime startAt,
        OffsetDateTime endAt,
        AvailabilityStatus status,
        String reason,
        boolean active,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
