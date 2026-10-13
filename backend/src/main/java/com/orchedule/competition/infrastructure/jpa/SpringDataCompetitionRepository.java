package com.orchedule.competition.infrastructure.jpa;

import com.orchedule.competition.domain.CompetitionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface SpringDataCompetitionRepository
        extends JpaRepository<CompetitionEntity, UUID>,
        JpaSpecificationExecutor<CompetitionEntity> {

    boolean existsByName(String name);

    List<CompetitionEntity> findByStatus(CompetitionStatus status);
}