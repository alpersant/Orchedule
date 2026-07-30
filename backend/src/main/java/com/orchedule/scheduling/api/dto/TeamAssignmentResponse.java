package com.orchedule.scheduling.api.dto;

import com.orchedule.scheduling.domain.TeamAssignment;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

public record TeamAssignmentResponse(
        UUID teamId,
        DayOfWeek day,
        LocalTime hour,
        UUID fieldId
) {
    public static TeamAssignmentResponse from(TeamAssignment assignment) {
        return new TeamAssignmentResponse(
                assignment.teamId(), assignment.slot().day(), assignment.slot().hour(), assignment.slot().fieldId());
    }
}
