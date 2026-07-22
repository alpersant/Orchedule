package com.orchedule.venue.application;

import com.orchedule.identity.application.exception.AlreadyExistsException;
import com.orchedule.venue.api.dto.CreateVenueRequest;
import com.orchedule.venue.api.dto.VenueResponse;
import com.orchedule.venue.domain.Venue;
import com.orchedule.venue.domain.VenueRepository;
import com.orchedule.venue.domain.VenueStatus;
import com.orchedule.venue.domain.VenueValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateVenueService {

    private final VenueRepository venueRepository;

    public CreateVenueService(VenueRepository venueRepository) {
        this.venueRepository = venueRepository;
    }

    @Transactional
    public VenueResponse create(CreateVenueRequest request) {
        VenueValidator.validate(
                request.name(),
                request.city(),
                request.address(),
                request.capacity(),
                VenueStatus.ACTIVE
        );

        String normalizedName = request.name().trim();

        if (venueRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new AlreadyExistsException("Venue already exists with name: " + normalizedName);
        }

        var venueId = venueRepository.create(
                normalizedName,
                request.city().trim(),
                request.address() == null ? null : request.address().trim(),
                request.capacity(),
                VenueStatus.ACTIVE,
                true
        );

        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new IllegalStateException("Created venue could not be loaded: " + venueId));

        return toResponse(venue);
    }

    private VenueResponse toResponse(Venue venue) {
        return new VenueResponse(
                venue.id(),
                venue.name(),
                venue.city(),
                venue.address(),
                venue.capacity(),
                venue.status(),
                venue.active()
        );
    }
}
