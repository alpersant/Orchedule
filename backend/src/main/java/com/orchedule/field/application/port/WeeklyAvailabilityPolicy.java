package com.orchedule.field.application.port;

import java.util.List;
import java.util.UUID;

/** Cross-module checks without leaking season or venue domain models. */
public interface WeeklyAvailabilityPolicy {
    void validateRead(UUID seasonId, Integer weekNumber);
    void lockAndValidateWrite(UUID seasonId, int weekNumber, List<UUID> enabledFieldIds);
}
