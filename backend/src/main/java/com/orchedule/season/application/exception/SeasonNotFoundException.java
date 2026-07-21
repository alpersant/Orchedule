package com.orchedule.season.application.exception;

import java.util.UUID;

public class SeasonNotFoundException extends RuntimeException {

    public SeasonNotFoundException(UUID seasonId) {
        super("Season not found: " + seasonId);
    }
}