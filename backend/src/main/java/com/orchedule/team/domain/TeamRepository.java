package com.orchedule.team.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TeamRepository {

    UUID create(String name);

    Optional<Team> findById(UUID id);

    List<Team> findAll();

    boolean existsByNameIgnoreCase(String name);

    Team update(UUID id, String name, boolean active);
}