package com.orchedule.availability.application.exception;

import com.orchedule.shared.exception.BusinessRuleException;

public class InvalidAvailabilityException extends BusinessRuleException {
    public InvalidAvailabilityException(String message) { super("INVALID_AVAILABILITY", message); }
}
