package com.orchedule.team.domain;

/**
 * Mirrors the `priority` column in team_preference_day / team_preference_hour.
 * FIRST = primary/preferred slot, SECOND = fallback slot.
 */
public enum PreferencePriority {
    FIRST,
    SECOND
}
