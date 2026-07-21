package com.orchedule.season.api.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateSeasonRequest(

        @NotBlank(message = "Season name is required")
        @Size(max = 120, message = "Season name must not exceed 120 characters")
        String name,

        @NotNull(message = "Season startDate is required")
        @FutureOrPresent(message = "Season startDate must be today or in the future")
        LocalDate startDate,

        @NotNull(message = "Season endDate is required")
        @FutureOrPresent(message = "Season endDate must be today or in the future")
        LocalDate endDate
) {
}