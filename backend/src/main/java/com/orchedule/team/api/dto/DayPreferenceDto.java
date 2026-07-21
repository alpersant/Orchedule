package com.orchedule.team.api.dto;

import com.orchedule.team.domain.MatchDay;
import com.orchedule.team.domain.PreferencePriority;
import jakarta.validation.constraints.NotNull;

public record DayPreferenceDto(
        @NotNull MatchDay day,
        @NotNull PreferencePriority priority
) {}