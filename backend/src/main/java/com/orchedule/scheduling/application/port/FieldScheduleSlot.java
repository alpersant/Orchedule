package com.orchedule.scheduling.application.port;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

/** A concrete opening slot owned by one field. */
public record FieldScheduleSlot(UUID fieldId, DayOfWeek day, LocalTime hour) {
    public FieldScheduleSlot {
        if (fieldId == null || day == null || hour == null) {
            throw new IllegalArgumentException("fieldId, day and hour are required");
        }
    }
}
