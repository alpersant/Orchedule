package com.orchedule.match.infrastructure.jpa;

import com.orchedule.match.domain.Match;
import com.orchedule.match.domain.MatchStatus;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "match_game", uniqueConstraints = {
        @UniqueConstraint(name = "uk_match_season_teams_schedule", columnNames = {"season_id","home_team_id","away_team_id","scheduled_at"})
}, indexes = {
        @Index(name = "idx_match_season_id", columnList = "season_id"),
        @Index(name = "idx_match_status", columnList = "status"),
        @Index(name = "idx_match_scheduled_at", columnList = "scheduled_at")
})
public class MatchEntity {
    @Id @Column(name = "id", nullable = false, updatable = false) private UUID id;
    @Column(name = "season_id", nullable = false) private UUID seasonId;
    @Column(name = "home_team_id", nullable = false) private UUID homeTeamId;
    @Column(name = "away_team_id", nullable = false) private UUID awayTeamId;
    @Column(name = "venue_id", nullable = false) private UUID venueId;
    @Column(name = "scheduled_at", nullable = false) private OffsetDateTime scheduledAt;
    @Enumerated(EnumType.STRING) @Column(name = "status", nullable = false, length = 20) private MatchStatus status;
    @Column(name = "round_number") private Integer round;
    @Column(name = "notes", length = 500) private String notes;
    @Column(name = "active", nullable = false) private boolean active;
    @Column(name = "created_at", nullable = false, updatable = false) private OffsetDateTime createdAt;
    @Column(name = "updated_at", nullable = false) private OffsetDateTime updatedAt;
    protected MatchEntity() {}
    public static MatchEntity create(UUID seasonId, UUID homeTeamId, UUID awayTeamId, UUID venueId, OffsetDateTime scheduledAt, MatchStatus status, Integer round, String notes, boolean active){
        MatchEntity e = new MatchEntity(); e.id=UUID.randomUUID(); e.seasonId=seasonId; e.homeTeamId=homeTeamId; e.awayTeamId=awayTeamId; e.venueId=venueId; e.scheduledAt=scheduledAt; e.status=status; e.round=round; e.notes=notes; e.active=active; e.createdAt=OffsetDateTime.now(); e.updatedAt=e.createdAt; return e;
    }
    public void update(UUID seasonId, UUID homeTeamId, UUID awayTeamId, UUID venueId, OffsetDateTime scheduledAt, MatchStatus status, Integer round, String notes, boolean active){ this.seasonId=seasonId; this.homeTeamId=homeTeamId; this.awayTeamId=awayTeamId; this.venueId=venueId; this.scheduledAt=scheduledAt; this.status=status; this.round=round; this.notes=notes; this.active=active; this.updatedAt=OffsetDateTime.now(); }
    public Match toDomain(){ return new Match(id, seasonId, homeTeamId, awayTeamId, venueId, scheduledAt, status, round, notes, active, createdAt, updatedAt); }
    public UUID getId(){ return id; }
}
