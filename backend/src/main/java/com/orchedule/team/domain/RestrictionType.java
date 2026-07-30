package com.orchedule.team.domain;

/**
 * Mirrors team_preference.restriction_type in V4__team_and_preferences.
 * A team excludes exactly one option — a day OR an hour, never both,
 * enforced both here and at the DB level via chk_team_preference_exclusion.
 */
public enum RestrictionType {
    EXCLUDED_DAY,
    EXCLUDED_HOUR
}
