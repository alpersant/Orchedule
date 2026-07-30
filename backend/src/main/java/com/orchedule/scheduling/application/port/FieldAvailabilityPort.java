package com.orchedule.scheduling.application.port;

import java.util.UUID;

/**
 * ACL port abstracting the field module. Scheduling only needs one
 * question answered: "for this competition, which fields/days/hours are
 * usable this round?" — expressed as a single batched call to avoid
 * redundant round-trips against the field module's application service.
 */
public interface FieldAvailabilityPort {

    /**
     * @throws com.orchedule.scheduling.application.exception.InvalidScheduleException
     *         if the competition has no active fields configured
     */
    FieldSchedulingContext getSchedulingContext(UUID competitionId);
}
