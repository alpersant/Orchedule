package com.orchedule.team.infrastructure.persistence;

import com.orchedule.team.domain.MatchDay;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class TeamPreferenceDayId implements Serializable {

    private UUID teamPreference;
    private MatchDay day;

    public TeamPreferenceDayId() {
    }

    public TeamPreferenceDayId(UUID teamPreference, MatchDay day) {
        this.teamPreference = teamPreference;
        this.day = day;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TeamPreferenceDayId that)) return false;
        return Objects.equals(teamPreference, that.teamPreference) && day == that.day;
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamPreference, day);
    }
}