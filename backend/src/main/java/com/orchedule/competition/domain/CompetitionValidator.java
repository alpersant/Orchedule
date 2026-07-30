package com.orchedule.competition.domain;

import com.orchedule.competition.application.exception.InvalidCompetitionException;

import java.util.Set;

public final class CompetitionValidator {

    private static final int MAX_NAME_LENGTH = 150;
    private static final int MAX_DESCRIPTION_LENGTH = 1000;
    private static final int MIN_FIELD_COUNT = 1;
    private static final int MAX_FIELD_COUNT = 20;

    private CompetitionValidator() {}

    public static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidCompetitionException("Competition name must not be blank");
        }
        if (name.length() > MAX_NAME_LENGTH) {
            throw new InvalidCompetitionException(
                    "Competition name must not exceed " + MAX_NAME_LENGTH + " characters");
        }
    }

    public static void validateDescription(String description) {
        if (description != null && description.length() > MAX_DESCRIPTION_LENGTH) {
            throw new InvalidCompetitionException(
                    "Competition description must not exceed " + MAX_DESCRIPTION_LENGTH + " characters");
        }
    }

    public static void validateDefaultDays(Set<CompetitionDay> days) {
        if (days == null || days.isEmpty()) {
            throw new InvalidCompetitionException("At least one default day must be selected");
        }
    }

    public static void validateDefaultHours(Set<CompetitionHour> hours) {
        if (hours == null || hours.isEmpty()) {
            throw new InvalidCompetitionException("At least one default hour must be selected");
        }
    }

    public static void validateDefaultFieldCount(int fieldCount) {
        if (fieldCount < MIN_FIELD_COUNT || fieldCount > MAX_FIELD_COUNT) {
            throw new InvalidCompetitionException(
                    "Default field count must be between " + MIN_FIELD_COUNT + " and " + MAX_FIELD_COUNT);
        }
    }
}
