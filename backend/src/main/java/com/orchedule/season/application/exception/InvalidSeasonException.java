package com.orchedule.season.application.exception;

public class InvalidSeasonException extends RuntimeException {

    public InvalidSeasonException(String message) {
        super(message);
    }
}