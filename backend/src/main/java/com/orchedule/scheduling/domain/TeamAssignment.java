package com.orchedule.scheduling.domain;

import java.util.UUID;

/**
 * A single team's assigned slot within a ScheduleRound.
 */
public record TeamAssignment(UUID teamId, ScheduleSlot slot) {

    public TeamAssignment {
        if (teamId == null || slot == null) {
            throw new IllegalArgumentException("teamId and slot must not be null");
        }
    }
}
