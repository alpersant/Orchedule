package com.orchedule.competition.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CompetitionRepository {

    Competition save(Competition competition);

    Optional<Competition> findById(UUID id);

    List<Competition> findAll();

    List<Competition> findByStatus(CompetitionStatus status);

    boolean existsByName(String name);

    Page<Competition> search(
            CompetitionStatus status,
            String search,
            Pageable pageable
    );

    void delete(Competition competition);
}