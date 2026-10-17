package com.orchedule.field.infrastructure.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataFieldRepository extends JpaRepository<FieldEntity, UUID> {

    List<FieldEntity> findByVenueId(UUID venueId);

    @org.springframework.data.jpa.repository.Query(value = """
            select f.* from field f join venue v on v.id = f.venue_id
            where f.status = :statusName and v.active = true and v.status = 'ACTIVE'
            order by f.name, f.id
            """, nativeQuery = true)
    List<FieldEntity> findEligibleByStatusName(@org.springframework.data.repository.query.Param("statusName") String statusName);

    default List<FieldEntity> findByStatus(com.orchedule.field.domain.FieldStatus status) {
        return findEligibleByStatusName(status.name());
    }

    boolean existsByVenueIdAndName(UUID venueId, String name);
}
