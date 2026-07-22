package com.orchedule.venue.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record CreateVenueRequest(

        @NotBlank(message = "Venue name is required")
        @Size(max = 120, message = "Venue name must not exceed 120 characters")
        String name,

        @NotBlank(message = "Venue city is required")
        @Size(max = 100, message = "Venue city must not exceed 100 characters")
        String city,

        @Size(max = 255, message = "Venue address must not exceed 255 characters")
        String address,

        @PositiveOrZero(message = "Venue capacity must be greater than or equal to 0")
        Integer capacity
) {
}
