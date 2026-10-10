package com.orchedule.venue.application;

import com.orchedule.venue.application.exception.VenueNotFoundException;
import com.orchedule.venue.domain.Venue;
import com.orchedule.venue.domain.VenueRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DeleteVenueService {

    private final VenueRepository venueRepository;

    public DeleteVenueService(
            VenueRepository venueRepository
    ) {
        this.venueRepository = venueRepository;
    }

    @Transactional
    public void delete(UUID venueId) {
        int deletedRows = venueRepository.deleteByVenueId(venueId);

        if (deletedRows == 0) {
            throw new VenueNotFoundException(venueId);
        }
    }
}