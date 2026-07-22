package com.orchedule.shared.exception;

import org.springframework.http.HttpStatus;

public abstract class NotFoundException extends ApplicationException {

    protected NotFoundException(String errorCode, String message) {
        super(HttpStatus.NOT_FOUND, errorCode, message);
    }
}
