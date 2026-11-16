package com.orchedule.venue.infrastructure.jpa;

import com.orchedule.venue.domain.Venue;
import com.orchedule.venue.domain.VenueRepository;
import com.orchedule.venue.domain.VenueStatus;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class VenueRepositoryJpaAdapter implements VenueRepository {

    private final SpringDataVenueRepository springDataVenueRepository;

    public VenueRepositoryJpaAdapter(SpringDataVenueRepository springDataVenueRepository) {
        this.springDataVenueRepository = springDataVenueRepository;
    }

    @Override
    public UUID create(String name,
                       String city,
                       String address,
                       Integer capacity,
                       VenueStatus status,
                       boolean active) {
        VenueEntity entity = VenueEntity.create(
                name,
                city,
                address,
                capacity,
                status,
                active
        );

        return springDataVenueRepository.save(entity).getId();
    }

    @Override
    public Venue update(UUID id,
                        String name,
                        String city,
                        String address,
                        Integer capacity,
                        VenueStatus status,
                        boolean active) {
        VenueEntity entity = springDataVenueRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Venue persistence inconsistency for id: " + id));

        entity.update(name, city, address, capacity, status, active);

        return springDataVenueRepository.save(entity).toDomain();
    }

    @Override
    public Optional<Venue> findById(UUID id) {
        return springDataVenueRepository.findById(id)
                .map(VenueEntity::toDomain);
    }

    @Override
    public List<Venue> findAll() {
        return springDataVenueRepository.findAllByOrderByNameAsc()
                .stream()
                .map(VenueEntity::toDomain)
                .toList();
    }

    @Override
    public boolean existsByNameIgnoreCase(String name) {
        return springDataVenueRepository.existsByNameIgnoreCase(name);
    }

    @Override
    public int deleteByVenueId(UUID venueId) {
        return springDataVenueRepository.deleteByVenueId(venueId);
    }

    @Override
    public com.orchedule.shared.pagination.PageResult<Venue> search(String search, Boolean active, int page, int size) {
        if (page < 0 || size < 1 || size > 100 || (search != null && search.length() > 150))
            throw new IllegalArgumentException("Invalid pagination or search");
        org.springframework.data.jpa.domain.Specification<VenueEntity> spec = (root, query, cb) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            if (active != null) predicates.add(cb.equal(root.get("active"), active));
            if (search != null && !search.isBlank()) {
                String escaped = search.trim().toLowerCase(java.util.Locale.ROOT)
                    .replace("!", "!!").replace("%", "!%").replace("_", "!_");
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + escaped + "%", '!'));
            }
            return cb.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
        var result = springDataVenueRepository.findAll(spec, org.springframework.data.domain.PageRequest.of(page, size,
            org.springframework.data.domain.Sort.by("name").and(org.springframework.data.domain.Sort.by("id"))))
            .map(VenueEntity::toDomain);
        return new com.orchedule.shared.pagination.PageResult<>(result.getContent(), result.getNumber(),
            result.getSize(), result.getTotalElements(), result.getTotalPages(), result.isLast());
    }

}

