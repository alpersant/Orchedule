package com.orchedule.scheduling.api.dto;

import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;

public record DayOptionDto(
        @NotNull DayOfWeek day,
        @NotNull String priority
) {}
