package com.orchedule.availability.application;

import com.orchedule.availability.api.dto.AvailabilityResponse;
import com.orchedule.availability.application.exception.AvailabilityNotFoundException;
import com.orchedule.availability.application.exception.InvalidAvailabilityException;
import com.orchedule.availability.domain.Availability;
import com.orchedule.availability.domain.AvailabilityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ActivateAvailabilityService {
    private final AvailabilityRepository repo;
    public ActivateAvailabilityService(AvailabilityRepository repo) { this.repo = repo; }

    @Transactional
    public AvailabilityResponse activate(UUID id) {
        Availability current = repo.findById(id).orElseThrow(() -> new AvailabilityNotFoundException(id));
        if (current.active()) throw new InvalidAvailabilityException("Availability is already active");
        if (repo.existsOverlap(current.scope(), current.referenceId(), current.startAt(), current.endAt(), id)) {
            throw new InvalidAvailabilityException("An overlapping active availability rule already exists for this scope and reference");
        }
        Availability updated = repo.update(current.id(), current.scope(), current.referenceId(), current.startAt(), current.endAt(), current.status(), current.reason(), true);
        return toResponse(updated);
    }

    private AvailabilityResponse toResponse(Availability a) { return new AvailabilityResponse(a.id(), a.scope(), a.referenceId(), a.startAt(), a.endAt(), a.status(), a.reason(), a.active()); }
}
