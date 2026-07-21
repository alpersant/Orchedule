package com.orchedule.team.api.dto;

import com.orchedule.team.domain.MatchHour;
import com.orchedule.team.domain.PreferencePriority;
import jakarta.validation.constraints.NotNull;

public record TimePreferenceDto(
        @NotNull MatchHour hour,
        @NotNull PreferencePriority priority
) {}