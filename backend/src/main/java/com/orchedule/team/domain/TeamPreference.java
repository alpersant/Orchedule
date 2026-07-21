package com.orchedule.team.domain;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record TeamPreference(
        UUID teamId,
        RestrictionType restrictionType,
        MatchDay excludedDay,
        MatchHour excludedHour,
        List<DayPreference> dayPreferences,
        List<TimePreference> timePreferences,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}