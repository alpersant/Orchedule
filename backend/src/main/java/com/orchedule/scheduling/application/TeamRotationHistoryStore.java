package com.orchedule.scheduling.application;

import com.orchedule.scheduling.domain.TeamRotationHistory;

import java.util.UUID;

/**
 * Port for retrieving and persisting each team's recent rotation history,
 * used by the RotationEngine to avoid repeating the same day/hour more than
 * twice in a row. Implemented by infrastructure (JPA-backed or in-memory).
 */
public interface TeamRotationHistoryStore {

    TeamRotationHistory getOrCreate(UUID teamId);

    void save(TeamRotationHistory history);
}
