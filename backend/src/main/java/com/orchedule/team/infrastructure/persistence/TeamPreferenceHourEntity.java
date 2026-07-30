package com.orchedule.team.infrastructure.persistence;

import com.orchedule.team.domain.MatchHour;
import com.orchedule.team.domain.PreferencePriority;
import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;


@Entity
@Table(name = "team_preference_hour")
@IdClass(TeamPreferenceHourEntity.Key.class)
public class TeamPreferenceHourEntity {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "team_id", referencedColumnName = "team_id", nullable = false),
            @JoinColumn(name = "competition_id", referencedColumnName = "competition_id", nullable = false)
    })
    private TeamPreferenceEntity teamPreference;

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "hour", nullable = false, length = 20)
    private MatchHour hour;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    private PreferencePriority priority;

    protected TeamPreferenceHourEntity() {}

    public TeamPreferenceHourEntity(MatchHour hour, PreferencePriority priority) {
        this.hour = hour;
        this.priority = priority;
    }

    public void setTeamPreference(TeamPreferenceEntity teamPreference) { this.teamPreference = teamPreference; }
    public MatchHour getHour() { return hour; }
    public PreferencePriority getPriority() { return priority; }

    public static class Key implements Serializable {
        private TeamPreferenceEntity.Key teamPreference;
        private MatchHour hour;

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
