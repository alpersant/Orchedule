package com.orchedule.competition.application.exception;

import com.orchedule.shared.exception.ConflictException;

public class CompetitionAlreadyExistsException extends ConflictException {

    public CompetitionAlreadyExistsException(String name) {
        super("COMPETITION_ALREADY_EXISTS", "A competition with name '" + name + "' already exists");
    }
}
