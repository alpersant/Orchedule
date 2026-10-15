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
    private final com.orchedule.season.application.DeleteSeasonService deleteSeasonService;
    private final com.orchedule.season.application.BatchDeleteSeasonsService batchDeleteSeasonsService;

    public SeasonController(CreateSeasonService createSeasonService,
                            UpdateSeasonService updateSeasonService,
                            GetSeasonService getSeasonService,
                            ActivateSeasonService activateSeasonService,
                            CloseSeasonService closeSeasonService,
                            com.orchedule.season.application.DeleteSeasonService deleteSeasonService,
                            com.orchedule.season.application.BatchDeleteSeasonsService batchDeleteSeasonsService) {
        this.createSeasonService = createSeasonService;
        this.updateSeasonService = updateSeasonService;
        this.getSeasonService = getSeasonService;
        this.activateSeasonService = activateSeasonService;
        this.closeSeasonService = closeSeasonService;
        this.deleteSeasonService = deleteSeasonService;
        this.batchDeleteSeasonsService = batchDeleteSeasonsService;
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
    @GetMapping("/search")
    public ResponseEntity<com.orchedule.season.api.dto.SeasonPageResponse> search(
            @RequestParam(required=false) UUID competitionId,
            @RequestParam(required=false) String search,
            @RequestParam(required=false) com.orchedule.season.domain.SeasonStatus status,
            @RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue="20") int size) {
        return ResponseEntity.ok(getSeasonService.search(competitionId, search, status, page, size));
    }

    @DeleteMapping("/{seasonId}")
    public ResponseEntity<Void> delete(@PathVariable UUID seasonId) {
        deleteSeasonService.delete(seasonId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/batch-delete")
    public ResponseEntity<com.orchedule.season.application.BatchDeleteSeasonsResult> deleteMany(
            @Valid @RequestBody com.orchedule.season.api.dto.BatchDeleteSeasonsRequest request) {
        return ResponseEntity.ok(batchDeleteSeasonsService.delete(request.seasonIds()));
    }
}
