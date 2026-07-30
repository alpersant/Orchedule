package com.orchedule.competition.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CompetitionRepository {

    Competition save(Competition competition);

    Optional<Competition> findById(UUID id);

    List<Competition> findAll();

    List<Competition> findByStatus(CompetitionStatus status);

    boolean existsByName(String name);
}
