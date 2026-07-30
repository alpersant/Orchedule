package com.orchedule.scheduling.application.exception;

import com.orchedule.shared.exception.NotFoundException;

import java.util.UUID;

public class ScheduleRoundNotFoundException extends NotFoundException {

    public ScheduleRoundNotFoundException(UUID roundId) {
        super("SCHEDULE_ROUND_NOT_FOUND", "Schedule round not found: " + roundId);
    }

    public ScheduleRoundNotFoundException(UUID seasonId, int weekNumber) {
        super("SCHEDULE_ROUND_NOT_FOUND",
                "Schedule round not found for season " + seasonId + " week " + weekNumber);
    }
}
