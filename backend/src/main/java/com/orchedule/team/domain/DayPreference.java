package com.orchedule.team.domain;

public record DayPreference(
        MatchDay day,
        PreferencePriority priority
) {}