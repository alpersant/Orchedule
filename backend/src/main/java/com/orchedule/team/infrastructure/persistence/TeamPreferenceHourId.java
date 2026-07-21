package com.orchedule.team.infrastructure.persistence;

import com.orchedule.team.domain.MatchHour;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class TeamPreferenceHourId implements Serializable {

    private UUID teamPreference;
    private MatchHour hour;

    public TeamPreferenceHourId() {
    }

    public TeamPreferenceHourId(UUID teamPreference, MatchHour hour) {
        this.teamPreference = teamPreference;
        this.hour = hour;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TeamPreferenceHourId that)) return false;
        return Objects.equals(teamPreference, that.teamPreference) && hour == that.hour;
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamPreference, hour);
    }
}