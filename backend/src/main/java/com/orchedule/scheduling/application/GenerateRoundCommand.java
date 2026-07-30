package com.orchedule.scheduling.application;

import com.orchedule.scheduling.domain.TeamScheduleProfile;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Immutable command object carrying every input GenerateScheduleForSeasonService
 * needs. Produced by {@link ScheduleInputAssembler}, consumed by the
 * service — this keeps the service signature stable even as the number of
 * upstream data sources grows.
 */
public record GenerateRoundCommand(
        UUID seasonId,
        int weekNumber,
        List<TeamScheduleProfile> teamProfiles,
        Set<DayOfWeek> openDays,
        Set<LocalTime> openHours,
        List<UUID> availableFieldIds
) {}
