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
}
