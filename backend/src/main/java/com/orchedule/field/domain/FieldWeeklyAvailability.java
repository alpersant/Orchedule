package com.orchedule.field.domain;

import java.util.UUID;

/**
 * Represents how many fields (and which ones) are enabled for a given
 * competition week. Supports the requirement that some weeks only 1 field
 * is used, other weeks 2 or 3, either globally or per-week override.
 */
public class FieldWeeklyAvailability {

    private final UUID id;
    private final UUID seasonId;
    private final int weekNumber;
    private final UUID fieldId;
    private boolean enabled;

    public FieldWeeklyAvailability(UUID id, UUID seasonId, int weekNumber, UUID fieldId, boolean enabled) {
        this.id = id;
        this.seasonId = seasonId;
        this.weekNumber = weekNumber;
        this.fieldId = fieldId;
        this.enabled = enabled;
    }

    public static FieldWeeklyAvailability create(UUID seasonId, int weekNumber, UUID fieldId, boolean enabled) {
        return new FieldWeeklyAvailability(UUID.randomUUID(), seasonId, weekNumber, fieldId, enabled);
    }

    public void enable() { this.enabled = true; }
    public void disable() { this.enabled = false; }

    public UUID getId() { return id; }
    public UUID getSeasonId() { return seasonId; }
    public int getWeekNumber() { return weekNumber; }
    public UUID getFieldId() { return fieldId; }
    public boolean isEnabled() { return enabled; }
}
