package com.orchedule.shared.exception;

public class AlreadyExistsException extends ConflictException {

    public AlreadyExistsException(String entityName, String value) {
        super(entityName.toUpperCase() + "_ALREADY_EXISTS", entityName + " already exists: " + value);
    }
}
