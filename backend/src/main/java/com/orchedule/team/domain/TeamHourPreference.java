package com.orchedule.team.domain;

import java.time.LocalTime;

public record TeamHourPreference(LocalTime hour, boolean primary) {

    public TeamHourPreference {
        if (hour == null) {
            throw new IllegalArgumentException("hour must not be null");
        }
    }
}
