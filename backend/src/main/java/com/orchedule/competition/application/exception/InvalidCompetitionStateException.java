package com.orchedule.competition.application.exception;

import com.orchedule.shared.exception.BusinessRuleException;

public class InvalidCompetitionStateException extends BusinessRuleException {

    public InvalidCompetitionStateException(String message) {
        super("INVALID_COMPETITION_STATE", message);
    }
}
