package com.orchedule.venue.application;

import com.orchedule.venue.api.dto.VenueResponse;
import com.orchedule.venue.application.exception.InvalidVenueException;
import com.orchedule.venue.application.exception.VenueNotFoundException;
import com.orchedule.venue.domain.Venue;
import com.orchedule.venue.domain.VenueRepository;
import com.orchedule.venue.domain.VenueStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ActivateVenueService {

    private final VenueRepository venueRepository;

    public ActivateVenueService(VenueRepository venueRepository) {
        this.venueRepository = venueRepository;
    }

    @Transactional
    public VenueResponse activate(UUID venueId) {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new VenueNotFoundException(venueId));

        if (venue.status() == VenueStatus.ACTIVE && venue.active()) {
            throw new InvalidVenueException("Venue is already active");
        }

        Venue updatedVenue = venueRepository.update(
                venue.id(),
                venue.name(),
                venue.city(),
                venue.address(),
                venue.capacity(),
                VenueStatus.ACTIVE,
                true
        );

        return toResponse(updatedVenue);
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
