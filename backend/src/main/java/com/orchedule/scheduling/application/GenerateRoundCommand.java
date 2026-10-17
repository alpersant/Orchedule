package com.orchedule.scheduling.application;

import com.orchedule.scheduling.application.port.FieldScheduleSlot;
import com.orchedule.scheduling.domain.TeamScheduleProfile;

import java.util.List;
import java.util.UUID;

public record GenerateRoundCommand(
        UUID seasonId,
        int weekNumber,
        List<TeamScheduleProfile> teamProfiles,
        List<FieldScheduleSlot> fieldSlots
) {
    public GenerateRoundCommand {
        teamProfiles = List.copyOf(teamProfiles);
        fieldSlots = List.copyOf(fieldSlots);
    }
}
