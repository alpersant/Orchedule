package com.orchedule.venue.domain;

import com.orchedule.venue.application.exception.InvalidVenueException;

public final class VenueValidator {

    private VenueValidator() {
    }

    public static void validate(String name,
                                String city,
                                String address,
                                Integer capacity,
                                VenueStatus status) {

        if (name == null || name.isBlank()) {
            throw new InvalidVenueException("Venue name is required");
        }

        if (name.trim().length() > 120) {
            throw new InvalidVenueException("Venue name must not exceed 120 characters");
        }

        if (city == null || city.isBlank()) {
            throw new InvalidVenueException("Venue city is required");
        }

        if (city.trim().length() > 100) {
            throw new InvalidVenueException("Venue city must not exceed 100 characters");
        }

        if (address != null && address.trim().length() > 255) {
            throw new InvalidVenueException("Venue address must not exceed 255 characters");
        }

        if (capacity != null && capacity < 0) {
            throw new InvalidVenueException("Venue capacity must be greater than or equal to 0");
        }

        if (status == null) {
            throw new InvalidVenueException("Venue status is required");
        }
    }
}
