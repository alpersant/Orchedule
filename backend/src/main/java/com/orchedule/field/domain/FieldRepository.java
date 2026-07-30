package com.orchedule.field.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FieldRepository {

    Field save(Field field);

    Optional<Field> findById(UUID id);

    List<Field> findByVenueId(UUID venueId);

    List<Field> findAllActive();

    boolean existsByVenueIdAndName(UUID venueId, String name);
}
