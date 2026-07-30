package com.orchedule.team.infrastructure.persistence;

import com.orchedule.team.domain.PreferencePriority;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.util.Objects;
import java.util.UUID;

/**
 * Maps to team_preference_day: composite PK (team_preference_id, day).
 * Uses an embeddable composite key to mirror the real PRIMARY KEY exactly
 * instead of introducing a surrogate id column that doesn't exist in V4.
 */
@Entity
@Table(name = "team_preference_day")
@IdClass(TeamPreferenceDayEntity.Key.class)
public class TeamPreferenceDayEntity {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_preference_id", nullable = false)
    private TeamPreferenceEntity teamPreference;

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "day", nullable = false, length = 20)
    private DayOfWeek day;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    private PreferencePriority priority;

    protected TeamPreferenceDayEntity() {}

    public TeamPreferenceDayEntity(DayOfWeek day, PreferencePriority priority) {
        this.day = day;
        this.priority = priority;
    }

    public void setTeamPreference(TeamPreferenceEntity teamPreference) { this.teamPreference = teamPreference; }
    public DayOfWeek getDay() { return day; }
    public PreferencePriority getPriority() { return priority; }

    public static class Key implements Serializable {
        private UUID teamPreference;
        private DayOfWeek day;

        public Key() {}

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Key key)) return false;
            return Objects.equals(teamPreference, key.teamPreference) && Objects.equals(day, key.day);
        }

        @Override
        public int hashCode() { return Objects.hash(teamPreference, day); }
    }
}
