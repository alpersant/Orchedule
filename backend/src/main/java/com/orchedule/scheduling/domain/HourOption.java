package com.orchedule.scheduling.domain;

import java.time.LocalTime;

public record HourOption(LocalTime hour, SlotPriority priority) {

    public HourOption {
        if (hour == null) {
            throw new IllegalArgumentException("hour must not be null");
        }
        if (priority == null) {
            throw new IllegalArgumentException("priority must not be null");
        }
    }
}
