package com.orchedule.venue.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

public record Venue(
        UUID id,
        String name,
        String city,
        String address,
        Integer capacity,
        VenueStatus status,
        boolean active,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
