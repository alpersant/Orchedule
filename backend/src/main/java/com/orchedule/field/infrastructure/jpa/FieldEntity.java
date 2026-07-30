package com.orchedule.field.infrastructure.jpa;

import com.orchedule.field.domain.FieldStatus;
import jakarta.persistence.*;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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

    @Column(name = "open_days", nullable = false, length = 100)
    private String openDays;

    @Column(name = "open_hours", nullable = false, length = 100)
    private String openHours;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected FieldEntity() {}

    public FieldEntity(UUID id, UUID venueId, String name, FieldStatus status,
                        Set<DayOfWeek> openDaysSet, Set<LocalTime> openHoursSet,
                        OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.venueId = venueId;
        this.name = name;
        this.status = status;
        this.openDays = openDaysSet.stream().map(Enum::name).collect(Collectors.joining(","));
        this.openHours = openHoursSet.stream().map(LocalTime::toString).collect(Collectors.joining(","));
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() { return id; }
    public UUID getVenueId() { return venueId; }
    public String getName() { return name; }
    public FieldStatus getStatus() { return status; }

    public Set<DayOfWeek> getOpenDaysSet() {
        return Arrays.stream(openDays.split(",")).map(DayOfWeek::valueOf).collect(Collectors.toSet());
    }

    public Set<LocalTime> getOpenHoursSet() {
        return Arrays.stream(openHours.split(",")).map(LocalTime::parse).collect(Collectors.toSet());
    }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
