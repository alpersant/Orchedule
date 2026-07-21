package com.orchedule.team.application.exception;

public class InvalidTeamPreferenceException extends RuntimeException {
    public InvalidTeamPreferenceException(String message) {
        super(message);
    }
}