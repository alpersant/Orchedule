package com.orchedule.season.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SeasonRepository {

    UUID create(String name,
                LocalDate startDate,
                LocalDate endDate,
                SeasonStatus status,
                boolean active);

    Season update(UUID id,
                  String name,
                  LocalDate startDate,
                  LocalDate endDate,
                  SeasonStatus status,
                  boolean active);

    Optional<Season> findById(UUID id);

    List<Season> findAll();

    boolean existsByNameIgnoreCase(String name);
}