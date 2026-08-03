package com.orchedule.scheduling.application;

import com.orchedule.scheduling.domain.TeamScheduleProfile;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;


public record GenerateRoundCommand(
        UUID seasonId,
        int weekNumber,
        List<TeamScheduleProfile> teamProfiles,
        Set<DayOfWeek> openDays,
        Set<LocalTime> openHours,
        List<UUID> availableFieldIds
) {}
