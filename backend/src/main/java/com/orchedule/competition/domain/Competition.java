package com.orchedule.competition.domain;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Aggregate root holding the global defaults of a competition: which days of
 * the week are played by default, which hours are open by default, and how
 * many fields are used by default. Individual seasons/weeks may later
 * override these defaults (see field.FieldWeeklyAvailability).
 */
public class Competition {

    public static final Set<CompetitionDay> DEFAULT_DAYS = Set.of(
            CompetitionDay.MONDAY, CompetitionDay.TUESDAY, CompetitionDay.WEDNESDAY,
            CompetitionDay.THURSDAY, CompetitionDay.FRIDAY);

    public static final Set<CompetitionHour> DEFAULT_HOURS = Set.of(
            CompetitionHour.H18, CompetitionHour.H19, CompetitionHour.H20,
            CompetitionHour.H21, CompetitionHour.H22);

    public static final int DEFAULT_FIELD_COUNT = 1;

    private final UUID id;
    private String name;
    private String description;
    private CompetitionStatus status;
    private Set<CompetitionDay> defaultDays;
    private Set<CompetitionHour> defaultHours;
    private int defaultFieldCount;
    private final OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public Competition(UUID id, String name, String description, CompetitionStatus status,
                        Set<CompetitionDay> defaultDays, Set<CompetitionHour> defaultHours,
                        int defaultFieldCount, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.status = status;
        this.defaultDays = new HashSet<>(defaultDays);
        this.defaultHours = new HashSet<>(defaultHours);
        this.defaultFieldCount = defaultFieldCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Competition createWithDefaults(String name, String description) {
        OffsetDateTime now = OffsetDateTime.now();
        return new Competition(UUID.randomUUID(), name, description, CompetitionStatus.DRAFT,
                DEFAULT_DAYS, DEFAULT_HOURS, DEFAULT_FIELD_COUNT, now, now);
    }

    public void updateDetails(String name, String description) {
        this.name = name;
        this.description = description;
        touch();
    }

    public void updateDefaultDays(Set<CompetitionDay> days) {
        this.defaultDays = new HashSet<>(days);
        touch();
    }

    public void updateDefaultHours(Set<CompetitionHour> hours) {
        this.defaultHours = new HashSet<>(hours);
        touch();
    }

    public void updateDefaultFieldCount(int fieldCount) {
        this.defaultFieldCount = fieldCount;
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

    public boolean isActive() {
        return status == CompetitionStatus.ACTIVE;
    }

    public boolean isClosed() {
        return status == CompetitionStatus.CLOSED;
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
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
