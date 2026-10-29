package com.orchedule.scheduling.application.port;

import com.orchedule.scheduling.application.RoundReadinessContext;

import java.util.UUID;

public interface RoundReadinessContextPort {
    RoundReadinessContext loadForWeek(UUID seasonId, int weekNumber);
}
