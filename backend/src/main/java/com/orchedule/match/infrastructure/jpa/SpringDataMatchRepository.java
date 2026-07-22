package com.orchedule.match.infrastructure.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface SpringDataMatchRepository extends JpaRepository<MatchEntity, UUID> {
    List<MatchEntity> findAllBySeasonIdOrderByScheduledAtAsc(UUID seasonId);
    boolean existsBySeasonIdAndHomeTeamIdAndAwayTeamIdAndScheduledAt(UUID seasonId, UUID homeTeamId, UUID awayTeamId, OffsetDateTime scheduledAt);
}
