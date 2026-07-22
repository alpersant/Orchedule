package com.orchedule.match.infrastructure.jpa;

import com.orchedule.match.domain.Match;
import com.orchedule.match.domain.MatchRepository;
import com.orchedule.match.domain.MatchStatus;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class MatchRepositoryJpaAdapter implements MatchRepository {
    private final SpringDataMatchRepository repo;
    public MatchRepositoryJpaAdapter(SpringDataMatchRepository repo){ this.repo=repo; }
    @Override public UUID create(UUID seasonId, UUID homeTeamId, UUID awayTeamId, UUID venueId, OffsetDateTime scheduledAt, MatchStatus status, Integer round, String notes, boolean active){ return repo.save(MatchEntity.create(seasonId, homeTeamId, awayTeamId, venueId, scheduledAt, status, round, notes, active)).getId(); }
    @Override public Match update(UUID id, UUID seasonId, UUID homeTeamId, UUID awayTeamId, UUID venueId, OffsetDateTime scheduledAt, MatchStatus status, Integer round, String notes, boolean active){ MatchEntity e = repo.findById(id).orElseThrow(() -> new IllegalStateException("Match persistence inconsistency for id: " + id)); e.update(seasonId, homeTeamId, awayTeamId, venueId, scheduledAt, status, round, notes, active); return repo.save(e).toDomain(); }
    @Override public Optional<Match> findById(UUID id){ return repo.findById(id).map(MatchEntity::toDomain); }
    @Override public List<Match> findAll(){ return repo.findAll().stream().map(MatchEntity::toDomain).toList(); }
    @Override public List<Match> findBySeasonId(UUID seasonId){ return repo.findAllBySeasonIdOrderByScheduledAtAsc(seasonId).stream().map(MatchEntity::toDomain).toList(); }
    @Override public boolean existsBySeasonIdAndHomeTeamIdAndAwayTeamIdAndScheduledAt(UUID seasonId, UUID homeTeamId, UUID awayTeamId, OffsetDateTime scheduledAt){ return repo.existsBySeasonIdAndHomeTeamIdAndAwayTeamIdAndScheduledAt(seasonId, homeTeamId, awayTeamId, scheduledAt); }
}
