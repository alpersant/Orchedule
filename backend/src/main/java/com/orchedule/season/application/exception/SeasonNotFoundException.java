package com.orchedule.season.application.exception;

import com.orchedule.shared.exception.NotFoundException;

import java.util.UUID;

public class SeasonNotFoundException extends NotFoundException {

    public SeasonNotFoundException(UUID seasonId) {
        super("SEASON_NOT_FOUND", "Season not found: " + seasonId);
    }
}
