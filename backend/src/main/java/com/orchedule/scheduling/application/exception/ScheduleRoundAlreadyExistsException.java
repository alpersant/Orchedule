package com.orchedule.scheduling.application.exception;

import com.orchedule.shared.exception.ConflictException;

import java.util.UUID;

public class ScheduleRoundAlreadyExistsException extends ConflictException {

    public ScheduleRoundAlreadyExistsException(UUID seasonId, int weekNumber) {
        super("SCHEDULE_ROUND_ALREADY_EXISTS",
                "A schedule round already exists for season " + seasonId + " week " + weekNumber);
    }
}
