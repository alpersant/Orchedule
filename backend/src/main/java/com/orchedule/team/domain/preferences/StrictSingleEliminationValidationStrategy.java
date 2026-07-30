package com.orchedule.team.domain.preferences;

import com.orchedule.team.application.exception.InvalidTeamPreferenceException;
import com.orchedule.team.domain.PreferencePriority;
import com.orchedule.team.domain.RestrictionType;

/**
 * Opt-in strict policy matching the original functional requirement,
 * expressed against the REAL V4 schema shape (restrictionType +
 * excludedDay/excludedHour + day/hour priority lists) rather than against
 * a hardcoded "10 candidate options" universe:
 *
 *   - EXCLUDED_HOUR: remaining hours must ALL be FIRST; days need >= 3 FIRST
 *   - EXCLUDED_DAY:  days need >= 3 FIRST; hours need >= 4 FIRST and exactly 1 SECOND
 *
 * Only applied when the team's competition explicitly opts into
 * PreferencePolicy.STRICT_SINGLE_ELIMINATION.
 */
public class StrictSingleEliminationValidationStrategy implements TeamPreferenceValidationStrategy {

    private static final int MIN_FIRST_DAYS = 3;
    private static final int MIN_FIRST_HOURS_WHEN_DAY_EXCLUDED = 4;

    @Override
    public void validate(TeamPreference preference) {
        long firstDays = countByPriority(preference, PreferencePriority.FIRST, true);
        long firstHours = countByPriority(preference, PreferencePriority.FIRST, false);
        int totalDays = preference.getDayPreferences().size();
        int totalHours = preference.getHourPreferences().size();

        if (preference.getRestrictionType() == RestrictionType.EXCLUDED_HOUR) {
            if (firstHours != totalHours) {
                throw new InvalidTeamPreferenceException(
                        "When excluding an hour, all " + totalHours + " remaining hours must be FIRST priority");
            }
            if (firstDays < MIN_FIRST_DAYS) {
                throw new InvalidTeamPreferenceException(
                        "When excluding an hour, at least " + MIN_FIRST_DAYS + " days must be FIRST priority");
            }
        } else if (preference.getRestrictionType() == RestrictionType.EXCLUDED_DAY) {
            if (firstDays < MIN_FIRST_DAYS) {
                throw new InvalidTeamPreferenceException(
                        "When excluding a day, at least " + MIN_FIRST_DAYS + " days must be FIRST priority");
            }
            if (firstHours < MIN_FIRST_HOURS_WHEN_DAY_EXCLUDED) {
                throw new InvalidTeamPreferenceException(
                        "When excluding a day, at least " + MIN_FIRST_HOURS_WHEN_DAY_EXCLUDED
                                + " hours must be FIRST priority (1 SECOND hour required)");
            }
            long secondHours = totalHours - firstHours;
            if (secondHours < 1) {
                throw new InvalidTeamPreferenceException(
                        "When excluding a day, exactly 1 hour must be SECOND priority");
            }
        } else {
            throw new InvalidTeamPreferenceException("Unknown restriction type: " + preference.getRestrictionType());
        }
    }

    private long countByPriority(TeamPreference preference, PreferencePriority priority, boolean days) {
        return days
                ? preference.getDayPreferences().stream().filter(d -> d.priority() == priority).count()
                : preference.getHourPreferences().stream().filter(h -> h.priority() == priority).count();
    }
}
