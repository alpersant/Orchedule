package com.orchedule.team.application.exception;

import com.orchedule.shared.exception.NotFoundException;

/**
 * Matches the real constructor contract used by TeamRepositoryAdapter:
 * TeamNotFoundException(String message) — not a UUID-only overload.
 */
public class TeamNotFoundException extends NotFoundException {
    public TeamNotFoundException(String message) {
        super("TEAM_NOT_FOUND", message);
    }
}
