package com.orchedule.field.api.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record SetWeeklyAvailabilityRequest(
        @NotNull UUID seasonId,
        int weekNumber,
        @NotEmpty List<UUID> enabledFieldIds
) {}
