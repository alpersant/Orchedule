package com.orchedule.team.infrastructure.persistence;

import jakarta.persistence.*;

import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "team_hour_preferences")
public class TeamHourPreferenceEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "preferences_id", nullable = false)
    private TeamPreferenceEntity preferences;

    @Column(nullable = false)
    private LocalTime hour;

    @Column(nullable = false)
    private boolean primary;

    protected TeamHourPreferenceEntity() {}

    public TeamHourPreferenceEntity(UUID id, LocalTime hour, boolean primary) {
        this.id = id;
        this.hour = hour;
        this.primary = primary;
    }

    public UUID getId() { return id; }
    public LocalTime getHour() { return hour; }
    public boolean isPrimary() { return primary; }
    public void setPreferences(TeamPreferenceEntity preferences) { this.preferences = preferences; }
}
