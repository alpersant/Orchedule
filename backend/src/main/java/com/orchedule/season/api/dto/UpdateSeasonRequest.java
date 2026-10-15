package com.orchedule.season.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateSeasonRequest(

        @NotBlank(message = "Season name is required")
        @Size(max = 120, message = "Season name must not exceed 120 characters")
        String name,

        @NotNull(message = "Season startDate is required")
        LocalDate startDate,

        @NotNull(message = "Season endDate is required")
        LocalDate endDate,

        @NotNull(message = "Season active flag is required")
        Boolean active
) {
}
