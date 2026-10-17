package com.orchedule.field.api;

import com.orchedule.field.api.dto.ApplyWeeklyAvailabilityRequest;
import com.orchedule.field.api.dto.FieldWeeklyAvailabilityResponse;
import com.orchedule.field.api.dto.SetWeeklyAvailabilityRequest;
import com.orchedule.field.application.ApplyWeeklyAvailabilityService;
import com.orchedule.field.application.ClearWeeklyAvailabilityService;
import com.orchedule.field.application.GetFieldAvailabilityService;
import com.orchedule.field.application.SetFieldAvailabilityForWeekService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/fields/availability")
public class FieldWeeklyAvailabilityController {

    private final GetFieldAvailabilityService getAvailabilityService;
    private final SetFieldAvailabilityForWeekService setAvailabilityService;
    private final ApplyWeeklyAvailabilityService applyAvailabilityService;
    private final ClearWeeklyAvailabilityService clearAvailabilityService;

    public FieldWeeklyAvailabilityController(GetFieldAvailabilityService getAvailabilityService,
                                             SetFieldAvailabilityForWeekService setAvailabilityService,
                                             ApplyWeeklyAvailabilityService applyAvailabilityService,
                                             ClearWeeklyAvailabilityService clearAvailabilityService) {
        this.getAvailabilityService = getAvailabilityService;
        this.setAvailabilityService = setAvailabilityService;
        this.applyAvailabilityService = applyAvailabilityService;
        this.clearAvailabilityService = clearAvailabilityService;
    }

    @GetMapping
    public ResponseEntity<List<FieldWeeklyAvailabilityResponse>> get(
            @RequestParam UUID seasonId,
            @RequestParam(required = false) Integer weekNumber) {
        List<FieldWeeklyAvailabilityResponse> response = weekNumber == null
                ? getAvailabilityService.getForSeason(seasonId).stream()
                        .map(FieldWeeklyAvailabilityResponse::from).toList()
                : getAvailabilityService.getForWeek(seasonId, weekNumber).stream()
                        .map(FieldWeeklyAvailabilityResponse::from).toList();
        return ResponseEntity.ok(response);
    }

    @PutMapping
    public ResponseEntity<List<FieldWeeklyAvailabilityResponse>> set(
            @Valid @RequestBody SetWeeklyAvailabilityRequest request) {
        List<FieldWeeklyAvailabilityResponse> response = setAvailabilityService
                .setForWeek(request.seasonId(), request.weekNumber(), request.enabledFieldIds())
                .stream().map(FieldWeeklyAvailabilityResponse::from).toList();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/apply")
    public ResponseEntity<List<FieldWeeklyAvailabilityResponse>> apply(
            @Valid @RequestBody ApplyWeeklyAvailabilityRequest request) {
        List<FieldWeeklyAvailabilityResponse> response = applyAvailabilityService
                .apply(request.seasonId(), request.weekNumbers(), request.enabledFieldIds())
                .stream().map(FieldWeeklyAvailabilityResponse::from).toList();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    public ResponseEntity<List<FieldWeeklyAvailabilityResponse>> clear(
            @RequestParam UUID seasonId,
            @RequestParam int weekNumber) {
        List<FieldWeeklyAvailabilityResponse> response = clearAvailabilityService
                .clear(seasonId, weekNumber)
                .stream().map(FieldWeeklyAvailabilityResponse::from).toList();
        return ResponseEntity.ok(response);
    }
}
