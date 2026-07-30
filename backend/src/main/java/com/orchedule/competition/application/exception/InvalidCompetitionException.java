package com.orchedule.competition.application.exception;

import com.orchedule.shared.exception.ValidationException;

public class InvalidCompetitionException extends ValidationException {

    public InvalidCompetitionException(String message) {
        super("INVALID_COMPETITION", message);
    }
}
