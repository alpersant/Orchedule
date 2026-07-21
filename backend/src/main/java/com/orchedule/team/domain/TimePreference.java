package com.orchedule.team.domain;

public record TimePreference(
        MatchHour hour,
        PreferencePriority priority
) {}