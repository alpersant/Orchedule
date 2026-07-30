package com.orchedule.field.infrastructure.jpa;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "field_weekly_availability",
       uniqueConstraints = @UniqueConstraint(columnNames = {"season_id", "week_number", "field_id"}))
public class FieldWeeklyAvailabilityEntity {

    @Id
    private UUID id;

    @Column(name = "season_id", nullable = false)
    private UUID seasonId;

    @Column(name = "week_number", nullable = false)
    private int weekNumber;

    @Column(name = "field_id", nullable = false)
    private UUID fieldId;

    @Column(nullable = false)
    private boolean enabled;

    protected FieldWeeklyAvailabilityEntity() {}

    public FieldWeeklyAvailabilityEntity(UUID id, UUID seasonId, int weekNumber, UUID fieldId, boolean enabled) {
        this.id = id;
        this.seasonId = seasonId;
        this.weekNumber = weekNumber;
        this.fieldId = fieldId;
        this.enabled = enabled;
    }

    public UUID getId() { return id; }
    public UUID getSeasonId() { return seasonId; }
    public int getWeekNumber() { return weekNumber; }
    public UUID getFieldId() { return fieldId; }
    public boolean isEnabled() { return enabled; }

    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
