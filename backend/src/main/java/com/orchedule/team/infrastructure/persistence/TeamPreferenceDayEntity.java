package com.orchedule.team.infrastructure.persistence;

import com.orchedule.team.domain.MatchDay;
import com.orchedule.team.domain.PreferencePriority;
import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;


@Entity
@Table(name = "team_preference_day")
@IdClass(TeamPreferenceDayEntity.Key.class)
public class TeamPreferenceDayEntity {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "team_id", referencedColumnName = "team_id", nullable = false),
            @JoinColumn(name = "competition_id", referencedColumnName = "competition_id", nullable = false)
    })
    private TeamPreferenceEntity teamPreference;

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "day", nullable = false, length = 20)
    private MatchDay day;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    private PreferencePriority priority;

    protected TeamPreferenceDayEntity() {}

    public TeamPreferenceDayEntity(MatchDay day, PreferencePriority priority) {
        this.day = day;
        this.priority = priority;
    }

    public void setTeamPreference(TeamPreferenceEntity teamPreference) { this.teamPreference = teamPreference; }
    public MatchDay getDay() { return day; }
    public PreferencePriority getPriority() { return priority; }

    public static class Key implements Serializable {
        private TeamPreferenceEntity.Key teamPreference;
        private MatchDay day;

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
