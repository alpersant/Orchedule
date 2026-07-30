package com.orchedule.scheduling.application.exception;

import com.orchedule.shared.exception.ValidationException;

public class InvalidScheduleException extends ValidationException {

    public InvalidScheduleException(String message) {
        super("INVALID_SCHEDULE", message);
    }
}
