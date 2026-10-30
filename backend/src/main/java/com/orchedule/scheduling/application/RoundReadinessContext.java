package com.orchedule.scheduling.application;

import com.orchedule.scheduling.domain.TeamScheduleProfile;
import com.orchedule.scheduling.application.port.FieldScheduleSlot;

import java.util.List;

public record RoundReadinessContext(
        List<TeamScheduleProfile> teamProfiles,
        List<FieldScheduleSlot> fieldSlots,
        String existingRoundStatus
) {
    public RoundReadinessContext {
        teamProfiles = teamProfiles == null ? List.of() : List.copyOf(teamProfiles);
        fieldSlots = fieldSlots == null ? List.of() : List.copyOf(fieldSlots);
    }
}
