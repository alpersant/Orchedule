package com.orchedule.field.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

public class Field {

    private final UUID id;
    private final UUID venueId;
    private String name;
    private FieldStatus status;
    private final OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public Field(UUID id, UUID venueId, String name, FieldStatus status,
                 OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.venueId = venueId;
        this.name = name;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Field create(UUID venueId, String name) {
        OffsetDateTime now = OffsetDateTime.now();
        return new Field(UUID.randomUUID(), venueId, name, FieldStatus.ACTIVE, now, now);
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

    public boolean isActive() {
        return status == FieldStatus.ACTIVE;
    }

    public UUID getId() { return id; }
    public UUID getVenueId() { return venueId; }
    public String getName() { return name; }
    public FieldStatus getStatus() { return status; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
