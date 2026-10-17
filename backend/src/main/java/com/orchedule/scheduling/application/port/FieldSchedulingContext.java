package com.orchedule.scheduling.application.port;

import java.util.List;

/**
 * Field availability for one season week. Slots preserve the relationship
 * between a field and its own opening day/hour; they are never combined
 * into independent day and hour sets.
 */
public record FieldSchedulingContext(List<FieldScheduleSlot> slots) {
    public FieldSchedulingContext {
        slots = List.copyOf(slots);
    }
}
