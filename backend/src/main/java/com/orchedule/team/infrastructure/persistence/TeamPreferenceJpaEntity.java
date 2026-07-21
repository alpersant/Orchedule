package com.orchedule.team.infrastructure.persistence;

import com.orchedule.team.domain.MatchDay;
import com.orchedule.team.domain.MatchHour;
import com.orchedule.team.domain.RestrictionType;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "team_preference")
public class TeamPreferenceJpaEntity {

    @Id
    @Column(name = "team_id", nullable = false, updatable = false)
    private UUID teamId;

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

    @OneToMany(mappedBy = "teamPreference", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TeamPreferenceDayJpaEntity> dayPreferences = new ArrayList<>();

    @OneToMany(mappedBy = "teamPreference", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TeamPreferenceHourJpaEntity> hourPreferences = new ArrayList<>();

    protected TeamPreferenceJpaEntity() {
    }

    public UUID getTeamId() {
        return teamId;
    }

    public void setTeamId(UUID teamId) {
        this.teamId = teamId;
    }

    public RestrictionType getRestrictionType() {
        return restrictionType;
    }

    public void setRestrictionType(RestrictionType restrictionType) {
        this.restrictionType = restrictionType;
    }

    public MatchDay getExcludedDay() {
        return excludedDay;
    }

    public void setExcludedDay(MatchDay excludedDay) {
        this.excludedDay = excludedDay;
    }

    public MatchHour getExcludedHour() {
        return excludedHour;
    }

    public void setExcludedHour(MatchHour excludedHour) {
        this.excludedHour = excludedHour;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<TeamPreferenceDayJpaEntity> getDayPreferences() {
        return dayPreferences;
    }

    public void setDayPreferences(List<TeamPreferenceDayJpaEntity> dayPreferences) {
        this.dayPreferences = dayPreferences;
    }

    public List<TeamPreferenceHourJpaEntity> getHourPreferences() {
        return hourPreferences;
    }

    public void setHourPreferences(List<TeamPreferenceHourJpaEntity> hourPreferences) {
        this.hourPreferences = hourPreferences;
    }
}