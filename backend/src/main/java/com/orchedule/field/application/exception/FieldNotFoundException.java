package com.orchedule.field.application.exception;

import com.orchedule.shared.exception.NotFoundException;

import java.util.UUID;

public class FieldNotFoundException extends NotFoundException {
    public FieldNotFoundException(UUID fieldId) { super("FIELD_NOT_FOUND", "Field not found: " + fieldId); }
}
