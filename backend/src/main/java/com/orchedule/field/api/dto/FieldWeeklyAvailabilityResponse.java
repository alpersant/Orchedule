package com.orchedule.field.api.dto;

import com.orchedule.field.domain.FieldWeeklyAvailability;

import java.util.UUID;

public record FieldWeeklyAvailabilityResponse(
        UUID id,
        UUID seasonId,
        int weekNumber,
        UUID fieldId,
        boolean enabled
) {
    public static FieldWeeklyAvailabilityResponse from(FieldWeeklyAvailability availability) {
        return new FieldWeeklyAvailabilityResponse(
                availability.getId(), availability.getSeasonId(), availability.getWeekNumber(),
                availability.getFieldId(), availability.isEnabled());
    }
}
