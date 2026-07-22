package com.orchedule.shared.exception;

import org.springframework.http.HttpStatus;

public abstract class BusinessRuleException extends ApplicationException {

    protected BusinessRuleException(String errorCode, String message) {
        super(HttpStatus.BAD_REQUEST, errorCode, message);
    }
}
