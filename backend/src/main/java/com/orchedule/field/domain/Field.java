package com.orchedule.field.domain;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

public class Field {

    private final UUID id;
    private final UUID venueId;
    private String name;
    private FieldStatus status;
    private Set<DayOfWeek> openDays;
    private Set<LocalTime> openHours;
    private final OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public Field(UUID id, UUID venueId, String name, FieldStatus status,
                 Set<DayOfWeek> openDays, Set<LocalTime> openHours,
                 OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.venueId = venueId;
        this.name = name;
        this.status = status;
        this.openDays = Set.copyOf(openDays);
        this.openHours = Set.copyOf(openHours);
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Field create(UUID venueId, String name) {
        OffsetDateTime now = OffsetDateTime.now();
        Set<DayOfWeek> defaultDays = Set.of(DayOfWeek.values());
        Set<LocalTime> defaultHours = Set.of(
                LocalTime.of(18, 0), LocalTime.of(19, 0), LocalTime.of(20, 0),
                LocalTime.of(21, 0), LocalTime.of(22, 0));
        return new Field(UUID.randomUUID(), venueId, name, FieldStatus.ACTIVE,
                defaultDays, defaultHours, now, now);
    }

    public void rename(String newName) {
        this.name = newName;
        this.updatedAt = OffsetDateTime.now();
    }

    public void activate() {
        this.status = FieldStatus.ACTIVE;
        this.updatedAt = OffsetDateTime.now();
    }

    public void deactivate() {
        this.status = FieldStatus.INACTIVE;
        this.updatedAt = OffsetDateTime.now();
    }

    public void changeOpeningHours(Set<DayOfWeek> openDays, Set<LocalTime> openHours) {
        FieldValidator.validateOpeningHours(openDays, openHours);
        this.openDays = Set.copyOf(openDays);
        this.openHours = Set.copyOf(openHours);
        this.updatedAt = OffsetDateTime.now();
    }

    public boolean isActive() {
        return status == FieldStatus.ACTIVE;
    }

    public UUID getId() { return id; }
    public UUID getVenueId() { return venueId; }
    public String getName() { return name; }
    public FieldStatus getStatus() { return status; }
    public Set<DayOfWeek> getOpenDays() { return Set.copyOf(openDays); }
    public Set<LocalTime> getOpenHours() { return Set.copyOf(openHours); }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
