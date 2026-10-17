package com.orchedule.field.application.exception;

import com.orchedule.shared.exception.ConflictException;

public class WeeklyAvailabilityConflictException extends ConflictException {
    public WeeklyAvailabilityConflictException(String message) {
        super("WEEKLY_AVAILABILITY_CONFLICT", message);
    }
}
