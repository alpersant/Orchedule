package com.orchedule.team.infrastructure.persistence;

import com.orchedule.team.domain.MatchHour;
import com.orchedule.team.domain.PreferencePriority;
import jakarta.persistence.*;

@Entity
@Table(name = "team_preference_hour")
@IdClass(TeamPreferenceHourId.class)
public class TeamPreferenceHourJpaEntity {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_preference_id", nullable = false)
    private TeamPreferenceJpaEntity teamPreference;

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "hour", nullable = false, length = 20)
    private MatchHour hour;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    private PreferencePriority priority;

    protected TeamPreferenceHourJpaEntity() {
    }

    public TeamPreferenceJpaEntity getTeamPreference() {
        return teamPreference;
    }

    public void setTeamPreference(TeamPreferenceJpaEntity teamPreference) {
        this.teamPreference = teamPreference;
    }

    public MatchHour getHour() {
        return hour;
    }

    public void setHour(MatchHour hour) {
        this.hour = hour;
    }

    public PreferencePriority getPriority() {
        return priority;
    }

    public void setPriority(PreferencePriority priority) {
        this.priority = priority;
    }
}