package com.orchedule.shared.exception;

import org.springframework.http.HttpStatus;

public abstract class ValidationException extends ApplicationException {

    protected ValidationException(String errorCode, String message) {
        super(HttpStatus.BAD_REQUEST, errorCode, message);
    }
}
