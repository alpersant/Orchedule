package com.orchedule.team.domain;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * REWRITTEN. This is the aggregate root; it was missing restrictionType /
 * excludedDay / excludedHour entirely (present in RestrictionType,
 * validated by TeamPreferenceValidator, but never stored here — a real
 * gap, not a naming issue). It also used TeamDayPreference/TeamHourPreference
 * (DayOfWeek + boolean primary) while every caller (SaveTeamPreferenceService,
 * TeamPreferenceValidator) operates on DayPreference/TimePreference
 * (MatchDay/MatchHour + PreferencePriority). DayPreference/TimePreference is
 * the richer, validated model (min-primary-days rules, exclusion rules) so
 * it is kept as the ONE preference item type. TeamDayPreference/
 * TeamHourPreference are now obsolete — delete them (see manifest).
 */
public class TeamPreference {

    private final UUID id;
    private final UUID teamId;
    private final UUID competitionId;
    private RestrictionType restrictionType;
    private MatchDay excludedDay;
    private MatchHour excludedHour;
    private List<DayPreference> dayPreferences;
    private List<TimePreference> timePreferences;
    private final OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public TeamPreference(UUID id, UUID teamId, UUID competitionId, RestrictionType restrictionType,
                           MatchDay excludedDay, MatchHour excludedHour,
                           List<DayPreference> dayPreferences, List<TimePreference> timePreferences,
                           OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.teamId = teamId;
        this.competitionId = competitionId;
        this.restrictionType = restrictionType;
        this.excludedDay = excludedDay;
        this.excludedHour = excludedHour;
        this.dayPreferences = List.copyOf(dayPreferences);
        this.timePreferences = List.copyOf(timePreferences);
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static TeamPreference create(UUID teamId, UUID competitionId, RestrictionType restrictionType,
                                         MatchDay excludedDay, MatchHour excludedHour,
                                         List<DayPreference> dayPreferences, List<TimePreference> timePreferences) {
        OffsetDateTime now = OffsetDateTime.now();
        return new TeamPreference(UUID.randomUUID(), teamId, competitionId, restrictionType,
                excludedDay, excludedHour, dayPreferences, timePreferences, now, now);
    }

    public void update(RestrictionType restrictionType, MatchDay excludedDay, MatchHour excludedHour,
                        List<DayPreference> dayPreferences, List<TimePreference> timePreferences) {
        this.restrictionType = restrictionType;
        this.excludedDay = excludedDay;
        this.excludedHour = excludedHour;
        this.dayPreferences = List.copyOf(dayPreferences);
        this.timePreferences = List.copyOf(timePreferences);
        this.updatedAt = OffsetDateTime.now();
    }

    public UUID getId() { return id; }
    public UUID getTeamId() { return teamId; }
    public UUID getCompetitionId() { return competitionId; }
    public RestrictionType getRestrictionType() { return restrictionType; }
    public MatchDay getExcludedDay() { return excludedDay; }
    public MatchHour getExcludedHour() { return excludedHour; }
    public List<DayPreference> getDayPreferences() { return List.copyOf(dayPreferences); }
    public List<TimePreference> getTimePreferences() { return List.copyOf(timePreferences); }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
