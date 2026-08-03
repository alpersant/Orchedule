package com.orchedule.availability.application;

import com.orchedule.availability.api.dto.AvailabilityResponse;
import com.orchedule.availability.api.dto.UpdateAvailabilityRequest;
import com.orchedule.availability.application.exception.AvailabilityNotFoundException;
import com.orchedule.availability.application.exception.InvalidAvailabilityException;
import com.orchedule.availability.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UpdateAvailabilityService {
    private final AvailabilityRepository repo;
    public UpdateAvailabilityService(AvailabilityRepository repo) { this.repo = repo; }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public AvailabilityResponse update(UUID id, UpdateAvailabilityRequest request) {
        Availability current = repo.findById(id).orElseThrow(() -> new AvailabilityNotFoundException(id));
        AvailabilityScope scope = AvailabilityScope.valueOf(request.scope().trim().toUpperCase());
        AvailabilityStatus status = AvailabilityStatus.valueOf(request.status().trim().toUpperCase());
        AvailabilityValidator.validate(scope, request.referenceId(), request.startAt(), request.endAt(), status, request.reason());

        if (repo.existsOverlap(scope, request.referenceId(), request.startAt(), request.endAt(), id)) {
            throw new InvalidAvailabilityException("An overlapping availability rule already exists for this scope and reference");
        }

        Availability updated = repo.update(current.id(), scope, request.referenceId(), request.startAt(), request.endAt(), status, clean(request.reason()), request.active());
        return toResponse(updated);
    }

    private String clean(String value) { return value == null ? null : value.trim(); }
    private AvailabilityResponse toResponse(Availability a) { return new AvailabilityResponse(a.id(), a.scope(), a.referenceId(), a.startAt(), a.endAt(), a.status(), a.reason(), a.active()); }
}
