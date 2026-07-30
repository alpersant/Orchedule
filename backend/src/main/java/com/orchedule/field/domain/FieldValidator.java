package com.orchedule.field.domain;

import com.orchedule.field.application.exception.InvalidFieldException;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;

public final class FieldValidator {

    private FieldValidator() {}

    public static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidFieldException("Field name must not be blank");
        }
        if (name.length() > 100) {
            throw new InvalidFieldException("Field name must not exceed 100 characters");
        }
    }

    public static void validateWeekNumber(int weekNumber) {
        if (weekNumber < 1 || weekNumber > 60) {
            throw new InvalidFieldException("Week number must be between 1 and 60");
        }
    }

    public static void validateOpeningHours(Set<DayOfWeek> openDays, Set<LocalTime> openHours) {
        if (openDays == null || openDays.isEmpty()) {
            throw new InvalidFieldException("A field must have at least one open day");
        }
        if (openHours == null || openHours.isEmpty()) {
            throw new InvalidFieldException("A field must have at least one open hour");
        }
    }
}
