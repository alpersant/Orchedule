package com.orchedule.field.infrastructure.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataFieldRepository extends JpaRepository<FieldEntity, UUID> {

    List<FieldEntity> findByVenueId(UUID venueId);

    List<FieldEntity> findByStatus(com.orchedule.field.domain.FieldStatus status);

    boolean existsByVenueIdAndName(UUID venueId, String name);
}
