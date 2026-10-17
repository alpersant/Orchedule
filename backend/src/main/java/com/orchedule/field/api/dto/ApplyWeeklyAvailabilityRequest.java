package com.orchedule.field.api.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public record ApplyWeeklyAvailabilityRequest(
        @NotNull UUID seasonId,
        @NotEmpty @Size(max = 60) List<@NotNull @Min(1) @Max(60) Integer> weekNumbers,
        @NotNull @Size(max = 500) List<@NotNull UUID> enabledFieldIds
) {}
