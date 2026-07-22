package com.orchedule.availability.api;

import com.orchedule.availability.api.dto.AvailabilityResponse;
import com.orchedule.availability.api.dto.CreateAvailabilityRequest;
import com.orchedule.availability.api.dto.UpdateAvailabilityRequest;
import com.orchedule.availability.application.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/availability")
public class AvailabilityController {
    private final CreateAvailabilityService createService;
    private final UpdateAvailabilityService updateService;
    private final GetAvailabilityService getService;
    private final ActivateAvailabilityService activateService;
    private final DeactivateAvailabilityService deactivateService;

    public AvailabilityController(CreateAvailabilityService createService, UpdateAvailabilityService updateService, GetAvailabilityService getService, ActivateAvailabilityService activateService, DeactivateAvailabilityService deactivateService) {
        this.createService = createService;
        this.updateService = updateService;
        this.getService = getService;
        this.activateService = activateService;
        this.deactivateService = deactivateService;
    }

    @PostMapping
    public ResponseEntity<AvailabilityResponse> create(@Valid @RequestBody CreateAvailabilityRequest request) {
        AvailabilityResponse response = createService.create(request);
        return ResponseEntity.created(URI.create("/api/v1/availability/" + response.id())).body(response);
    }

    @PutMapping("/{availabilityId}")
    public ResponseEntity<AvailabilityResponse> update(@PathVariable UUID availabilityId, @Valid @RequestBody UpdateAvailabilityRequest request) {
        return ResponseEntity.ok(updateService.update(availabilityId, request));
    }

    @GetMapping("/{availabilityId}")
    public ResponseEntity<AvailabilityResponse> getById(@PathVariable UUID availabilityId) {
        return ResponseEntity.ok(getService.getById(availabilityId));
    }

    @GetMapping
    public ResponseEntity<List<AvailabilityResponse>> getAll(@RequestParam(required = false) String scope, @RequestParam(required = false) UUID referenceId) {
        if (scope != null && referenceId != null) {
            return ResponseEntity.ok(getService.getByScopeAndReference(scope, referenceId));
        }
        return ResponseEntity.ok(getService.getAll());
    }

    @PatchMapping("/{availabilityId}/activate")
    public ResponseEntity<AvailabilityResponse> activate(@PathVariable UUID availabilityId) {
        return ResponseEntity.ok(activateService.activate(availabilityId));
    }

    @PatchMapping("/{availabilityId}/deactivate")
    public ResponseEntity<AvailabilityResponse> deactivate(@PathVariable UUID availabilityId) {
        return ResponseEntity.ok(deactivateService.deactivate(availabilityId));
    }
}
