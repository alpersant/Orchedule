package com.orchedule.team.api.dto;

import com.orchedule.team.domain.MatchDay;
import com.orchedule.team.domain.MatchHour;
import com.orchedule.team.domain.RestrictionType;

import java.util.List;
import java.util.UUID;

public record TeamPreferenceResponse(
        UUID teamId,
        RestrictionType restrictionType,
        MatchDay excludedDay,
        MatchHour excludedHour,
        List<DayPreferenceDto> dayPreferences,
        List<TimePreferenceDto> timePreferences
) {}