package com.orchedule.match.application.exception;

import com.orchedule.shared.exception.BusinessRuleException;

public class InvalidMatchException extends BusinessRuleException {
    public InvalidMatchException(String message) { super("INVALID_MATCH", message); }
}
