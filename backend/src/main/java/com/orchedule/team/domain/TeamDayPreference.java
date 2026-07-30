package com.orchedule.team.domain;

import java.time.DayOfWeek;

/**
 * A single day preference entry for a team. `primary` maps to the
 * scheduling module's SlotPriority.FIRST when true, SECOND when false.
 */
public record TeamDayPreference(DayOfWeek day, boolean primary) {

    public TeamDayPreference {
        if (day == null) {
            throw new IllegalArgumentException("day must not be null");
        }
    }
}
