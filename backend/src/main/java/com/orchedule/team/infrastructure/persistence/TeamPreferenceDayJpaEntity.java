package com.orchedule.team.infrastructure.persistence;

import com.orchedule.team.domain.MatchDay;
import com.orchedule.team.domain.PreferencePriority;
import jakarta.persistence.*;

@Entity
@Table(name = "team_preference_day")
@IdClass(TeamPreferenceDayId.class)
public class TeamPreferenceDayJpaEntity {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_preference_id", nullable = false)
    private TeamPreferenceJpaEntity teamPreference;

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "day", nullable = false, length = 20)
    private MatchDay day;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    private PreferencePriority priority;

    protected TeamPreferenceDayJpaEntity() {
    }

    public TeamPreferenceJpaEntity getTeamPreference() {
        return teamPreference;
    }

    public void setTeamPreference(TeamPreferenceJpaEntity teamPreference) {
        this.teamPreference = teamPreference;
    }

    public MatchDay getDay() {
        return day;
    }

    public void setDay(MatchDay day) {
        this.day = day;
    }

    public PreferencePriority getPriority() {
        return priority;
    }

    public void setPriority(PreferencePriority priority) {
        this.priority = priority;
    }
}