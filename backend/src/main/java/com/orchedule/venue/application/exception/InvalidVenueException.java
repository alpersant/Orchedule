package com.orchedule.venue.application.exception;

import com.orchedule.shared.exception.BusinessRuleException;

public class InvalidVenueException extends BusinessRuleException {

    public InvalidVenueException(String message) {
        super("INVALID_VENUE", message);
    }
}
