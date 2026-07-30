package com.orchedule.team.api.dto;

import com.orchedule.team.domain.MatchDay;
import com.orchedule.team.domain.MatchHour;
import com.orchedule.team.domain.RestrictionType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record SaveTeamPreferenceRequest(
        @NotNull UUID competitionId,
        @NotNull RestrictionType restrictionType,
        MatchDay excludedDay,
        MatchHour excludedHour,
        @NotEmpty List<DayPreferenceDto> dayPreferences,
        @NotEmpty List<TimePreferenceDto> timePreferences
) {}
