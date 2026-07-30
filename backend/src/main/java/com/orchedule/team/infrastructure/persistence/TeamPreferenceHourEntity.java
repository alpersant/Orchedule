package com.orchedule.team.infrastructure.persistence;

import com.orchedule.team.domain.PreferencePriority;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalTime;
import java.util.Objects;
import java.util.UUID;

/** Maps to team_preference_hour: composite PK (team_preference_id, hour). */
@Entity
@Table(name = "team_preference_hour")
@IdClass(TeamPreferenceHourEntity.Key.class)
public class TeamPreferenceHourEntity {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_preference_id", nullable = false)
    private TeamPreferenceEntity teamPreference;

    @Id
    @Column(name = "hour", nullable = false, length = 20)
    private String hour;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    private PreferencePriority priority;

    protected TeamPreferenceHourEntity() {}

    public TeamPreferenceHourEntity(LocalTime hour, PreferencePriority priority) {
        this.hour = hour.toString();
        this.priority = priority;
    }

    public void setTeamPreference(TeamPreferenceEntity teamPreference) { this.teamPreference = teamPreference; }
    public LocalTime getHour() { return LocalTime.parse(hour); }
    public PreferencePriority getPriority() { return priority; }

    public static class Key implements Serializable {
        private UUID teamPreference;
        private String hour;

        public Key() {}

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Key key)) return false;
            return Objects.equals(teamPreference, key.teamPreference) && Objects.equals(hour, key.hour);
        }

        @Override
        public int hashCode() { return Objects.hash(teamPreference, hour); }
    }
}
