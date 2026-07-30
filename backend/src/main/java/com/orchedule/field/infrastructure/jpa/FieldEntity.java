package com.orchedule.field.infrastructure.jpa;

import com.orchedule.field.domain.FieldStatus;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "field")
public class FieldEntity {

    @Id
    private UUID id;

    @Column(name = "venue_id", nullable = false)
    private UUID venueId;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FieldStatus status;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected FieldEntity() {}

    public FieldEntity(UUID id, UUID venueId, String name, FieldStatus status,
                        OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.venueId = venueId;
        this.name = name;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() { return id; }
    public UUID getVenueId() { return venueId; }
    public String getName() { return name; }
    public FieldStatus getStatus() { return status; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }

    public void setName(String name) { this.name = name; }
    public void setStatus(FieldStatus status) { this.status = status; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
