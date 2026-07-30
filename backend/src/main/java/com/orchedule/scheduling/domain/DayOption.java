package com.orchedule.scheduling.domain;

import java.time.DayOfWeek;

/**
 * A day of the week available to a team, tagged with its priority.
 * Lightweight value object owned by the scheduling module — it does NOT
 * reach into the team module's domain types, keeping module boundaries
 * clean for Spring Modulith. The orchestrating application layer is
 * responsible for translating team preferences into this shape.
 */
public record DayOption(DayOfWeek day, SlotPriority priority) {

    public DayOption {
        if (day == null) {
            throw new IllegalArgumentException("day must not be null");
        }
        if (priority == null) {
            throw new IllegalArgumentException("priority must not be null");
        }
    }
}
