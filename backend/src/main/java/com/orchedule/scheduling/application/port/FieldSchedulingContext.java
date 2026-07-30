package com.orchedule.scheduling.application.port;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Aggregates everything the field module can tell scheduling about a
 * competition in a SINGLE query round-trip: which fields are usable, and
 * the union of days/hours they are open. Replaces three separate port
 * calls (getAvailableFieldIds/getOpenDays/getOpenHours) that previously
 * each re-fetched the same field list — an avoidable N+1 pattern.
 */
public record FieldSchedulingContext(
        List<UUID> availableFieldIds,
        Set<DayOfWeek> openDays,
        Set<LocalTime> openHours
) {}
