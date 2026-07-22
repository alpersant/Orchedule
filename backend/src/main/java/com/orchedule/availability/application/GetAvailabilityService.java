package com.orchedule.availability.application;

import com.orchedule.availability.api.dto.AvailabilityResponse;
import com.orchedule.availability.application.exception.AvailabilityNotFoundException;
import com.orchedule.availability.domain.Availability;
import com.orchedule.availability.domain.AvailabilityRepository;
import com.orchedule.availability.domain.AvailabilityScope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class GetAvailabilityService {
    private final AvailabilityRepository repo;
    public GetAvailabilityService(AvailabilityRepository repo) { this.repo = repo; }

    @Transactional(readOnly = true)
    public AvailabilityResponse getById(UUID id) {
        Availability availability = repo.findById(id).orElseThrow(() -> new AvailabilityNotFoundException(id));
        return toResponse(availability);
    }

    @Transactional(readOnly = true)
    public List<AvailabilityResponse> getAll() {
        return repo.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<AvailabilityResponse> getByScopeAndReference(String scope, UUID referenceId) {
        AvailabilityScope availabilityScope = AvailabilityScope.valueOf(scope.trim().toUpperCase());
        return repo.findByScopeAndReferenceId(availabilityScope, referenceId).stream().map(this::toResponse).toList();
    }

    private AvailabilityResponse toResponse(Availability a) { return new AvailabilityResponse(a.id(), a.scope(), a.referenceId(), a.startAt(), a.endAt(), a.status(), a.reason(), a.active()); }
}
