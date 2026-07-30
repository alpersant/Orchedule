package com.orchedule.scheduling.application.exception;

import com.orchedule.shared.exception.BusinessRuleException;

public class ScheduleGenerationException extends BusinessRuleException {

    public ScheduleGenerationException(String message) {
        super("SCHEDULE_GENERATION_FAILED", message);
    }
}
