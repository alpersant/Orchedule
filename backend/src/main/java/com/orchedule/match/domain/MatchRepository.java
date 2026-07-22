package com.orchedule.match.domain;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MatchRepository {
    UUID create(UUID seasonId, UUID homeTeamId, UUID awayTeamId, UUID venueId, OffsetDateTime scheduledAt, MatchStatus status, Integer round, String notes, boolean active);
    Match update(UUID id, UUID seasonId, UUID homeTeamId, UUID awayTeamId, UUID venueId, OffsetDateTime scheduledAt, MatchStatus status, Integer round, String notes, boolean active);
    Optional<Match> findById(UUID id);
    List<Match> findAll();
    List<Match> findBySeasonId(UUID seasonId);
    boolean existsBySeasonIdAndHomeTeamIdAndAwayTeamIdAndScheduledAt(UUID seasonId, UUID homeTeamId, UUID awayTeamId, OffsetDateTime scheduledAt);
}
