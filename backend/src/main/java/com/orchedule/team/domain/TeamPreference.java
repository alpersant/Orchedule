package com.orchedule.team.domain;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Aggregate root holding a team's day/hour scheduling preferences for a
 * specific competition. Kept SEPARATE from the {@link Team} record
 * (identity: id, name, active) because Team is intentionally minimal and
 * immutable — preferences are a distinct concern with their own lifecycle
 * (they change often, independently of the team's name/active status) and
 * their own competition-scoped validation rules (see
 * com.orchedule.team.domain.preferences.TeamPreferenceValidationStrategy).
 *
 * One Team may have at most one TeamPreferences row per competition it is
 * registered in — enforced by a unique (team_id, competition_id)
 * constraint at the persistence level.
 */
public class TeamPreference {

    private final UUID id;
    private final UUID teamId;
    private final UUID competitionId;
    private List<TeamDayPreference> dayPreferences;
    private List<TeamHourPreference> hourPreferences;
    private final OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public TeamPreference(UUID id, UUID teamId, UUID competitionId,
                          List<TeamDayPreference> dayPreferences, List<TeamHourPreference> hourPreferences,
                          OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.teamId = teamId;
        this.competitionId = competitionId;
        this.dayPreferences = List.copyOf(dayPreferences);
        this.hourPreferences = List.copyOf(hourPreferences);
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static TeamPreference create(UUID teamId, UUID competitionId,
                                        List<TeamDayPreference> dayPreferences,
                                        List<TeamHourPreference> hourPreferences) {
        OffsetDateTime now = OffsetDateTime.now();
        return new TeamPreference(UUID.randomUUID(), teamId, competitionId,
                dayPreferences, hourPreferences, now, now);
    }

    public void update(List<TeamDayPreference> dayPreferences, List<TeamHourPreference> hourPreferences) {
        this.dayPreferences = List.copyOf(dayPreferences);
        this.hourPreferences = List.copyOf(hourPreferences);
        this.updatedAt = OffsetDateTime.now();
    }

    public UUID getId() { return id; }
    public UUID getTeamId() { return teamId; }
    public UUID getCompetitionId() { return competitionId; }
    public List<TeamDayPreference> getDayPreferences() { return List.copyOf(dayPreferences); }
    public List<TeamHourPreference> getHourPreferences() { return List.copyOf(hourPreferences); }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
