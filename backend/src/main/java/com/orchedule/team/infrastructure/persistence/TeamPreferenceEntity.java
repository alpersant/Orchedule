package com.orchedule.team.infrastructure.persistence;

import com.orchedule.team.domain.MatchDay;
import com.orchedule.team.domain.MatchHour;
import com.orchedule.team.domain.RestrictionType;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "team_preference")
@IdClass(TeamPreferenceEntity.Key.class)
public class TeamPreferenceEntity {

    @Id
    @Column(name = "team_id", nullable = false, updatable = false)
    private UUID teamId;

    @Id
    @Column(name = "competition_id", nullable = false, updatable = false)
    private UUID competitionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "restriction_type", nullable = false, length = 30)
    private RestrictionType restrictionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "excluded_day", length = 20)
    private MatchDay excludedDay;

    @Enumerated(EnumType.STRING)
    @Column(name = "excluded_hour", length = 20)
    private MatchHour excludedHour;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @OneToMany(mappedBy = "teamPreference", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
    private List<TeamPreferenceDayEntity> dayPreferences = new ArrayList<>();

    @OneToMany(mappedBy = "teamPreference", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
    private List<TeamPreferenceHourEntity> hourPreferences = new ArrayList<>();

    protected TeamPreferenceEntity() {}

    public TeamPreferenceEntity(UUID teamId, UUID competitionId, RestrictionType restrictionType,
                                 MatchDay excludedDay, MatchHour excludedHour,
                                 OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.teamId = teamId;
        this.competitionId = competitionId;
        this.restrictionType = restrictionType;
        this.excludedDay = excludedDay;
        this.excludedHour = excludedHour;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void applyUpdate(RestrictionType restrictionType, MatchDay excludedDay,
                             MatchHour excludedHour, OffsetDateTime updatedAt) {
        this.restrictionType = restrictionType;
        this.excludedDay = excludedDay;
        this.excludedHour = excludedHour;
        this.updatedAt = updatedAt;
    }

    public void replaceDayPreferences(List<TeamPreferenceDayEntity> newDayPreferences) {
        this.dayPreferences.clear();
        for (TeamPreferenceDayEntity day : newDayPreferences) {
            day.setTeamPreference(this);
            this.dayPreferences.add(day);
        }
    }

    public void replaceHourPreferences(List<TeamPreferenceHourEntity> newHourPreferences) {
        this.hourPreferences.clear();
        for (TeamPreferenceHourEntity hour : newHourPreferences) {
            hour.setTeamPreference(this);
            this.hourPreferences.add(hour);
        }
    }

    public UUID getTeamId() { return teamId; }
    public UUID getCompetitionId() { return competitionId; }
    public RestrictionType getRestrictionType() { return restrictionType; }
    public MatchDay getExcludedDay() { return excludedDay; }
    public MatchHour getExcludedHour() { return excludedHour; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public List<TeamPreferenceDayEntity> getDayPreferences() { return dayPreferences; }
    public List<TeamPreferenceHourEntity> getHourPreferences() { return hourPreferences; }

    public static class Key implements Serializable {
        private UUID teamId;
        private UUID competitionId;

        public Key() {}

        public Key(UUID teamId, UUID competitionId) {
            this.teamId = teamId;
            this.competitionId = competitionId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Key key)) return false;
            return Objects.equals(teamId, key.teamId) && Objects.equals(competitionId, key.competitionId);
        }

        @Override
        public int hashCode() { return Objects.hash(teamId, competitionId); }
    }
}
