package com.orchedule.scheduling.infrastructure.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataTeamRotationHistoryRepository extends JpaRepository<TeamRotationHistoryEntity, UUID> {
}
