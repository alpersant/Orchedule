package com.orchedule.venue.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VenueRepository {

    UUID create(String name,
                String city,
                String address,
                Integer capacity,
                VenueStatus status,
                boolean active);

    Venue update(UUID id,
                 String name,
                 String city,
                 String address,
                 Integer capacity,
                 VenueStatus status,
                 boolean active);

    Optional<Venue> findById(UUID id);

    List<Venue> findAll();

    boolean existsByNameIgnoreCase(String name);

    int deleteByVenueId(UUID venueId);
}
