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

    @org.springframework.data.jpa.repository.Query("""
        select p from TeamPreferenceEntity p
        where p.competitionId = :competitionId and p.teamId in :teamIds
        """)
    List<TeamPreferenceEntity> findSelected(
        @org.springframework.data.repository.query.Param("teamIds") List<UUID> teamIds,
        @org.springframework.data.repository.query.Param("competitionId") UUID competitionId);

    @org.springframework.data.jpa.repository.Query("""
        select p from TeamPreferenceEntity p
        where p.competitionId = :competitionId
        and exists (select t.id from TeamJpaEntity t where t.id = p.teamId and t.active = true)
        order by p.teamId
        """)
    List<TeamPreferenceEntity> findActiveForCompetition(
        @org.springframework.data.repository.query.Param("competitionId") UUID competitionId);

}

