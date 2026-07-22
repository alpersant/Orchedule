package com.orchedule.availability.domain;

import com.orchedule.availability.application.exception.InvalidAvailabilityException;

import java.time.OffsetDateTime;
import java.util.UUID;

public final class AvailabilityValidator {
    private AvailabilityValidator() {}

    public static void validate(AvailabilityScope scope, UUID referenceId, OffsetDateTime startAt, OffsetDateTime endAt, AvailabilityStatus status, String reason) {
        if (scope == null) throw new InvalidAvailabilityException("Availability scope is required");
        if (referenceId == null) throw new InvalidAvailabilityException("Reference id is required");
        if (startAt == null) throw new InvalidAvailabilityException("Start date is required");
        if (endAt == null) throw new InvalidAvailabilityException("End date is required");
        if (!endAt.isAfter(startAt)) throw new InvalidAvailabilityException("End date must be after start date");
        if (status == null) throw new InvalidAvailabilityException("Availability status is required");
        if (reason != null && reason.trim().length() > 255) throw new InvalidAvailabilityException("Reason must not exceed 255 characters");
    }
}
