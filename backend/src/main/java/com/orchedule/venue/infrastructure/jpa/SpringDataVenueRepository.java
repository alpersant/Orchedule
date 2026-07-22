package com.orchedule.venue.infrastructure.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataVenueRepository extends JpaRepository<VenueEntity, UUID> {

    boolean existsByNameIgnoreCase(String name);

    List<VenueEntity> findAllByOrderByNameAsc();
}
