package com.orchedule.field.application.port;

import com.orchedule.scheduling.application.port.FieldSchedulingContext;

import java.util.UUID;

public interface FieldAvailabilityPort {

    FieldSchedulingContext getSchedulingContext(UUID competitionId, UUID seasonId, int weekNumber);
}
