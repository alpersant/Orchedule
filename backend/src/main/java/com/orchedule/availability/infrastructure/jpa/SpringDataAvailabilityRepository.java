package com.orchedule.availability.infrastructure.jpa;

import com.orchedule.availability.domain.AvailabilityScope;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface SpringDataAvailabilityRepository extends JpaRepository<AvailabilityEntity, UUID> {
    List<AvailabilityEntity> findAllByOrderByStartAtAsc();
    List<AvailabilityEntity> findAllByScopeAndReferenceIdOrderByStartAtAsc(AvailabilityScope scope, UUID referenceId);

    @Query("""
            select case when count(a) > 0 then true else false end
            from AvailabilityEntity a
            where a.scope = :scope
              and a.referenceId = :referenceId
              and a.active = true
              and (:excludeId is null or a.id <> :excludeId)
              and a.startAt < :endAt
              and a.endAt > :startAt
            """)
    boolean existsOverlap(@Param("scope") AvailabilityScope scope,
                          @Param("referenceId") UUID referenceId,
                          @Param("startAt") OffsetDateTime startAt,
                          @Param("endAt") OffsetDateTime endAt,
                          @Param("excludeId") UUID excludeId);
}
