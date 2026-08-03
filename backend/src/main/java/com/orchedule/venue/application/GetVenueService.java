package com.orchedule.venue.application;

import com.orchedule.venue.api.dto.VenueResponse;
import com.orchedule.venue.application.exception.VenueNotFoundException;
import com.orchedule.venue.domain.Venue;
import com.orchedule.venue.domain.VenueRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class GetVenueService {

    private final VenueRepository venueRepository;

    public GetVenueService(VenueRepository venueRepository) {
        this.venueRepository = venueRepository;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public VenueResponse getById(UUID venueId) {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new VenueNotFoundException(venueId));

        return toResponse(venue);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public List<VenueResponse> getAll() {
        return venueRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
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
