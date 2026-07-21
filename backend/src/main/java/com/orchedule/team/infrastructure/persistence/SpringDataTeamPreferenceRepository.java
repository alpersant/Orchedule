package com.orchedule.team.infrastructure.persistence;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataTeamPreferenceRepository extends JpaRepository<TeamPreferenceJpaEntity, UUID> {

    @EntityGraph(attributePaths = {"dayPreferences", "hourPreferences"})
    Optional<TeamPreferenceJpaEntity> findByTeamId(UUID teamId);
}