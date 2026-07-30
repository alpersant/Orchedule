package com.orchedule.field.api;

import com.orchedule.field.api.dto.*;
import com.orchedule.field.application.*;
import com.orchedule.field.domain.Field;
import com.orchedule.field.domain.FieldWeeklyAvailability;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/fields")
public class FieldController {

    private final CreateFieldService createFieldService;
    private final GetFieldService getFieldService;
    private final UpdateFieldService updateFieldService;
    private final ActivateFieldService activateFieldService;
    private final DeactivateFieldService deactivateFieldService;
    private final SetFieldAvailabilityForWeekService setAvailabilityService;
    private final GetFieldAvailabilityService getAvailabilityService;

    public FieldController(CreateFieldService createFieldService,
                            GetFieldService getFieldService,
                            UpdateFieldService updateFieldService,
                            ActivateFieldService activateFieldService,
                            DeactivateFieldService deactivateFieldService,
                            SetFieldAvailabilityForWeekService setAvailabilityService,
                            GetFieldAvailabilityService getAvailabilityService) {
        this.createFieldService = createFieldService;
        this.getFieldService = getFieldService;
        this.updateFieldService = updateFieldService;
        this.activateFieldService = activateFieldService;
        this.deactivateFieldService = deactivateFieldService;
        this.setAvailabilityService = setAvailabilityService;
        this.getAvailabilityService = getAvailabilityService;
    }

    @PostMapping
    public ResponseEntity<FieldResponse> create(@Valid @RequestBody CreateFieldRequest request) {
        Field field = createFieldService.create(request.venueId(), request.name());
        return ResponseEntity.status(HttpStatus.CREATED).body(FieldResponse.from(field));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FieldResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(FieldResponse.from(getFieldService.getById(id)));
    }

    @GetMapping
    public ResponseEntity<List<FieldResponse>> getByVenue(@RequestParam(required = false) UUID venueId) {
        List<Field> fields = venueId != null
                ? getFieldService.getByVenue(venueId)
                : getFieldService.getAllActive();
        return ResponseEntity.ok(fields.stream().map(FieldResponse::from).toList());
    }

    @PutMapping("/{id}")
    public ResponseEntity<FieldResponse> update(@PathVariable UUID id, @Valid @RequestBody UpdateFieldRequest request) {
        Field field = updateFieldService.rename(id, request.name());
        return ResponseEntity.ok(FieldResponse.from(field));
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<FieldResponse> activate(@PathVariable UUID id) {
        return ResponseEntity.ok(FieldResponse.from(activateFieldService.activate(id)));
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<FieldResponse> deactivate(@PathVariable UUID id) {
        return ResponseEntity.ok(FieldResponse.from(deactivateFieldService.deactivate(id)));
    }

    @PutMapping("/availability")
    public ResponseEntity<List<FieldWeeklyAvailabilityResponse>> setWeeklyAvailability(
            @Valid @RequestBody SetWeeklyAvailabilityRequest request) {
        List<FieldWeeklyAvailability> result = setAvailabilityService.setForWeek(
                request.seasonId(), request.weekNumber(), request.enabledFieldIds());
        return ResponseEntity.ok(result.stream().map(FieldWeeklyAvailabilityResponse::from).toList());
    }

    @GetMapping("/availability")
    public ResponseEntity<List<FieldWeeklyAvailabilityResponse>> getWeeklyAvailability(
            @RequestParam UUID seasonId, @RequestParam(required = false) Integer weekNumber) {
        List<FieldWeeklyAvailability> result = weekNumber != null
                ? getAvailabilityService.getForWeek(seasonId, weekNumber)
                : getAvailabilityService.getForSeason(seasonId);
        return ResponseEntity.ok(result.stream().map(FieldWeeklyAvailabilityResponse::from).toList());
    }
}
