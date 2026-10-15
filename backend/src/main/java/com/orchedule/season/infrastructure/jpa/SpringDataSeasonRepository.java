package com.orchedule.season.infrastructure.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataSeasonRepository extends JpaRepository<SeasonEntity, UUID>, org.springframework.data.jpa.repository.JpaSpecificationExecutor<SeasonEntity> {

    boolean existsByNameIgnoreCase(String name);

    List<SeasonEntity> findAllByOrderByStartDateAsc();
    boolean existsByCompetitionIdAndNameIgnoreCase(UUID competitionId, String name);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("delete from SeasonEntity s where s.id = :id")
    int deleteRow(@org.springframework.data.repository.query.Param("id") UUID id);
}
