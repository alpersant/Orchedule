package com.orchedule.season.infrastructure.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataSeasonRepository extends JpaRepository<SeasonEntity, UUID> {

    boolean existsByNameIgnoreCase(String name);

    List<SeasonEntity> findAllByOrderByStartDateAsc();
}