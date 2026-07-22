package com.orchedule.availability.infrastructure.jpa;

import com.orchedule.availability.domain.*;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "availability_rule", indexes = {
        @Index(name = "idx_availability_scope_reference", columnList = "scope,reference_id"),
        @Index(name = "idx_availability_start_at", columnList = "start_at"),
        @Index(name = "idx_availability_end_at", columnList = "end_at"),
        @Index(name = "idx_availability_active", columnList = "active")
})
public class AvailabilityEntity {
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "scope", nullable = false, length = 20)
    private AvailabilityScope scope;

    @Column(name = "reference_id", nullable = false)
    private UUID referenceId;

    @Column(name = "start_at", nullable = false)
    private OffsetDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private OffsetDateTime endAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AvailabilityStatus status;

    @Column(name = "reason", length = 255)
    private String reason;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected AvailabilityEntity() {}

    public static AvailabilityEntity create(AvailabilityScope scope, UUID referenceId, OffsetDateTime startAt, OffsetDateTime endAt, AvailabilityStatus status, String reason, boolean active) {
        AvailabilityEntity entity = new AvailabilityEntity();
        OffsetDateTime now = OffsetDateTime.now();
        entity.id = UUID.randomUUID();
        entity.scope = scope;
        entity.referenceId = referenceId;
        entity.startAt = startAt;
        entity.endAt = endAt;
        entity.status = status;
        entity.reason = reason;
        entity.active = active;
        entity.createdAt = now;
        entity.updatedAt = now;
        return entity;
    }

    public void update(AvailabilityScope scope, UUID referenceId, OffsetDateTime startAt, OffsetDateTime endAt, AvailabilityStatus status, String reason, boolean active) {
        this.scope = scope;
        this.referenceId = referenceId;
        this.startAt = startAt;
        this.endAt = endAt;
        this.status = status;
        this.reason = reason;
        this.active = active;
        this.updatedAt = OffsetDateTime.now();
    }

    public Availability toDomain() {
        return new Availability(id, scope, referenceId, startAt, endAt, status, reason, active, createdAt, updatedAt);
    }

    public UUID getId() { return id; }
}
