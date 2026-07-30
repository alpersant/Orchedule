package com.orchedule.scheduling.api.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record HourOptionDto(
        @NotNull LocalTime hour,
        @NotNull String priority
) {}
