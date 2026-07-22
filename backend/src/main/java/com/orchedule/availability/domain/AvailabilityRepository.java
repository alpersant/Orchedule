package com.orchedule.availability.domain;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AvailabilityRepository {
    UUID create(AvailabilityScope scope, UUID referenceId, OffsetDateTime startAt, OffsetDateTime endAt, AvailabilityStatus status, String reason, boolean active);
    Availability update(UUID id, AvailabilityScope scope, UUID referenceId, OffsetDateTime startAt, OffsetDateTime endAt, AvailabilityStatus status, String reason, boolean active);
    Optional<Availability> findById(UUID id);
    List<Availability> findAll();
    List<Availability> findByScopeAndReferenceId(AvailabilityScope scope, UUID referenceId);
    boolean existsOverlap(AvailabilityScope scope, UUID referenceId, OffsetDateTime startAt, OffsetDateTime endAt, UUID excludeId);
}
