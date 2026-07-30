package com.orchedule.field.api.dto;

import com.orchedule.field.domain.Field;

import java.time.OffsetDateTime;
import java.util.UUID;

public record FieldResponse(
        UUID id,
        UUID venueId,
        String name,
        String status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static FieldResponse from(Field field) {
        return new FieldResponse(
                field.getId(), field.getVenueId(), field.getName(),
                field.getStatus().name(), field.getCreatedAt(), field.getUpdatedAt());
    }
}
