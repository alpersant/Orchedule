package com.orchedule.field.application.exception;

import com.orchedule.shared.exception.ValidationException;

public class InvalidFieldException extends ValidationException {
    public InvalidFieldException(String message) { super("INVALID_FIELD", message); }
}
