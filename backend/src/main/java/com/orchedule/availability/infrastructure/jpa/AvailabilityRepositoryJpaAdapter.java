package com.orchedule.availability.infrastructure.jpa;

import com.orchedule.availability.domain.*;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class AvailabilityRepositoryJpaAdapter implements AvailabilityRepository {
    private final SpringDataAvailabilityRepository repo;
    public AvailabilityRepositoryJpaAdapter(SpringDataAvailabilityRepository repo) { this.repo = repo; }

    @Override
    public UUID create(AvailabilityScope scope, UUID referenceId, OffsetDateTime startAt, OffsetDateTime endAt, AvailabilityStatus status, String reason, boolean active) {
        return repo.save(AvailabilityEntity.create(scope, referenceId, startAt, endAt, status, reason, active)).getId();
    }

    @Override
    public Availability update(UUID id, AvailabilityScope scope, UUID referenceId, OffsetDateTime startAt, OffsetDateTime endAt, AvailabilityStatus status, String reason, boolean active) {
        AvailabilityEntity entity = repo.findById(id).orElseThrow(() -> new IllegalStateException("Availability persistence inconsistency for id: " + id));
        entity.update(scope, referenceId, startAt, endAt, status, reason, active);
        return repo.save(entity).toDomain();
    }

    @Override
    public Optional<Availability> findById(UUID id) { return repo.findById(id).map(AvailabilityEntity::toDomain); }

    @Override
    public List<Availability> findAll() { return repo.findAllByOrderByStartAtAsc().stream().map(AvailabilityEntity::toDomain).toList(); }

    @Override
    public List<Availability> findByScopeAndReferenceId(AvailabilityScope scope, UUID referenceId) {
        return repo.findAllByScopeAndReferenceIdOrderByStartAtAsc(scope, referenceId).stream().map(AvailabilityEntity::toDomain).toList();
    }

    @Override
    public boolean existsOverlap(AvailabilityScope scope, UUID referenceId, OffsetDateTime startAt, OffsetDateTime endAt, UUID excludeId) {
        return repo.existsOverlap(scope, referenceId, startAt, endAt, excludeId);
    }
}
