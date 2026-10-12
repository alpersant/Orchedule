package com.orchedule.team.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface SpringDataTeamRepository extends JpaRepository<TeamJpaEntity, UUID> {

    boolean existsByNameIgnoreCase(String name);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            delete from TeamJpaEntity team
            where team.id = :teamId
            """)
    int deleteTeamById(UUID teamId);
}