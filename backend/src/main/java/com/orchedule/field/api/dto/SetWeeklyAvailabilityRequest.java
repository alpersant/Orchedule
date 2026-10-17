package com.orchedule.field.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

/** An empty list explicitly means that no field is enabled for the week. */
public record SetWeeklyAvailabilityRequest(
        @NotNull UUID seasonId,
        @NotNull @Min(1) @Max(60) Integer weekNumber,
        @NotNull @Size(max = 500) List<@NotNull UUID> enabledFieldIds
) {}
