package com.orchedule.competition.domain;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

public class Competition {

    public static final Set<CompetitionDay> DEFAULT_DAYS = Set.of(
            CompetitionDay.MONDAY, CompetitionDay.TUESDAY, CompetitionDay.WEDNESDAY,
            CompetitionDay.THURSDAY, CompetitionDay.FRIDAY);

    public static final Set<CompetitionHour> DEFAULT_HOURS = Set.of(
            CompetitionHour.H18, CompetitionHour.H19, CompetitionHour.H20,
            CompetitionHour.H21, CompetitionHour.H22);

    public static final int DEFAULT_FIELD_COUNT = 1;

    public static final PreferencePolicy DEFAULT_PREFERENCE_POLICY = PreferencePolicy.FLEXIBLE;

    private final UUID id;
    private String name;
    private String description;
    private CompetitionStatus status;
    private Set<CompetitionDay> defaultDays;
    private Set<CompetitionHour> defaultHours;
    private int defaultFieldCount;
    private PreferencePolicy preferencePolicy;
    private final OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public Competition(UUID id, String name, String description, CompetitionStatus status,
                        Set<CompetitionDay> defaultDays, Set<CompetitionHour> defaultHours,
                        int defaultFieldCount, PreferencePolicy preferencePolicy,
                        OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.status = status;
        this.defaultDays = defaultDays;
        this.defaultHours = defaultHours;
        this.defaultFieldCount = defaultFieldCount;
        this.preferencePolicy = preferencePolicy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Competition createWithDefaults(String name, String description) {
        OffsetDateTime now = OffsetDateTime.now();
        return new Competition(UUID.randomUUID(), name, description, CompetitionStatus.DRAFT,
                DEFAULT_DAYS, DEFAULT_HOURS, DEFAULT_FIELD_COUNT, DEFAULT_PREFERENCE_POLICY, now, now);
    }

    public void updateDetails(String name, String description) {
        this.name = name;
        this.description = description;
        touch();
    }

    public void updateDefaultDays(Set<CompetitionDay> days) {
        this.defaultDays = Set.copyOf(days);
        touch();
    }

    public void updateDefaultHours(Set<CompetitionHour> hours) {
        this.defaultHours = Set.copyOf(hours);
        touch();
    }

    public void updateDefaultFieldCount(int fieldCount) {
        this.defaultFieldCount = fieldCount;
        touch();
    }

    /**
     * Opt-in switch: the organizer explicitly chooses to enforce the strict
     * single-elimination rule for this competition. Not the system default,
     * per the generic-by-design requirement.
     */
    public void updatePreferencePolicy(PreferencePolicy policy) {
        this.preferencePolicy = policy;
        touch();
    }

    public void activate() {
        if (status == CompetitionStatus.CLOSED) {
            throw new IllegalStateException("Cannot activate a closed competition");
        }
        this.status = CompetitionStatus.ACTIVE;
        touch();
    }

    public void close() {
        this.status = CompetitionStatus.CLOSED;
        touch();
    }

    private void touch() {
        this.updatedAt = OffsetDateTime.now();
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public CompetitionStatus getStatus() { return status; }
    public Set<CompetitionDay> getDefaultDays() { return Set.copyOf(defaultDays); }
    public Set<CompetitionHour> getDefaultHours() { return Set.copyOf(defaultHours); }
    public int getDefaultFieldCount() { return defaultFieldCount; }
    public PreferencePolicy getPreferencePolicy() { return preferencePolicy; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }

    public boolean isActive() { return status == CompetitionStatus.ACTIVE; }
    public boolean isClosed() { return status == CompetitionStatus.CLOSED; }
}
