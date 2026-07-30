package com.orchedule.team.domain;

import com.orchedule.team.application.exception.InvalidTeamPreferenceException;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public final class TeamPreferenceValidator {

    private TeamPreferenceValidator() {
    }

    public static void validate(RestrictionType restrictionType,
                                MatchDay excludedDay,
                                MatchHour excludedHour,
                                List<DayPreference> dayPreferences,
                                List<TimePreference> timePreferences) {

        if (restrictionType == RestrictionType.EXCLUDED_DAY) {
            if (excludedDay == null || excludedHour != null) {
                throw new InvalidTeamPreferenceException("When restrictionType is EXCLUDED_DAY, excludedDay is required and excludedHour must be null");
            }
        }

        if (restrictionType == RestrictionType.EXCLUDED_HOUR) {
            if (excludedHour == null || excludedDay != null) {
                throw new InvalidTeamPreferenceException("When restrictionType is EXCLUDED_HOUR, excludedHour is required and excludedDay must be null");
            }
        }

        validateUniqueDays(dayPreferences);
        validateUniqueHours(timePreferences);
        validateMinimumPrimaryDays(dayPreferences);

        if (restrictionType == RestrictionType.EXCLUDED_DAY) {
            validateMinimumPrimaryHours(timePreferences);
        }
    }

    private static void validateUniqueDays(List<DayPreference> dayPreferences) {
        Set<MatchDay> unique = EnumSet.noneOf(MatchDay.class);
        for (DayPreference preference : dayPreferences) {
            if (!unique.add(preference.day())) {
                throw new InvalidTeamPreferenceException("Duplicated day preference: " + preference.day());
            }
        }
    }

    private static void validateUniqueHours(List<TimePreference> timePreferences) {
        Set<MatchHour> unique = EnumSet.noneOf(MatchHour.class);
        for (TimePreference preference : timePreferences) {
            if (!unique.add(preference.hour())) {
                throw new InvalidTeamPreferenceException("Duplicated hour preference: " + preference.hour());
            }
        }
    }

    private static void validateMinimumPrimaryDays(List<DayPreference> dayPreferences) {
        long primaryDays = dayPreferences.stream()
                .filter(p -> p.priority() == PreferencePriority.FIRST)
                .count();

        if (primaryDays < 3) {
            throw new InvalidTeamPreferenceException("At least 3 primary days are required");
        }
    }

    private static void validateMinimumPrimaryHours(List<TimePreference> timePreferences) {
        long primaryHours = timePreferences.stream()
                .filter(p -> p.priority() == PreferencePriority.FIRST)
                .count();

        if (primaryHours < 4) {
            throw new InvalidTeamPreferenceException("At least 4 primary hours are required when excluding a day");
        }
    }
}