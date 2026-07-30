package com.orchedule.scheduling.domain;

import com.orchedule.scheduling.application.exception.InvalidScheduleException;

import java.util.List;
import java.util.Set;

public final class ScheduleValidator {

    private static final int MIN_WEEK_NUMBER = 1;
    private static final int MAX_WEEK_NUMBER = 60;

    private ScheduleValidator() {}

    public static void validateWeekNumber(int weekNumber) {
        if (weekNumber < MIN_WEEK_NUMBER || weekNumber > MAX_WEEK_NUMBER) {
            throw new InvalidScheduleException(
                    "Week number must be between " + MIN_WEEK_NUMBER + " and " + MAX_WEEK_NUMBER);
        }
    }

    public static void validateTeamProfiles(List<TeamScheduleProfile> profiles) {
        if (profiles == null || profiles.isEmpty()) {
            throw new InvalidScheduleException("At least one team schedule profile is required to generate a round");
        }
    }

    public static void validateOpenDays(Set<?> openDays) {
        if (openDays == null || openDays.isEmpty()) {
            throw new InvalidScheduleException("At least one day must be open for this round");
        }
    }

    public static void validateOpenHours(Set<?> openHours) {
        if (openHours == null || openHours.isEmpty()) {
            throw new InvalidScheduleException("At least one hour must be open for this round");
        }
    }

    public static void validateFieldIds(List<?> fieldIds) {
        if (fieldIds == null || fieldIds.isEmpty()) {
            throw new InvalidScheduleException("At least one field must be available for this round");
        }
    }
}
