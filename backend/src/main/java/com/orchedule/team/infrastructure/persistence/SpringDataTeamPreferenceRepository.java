package com.orchedule.team.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataTeamPreferenceRepository
        extends JpaRepository<TeamPreferenceEntity, TeamPreferenceEntity.Key> {

    Optional<TeamPreferenceEntity> findByTeamIdAndCompetitionId(UUID teamId, UUID competitionId);

    List<TeamPreferenceEntity> findByCompetitionId(UUID competitionId);

    boolean existsByTeamIdAndCompetitionId(UUID teamId, UUID competitionId);
}
