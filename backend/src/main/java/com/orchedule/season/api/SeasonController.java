package com.orchedule.season.api;

import com.orchedule.season.api.dto.CreateSeasonRequest;
import com.orchedule.season.api.dto.SeasonResponse;
import com.orchedule.season.api.dto.UpdateSeasonRequest;
import com.orchedule.season.application.ActivateSeasonService;
import com.orchedule.season.application.CloseSeasonService;
import com.orchedule.season.application.CreateSeasonService;
import com.orchedule.season.application.GetSeasonService;
import com.orchedule.season.application.UpdateSeasonService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/seasons")
public class SeasonController {

    private final CreateSeasonService createSeasonService;
    private final UpdateSeasonService updateSeasonService;
    private final GetSeasonService getSeasonService;
    private final ActivateSeasonService activateSeasonService;
    private final CloseSeasonService closeSeasonService;

    public SeasonController(CreateSeasonService createSeasonService,
                            UpdateSeasonService updateSeasonService,
                            GetSeasonService getSeasonService,
                            ActivateSeasonService activateSeasonService,
                            CloseSeasonService closeSeasonService) {
        this.createSeasonService = createSeasonService;
        this.updateSeasonService = updateSeasonService;
        this.getSeasonService = getSeasonService;
        this.activateSeasonService = activateSeasonService;
        this.closeSeasonService = closeSeasonService;
    }

    @PostMapping
    public ResponseEntity<SeasonResponse> create(@Valid @RequestBody CreateSeasonRequest request) {
        SeasonResponse response = createSeasonService.create(request);

        return ResponseEntity
                .created(URI.create("/api/v1/seasons/" + response.id()))
                .body(response);
    }

    @PutMapping("/{seasonId}")
    public ResponseEntity<SeasonResponse> update(@PathVariable UUID seasonId,
                                                 @Valid @RequestBody UpdateSeasonRequest request) {
        return ResponseEntity.ok(updateSeasonService.update(seasonId, request));
    }

    @GetMapping("/{seasonId}")
    public ResponseEntity<SeasonResponse> getById(@PathVariable UUID seasonId) {
        return ResponseEntity.ok(getSeasonService.getById(seasonId));
    }

    @GetMapping
    public ResponseEntity<List<SeasonResponse>> getAll() {
        return ResponseEntity.ok(getSeasonService.getAll());
    }

    @PatchMapping("/{seasonId}/activate")
    public ResponseEntity<SeasonResponse> activate(@PathVariable UUID seasonId) {
        return ResponseEntity.ok(activateSeasonService.activate(seasonId));
    }

    @PatchMapping("/{seasonId}/close")
    public ResponseEntity<SeasonResponse> close(@PathVariable UUID seasonId) {
        return ResponseEntity.ok(closeSeasonService.close(seasonId));
    }
}