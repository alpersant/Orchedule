package com.orchedule.competition.application.exception;

import com.orchedule.shared.exception.NotFoundException;

import java.util.UUID;

public class CompetitionNotFoundException extends NotFoundException {

    public CompetitionNotFoundException(UUID competitionId) {
        super("COMPETITION_NOT_FOUND", "Competition not found: " + competitionId);
    }
}
