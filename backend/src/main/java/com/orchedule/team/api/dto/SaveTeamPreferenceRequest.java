package com.orchedule.team.api.dto;

import com.orchedule.team.domain.MatchDay;
import com.orchedule.team.domain.MatchHour;
import com.orchedule.team.domain.RestrictionType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record SaveTeamPreferenceRequest(
        @NotNull RestrictionType restrictionType,
        MatchDay excludedDay,
        MatchHour excludedHour,
        @NotEmpty List<DayPreferenceDto> dayPreferences,
        @NotEmpty List<TimePreferenceDto> timePreferences
) {}