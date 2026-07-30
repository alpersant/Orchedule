package com.orchedule.team.domain;

import java.time.LocalTime;

/** Row in team_preference_hour: (team_preference_id, hour) + priority. */
public record TeamPreferenceHour(LocalTime hour, PreferencePriority priority) {

    public TeamPreferenceHour {
        if (hour == null) throw new IllegalArgumentException("hour must not be null");
        if (priority == null) throw new IllegalArgumentException("priority must not be null");
    }
}
