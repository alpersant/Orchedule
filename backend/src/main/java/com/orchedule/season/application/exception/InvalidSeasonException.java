package com.orchedule.season.application.exception;

import com.orchedule.shared.exception.BusinessRuleException;

public class InvalidSeasonException extends BusinessRuleException {

    public InvalidSeasonException(String message) {
        super("INVALID_SEASON", message);
    }
}
