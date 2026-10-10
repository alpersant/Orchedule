package com.orchedule.venue.infrastructure.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface SpringDataVenueRepository extends JpaRepository<VenueEntity, UUID> {

    boolean existsByNameIgnoreCase(String name);

    List<VenueEntity> findAllByOrderByNameAsc();

    @Modifying
    @Query("""
            delete from VenueEntity venue
            where venue.id = :venueId
            """)
    int deleteByVenueId(
            @Param("venueId") UUID venueId
    );
}
