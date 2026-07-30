package com.orchedule.scheduling.api.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public record GenerateRoundRequest(
        @NotNull UUID seasonId,
        int weekNumber,
        @NotEmpty List<TeamScheduleProfileDto> teamProfiles,
        @NotEmpty Set<DayOfWeek> openDays,
        @NotEmpty Set<LocalTime> openHours,
        @NotEmpty List<UUID> availableFieldIds
) {}
