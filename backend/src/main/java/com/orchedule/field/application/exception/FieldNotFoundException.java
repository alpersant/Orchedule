package com.orchedule.field.application.exception;

import com.orchedule.shared.exception.NotFoundException;

public class FieldNotFoundException extends NotFoundException {
    public FieldNotFoundException(String message) {
        super(message);
    }
}
