package com.orchedule.availability.application;

import com.orchedule.availability.api.dto.AvailabilityResponse;
import com.orchedule.availability.api.dto.CreateAvailabilityRequest;
import com.orchedule.availability.application.exception.InvalidAvailabilityException;
import com.orchedule.availability.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateAvailabilityService {
    private final AvailabilityRepository repo;
    public CreateAvailabilityService(AvailabilityRepository repo) { this.repo = repo; }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public AvailabilityResponse create(CreateAvailabilityRequest request) {
        AvailabilityScope scope = AvailabilityScope.valueOf(request.scope().trim().toUpperCase());
        AvailabilityStatus status = AvailabilityStatus.valueOf(request.status().trim().toUpperCase());
        AvailabilityValidator.validate(scope, request.referenceId(), request.startAt(), request.endAt(), status, request.reason());

        if (repo.existsOverlap(scope, request.referenceId(), request.startAt(), request.endAt(), null)) {
            throw new InvalidAvailabilityException("An overlapping availability rule already exists for this scope and reference");
        }

        var id = repo.create(scope, request.referenceId(), request.startAt(), request.endAt(), status, clean(request.reason()), true);
        Availability availability = repo.findById(id).orElseThrow(() -> new IllegalStateException("Created availability could not be loaded: " + id));
        return toResponse(availability);
    }

    private String clean(String value) { return value == null ? null : value.trim(); }
    private AvailabilityResponse toResponse(Availability a) { return new AvailabilityResponse(a.id(), a.scope(), a.referenceId(), a.startAt(), a.endAt(), a.status(), a.reason(), a.active()); }
}
