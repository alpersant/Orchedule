package com.orchedule.venue.application;

import com.orchedule.venue.application.exception.VenueNotFoundException;
import com.orchedule.venue.application.port.VenueDeletionGuard;
import org.springframework.security.access.prepost.PreAuthorize;
import com.orchedule.venue.domain.VenueRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DeleteVenueService {

    private final VenueRepository venueRepository;

    private final VenueDeletionGuard deletionGuard;

    public DeleteVenueService(
            VenueRepository venueRepository,
            VenueDeletionGuard deletionGuard
    ) {
        this.venueRepository = venueRepository;
        this.deletionGuard = deletionGuard;
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public void delete(UUID venueId) {
        deletionGuard.lockAndAssertDeletable(venueId);
        int deletedRows = venueRepository.deleteByVenueId(venueId);

        if (deletedRows == 0) {
            throw new VenueNotFoundException(venueId);
        }
    }
}
