package com.orchedule.team.application.exception;

import com.orchedule.shared.exception.ValidationException;

public class InvalidTeamPreferenceException extends ValidationException {
    public InvalidTeamPreferenceException(String message) {
        super("INVALID_TEAM_PREFERENCE", message);
    }
}
