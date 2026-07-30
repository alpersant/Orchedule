package com.orchedule.team.domain;

import java.time.DayOfWeek;

/** Row in team_preference_day: (team_preference_id, day) + priority. */
public record TeamPreferenceDay(DayOfWeek day, PreferencePriority priority) {

    public TeamPreferenceDay {
        if (day == null) throw new IllegalArgumentException("day must not be null");
        if (priority == null) throw new IllegalArgumentException("priority must not be null");
    }
}
