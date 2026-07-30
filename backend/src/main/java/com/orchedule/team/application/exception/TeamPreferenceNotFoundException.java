package com.orchedule.team.application.exception;

import com.orchedule.shared.exception.NotFoundException;

import java.util.UUID;

public class TeamPreferenceNotFoundException extends NotFoundException {
    public TeamPreferenceNotFoundException(UUID teamId, UUID competitionId) {
        super("TEAM_PREFERENCES_NOT_FOUND",
                "No preferences found for team " + teamId + " in competition " + competitionId);
    }
}
