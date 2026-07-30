package com.orchedule.scheduling.domain;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

public record ScheduleSlot(DayOfWeek day, LocalTime hour, UUID fieldId) {

    public ScheduleSlot {
        if (day == null || hour == null || fieldId == null) {
            throw new IllegalArgumentException("day, hour and fieldId must not be null");
        }
    }
}
