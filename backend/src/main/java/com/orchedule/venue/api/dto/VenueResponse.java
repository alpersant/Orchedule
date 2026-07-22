package com.orchedule.venue.api.dto;

import com.orchedule.venue.domain.VenueStatus;

import java.util.UUID;

public record VenueResponse(
        UUID id,
        String name,
        String city,
        String address,
        Integer capacity,
        VenueStatus status,
        boolean active
) {
}
