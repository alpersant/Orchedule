package com.orchedule.venue.api;

import com.orchedule.venue.api.dto.CreateVenueRequest;
import com.orchedule.venue.api.dto.UpdateVenueRequest;
import com.orchedule.venue.api.dto.VenueResponse;
import com.orchedule.venue.application.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/venues")
public class VenueController {

    private final CreateVenueService createVenueService;
    private final UpdateVenueService updateVenueService;
    private final GetVenueService getVenueService;
    private final ActivateVenueService activateVenueService;
    private final DeactivateVenueService deactivateVenueService;
    private final DeleteVenueService deleteVenueService;

    public VenueController(CreateVenueService createVenueService,
                           UpdateVenueService updateVenueService,
                           GetVenueService getVenueService,
                           ActivateVenueService activateVenueService,
                           DeactivateVenueService deactivateVenueService,
                           DeleteVenueService deleteVenueService) {
        this.createVenueService = createVenueService;
        this.updateVenueService = updateVenueService;
        this.getVenueService = getVenueService;
        this.activateVenueService = activateVenueService;
        this.deactivateVenueService = deactivateVenueService;

        this.deleteVenueService = deleteVenueService;
    }

    @PostMapping
    public ResponseEntity<VenueResponse> create(@Valid @RequestBody CreateVenueRequest request) {
        VenueResponse response = createVenueService.create(request);

        return ResponseEntity
                .created(URI.create("/api/v1/venues/" + response.id()))
                .body(response);
    }

    @PutMapping("/{venueId}")
    public ResponseEntity<VenueResponse> update(@PathVariable UUID venueId,
                                                @Valid @RequestBody UpdateVenueRequest request) {
        return ResponseEntity.ok(updateVenueService.update(venueId, request));
    }

    @GetMapping("/{venueId}")
    public ResponseEntity<VenueResponse> getById(@PathVariable UUID venueId) {
        return ResponseEntity.ok(getVenueService.getById(venueId));
    }

    @GetMapping
    public ResponseEntity<List<VenueResponse>> getAll() {
        return ResponseEntity.ok(getVenueService.getAll());
    }

    @PatchMapping("/{venueId}/activate")
    public ResponseEntity<VenueResponse> activate(@PathVariable UUID venueId) {
        return ResponseEntity.ok(activateVenueService.activate(venueId));
    }

    @PatchMapping("/{venueId}/deactivate")
    public ResponseEntity<VenueResponse> deactivate(@PathVariable UUID venueId) {
        return ResponseEntity.ok(deactivateVenueService.deactivate(venueId));
    }

    @DeleteMapping("/{venueId}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID venueId
    ) {
        deleteVenueService.delete(venueId);

        return ResponseEntity.noContent().build();
    }
}
