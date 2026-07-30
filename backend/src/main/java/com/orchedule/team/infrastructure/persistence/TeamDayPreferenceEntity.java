package com.orchedule.team.infrastructure.persistence;

import jakarta.persistence.*;

import java.time.DayOfWeek;
import java.util.UUID;

@Entity
@Table(name = "team_day_preferences")
public class TeamDayPreferenceEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "preferences_id", nullable = false)
    private TeamPreferenceEntity preferences;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private DayOfWeek day;

    @Column(nullable = false)
    private boolean primary;

    protected TeamDayPreferenceEntity() {}

    public TeamDayPreferenceEntity(UUID id, DayOfWeek day, boolean primary) {
        this.id = id;
        this.day = day;
        this.primary = primary;
    }

    public UUID getId() { return id; }
    public DayOfWeek getDay() { return day; }
    public boolean isPrimary() { return primary; }
    public void setPreferences(TeamPreferenceEntity preferences) { this.preferences = preferences; }
}
