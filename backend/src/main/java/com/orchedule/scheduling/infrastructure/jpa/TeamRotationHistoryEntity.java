package com.orchedule.scheduling.infrastructure.jpa;

import jakarta.persistence.*;

import java.util.UUID;

/**
 * Stores the serialized recent-day/recent-hour history for a team so
 * rotation state survives across separate schedule generation calls.
 */
@Entity
@Table(name = "team_rotation_history")
public class TeamRotationHistoryEntity {

    @Id
    private UUID teamId;

    @Column(name = "recent_days", length = 100)
    private String recentDaysCsv;

    @Column(name = "recent_hours", length = 100)
    private String recentHoursCsv;

    protected TeamRotationHistoryEntity() {}

    public TeamRotationHistoryEntity(UUID teamId, String recentDaysCsv, String recentHoursCsv) {
        this.teamId = teamId;
        this.recentDaysCsv = recentDaysCsv;
        this.recentHoursCsv = recentHoursCsv;
    }

    public UUID getTeamId() { return teamId; }
    public String getRecentDaysCsv() { return recentDaysCsv; }
    public String getRecentHoursCsv() { return recentHoursCsv; }

    public void setRecentDaysCsv(String recentDaysCsv) { this.recentDaysCsv = recentDaysCsv; }
    public void setRecentHoursCsv(String recentHoursCsv) { this.recentHoursCsv = recentHoursCsv; }
}
