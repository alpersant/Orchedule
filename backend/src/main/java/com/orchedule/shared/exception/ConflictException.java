package com.orchedule.shared.exception;

import org.springframework.http.HttpStatus;

public abstract class ConflictException extends ApplicationException {

    protected ConflictException(String errorCode, String message) {
        super(HttpStatus.CONFLICT, errorCode, message);
    }
}
