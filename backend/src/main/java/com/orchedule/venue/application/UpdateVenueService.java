package com.orchedule.venue.application;

import com.orchedule.identity.application.exception.AlreadyExistsException;
import com.orchedule.venue.api.dto.UpdateVenueRequest;
import com.orchedule.venue.api.dto.VenueResponse;
import com.orchedule.venue.application.exception.VenueNotFoundException;
import com.orchedule.venue.domain.Venue;
import com.orchedule.venue.domain.VenueRepository;
import com.orchedule.venue.domain.VenueValidator;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UpdateVenueService {

    private final VenueRepository venueRepository;

    public UpdateVenueService(VenueRepository venueRepository) {
        this.venueRepository = venueRepository;
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public VenueResponse update(UUID venueId, UpdateVenueRequest request) {
        Venue currentVenue = venueRepository.findById(venueId)
                .orElseThrow(() -> new VenueNotFoundException(venueId));

        VenueValidator.validate(
                request.name(),
                request.city(),
                request.address(),
                request.capacity(),
                currentVenue.status()
        );

        String normalizedName = request.name().trim();
        boolean nameChanged = !currentVenue.name().equalsIgnoreCase(normalizedName);

        if (nameChanged && venueRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new AlreadyExistsException("Venue already exists with name: " + normalizedName);
        }

        Venue updatedVenue = venueRepository.update(
                venueId,
                normalizedName,
                request.city().trim(),
                request.address() == null ? null : request.address().trim(),
                request.capacity(),
                currentVenue.status(),
                request.active()
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
