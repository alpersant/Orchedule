package com.orchedule.competition.api;

import com.orchedule.competition.api.dto.*;
import com.orchedule.competition.application.*;
import com.orchedule.competition.domain.Competition;
import com.orchedule.competition.domain.CompetitionStatus;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/competitions")
public class CompetitionController {

    private final CreateCompetitionService createCompetitionService;
    private final GetCompetitionService getCompetitionService;
    private final UpdateCompetitionService updateCompetitionService;
    private final UpdateCompetitionDefaultsService updateCompetitionDefaultsService;
    private final ActivateCompetitionService activateCompetitionService;
    private final CloseCompetitionService closeCompetitionService;

    public CompetitionController(CreateCompetitionService createCompetitionService,
                                  GetCompetitionService getCompetitionService,
                                  UpdateCompetitionService updateCompetitionService,
                                  UpdateCompetitionDefaultsService updateCompetitionDefaultsService,
                                  ActivateCompetitionService activateCompetitionService,
                                  CloseCompetitionService closeCompetitionService) {
        this.createCompetitionService = createCompetitionService;
        this.getCompetitionService = getCompetitionService;
        this.updateCompetitionService = updateCompetitionService;
        this.updateCompetitionDefaultsService = updateCompetitionDefaultsService;
        this.activateCompetitionService = activateCompetitionService;
        this.closeCompetitionService = closeCompetitionService;
    }

    @PostMapping
    public ResponseEntity<CompetitionResponse> create(@Valid @RequestBody CreateCompetitionRequest request) {
        Competition competition = createCompetitionService.create(request.name(), request.description());
        return ResponseEntity.status(HttpStatus.CREATED).body(CompetitionResponse.from(competition));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompetitionResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(CompetitionResponse.from(getCompetitionService.getById(id)));
    }

    @GetMapping
    public ResponseEntity<List<CompetitionResponse>> getAll(
            @RequestParam(required = false) CompetitionStatus status) {
        List<Competition> competitions = status != null
                ? getCompetitionService.getByStatus(status)
                : getCompetitionService.getAll();
        return ResponseEntity.ok(competitions.stream().map(CompetitionResponse::from).toList());
    }

    @PutMapping("/{id}")
    public ResponseEntity<CompetitionResponse> update(
            @PathVariable UUID id, @Valid @RequestBody UpdateCompetitionRequest request) {
        Competition competition = updateCompetitionService.update(id, request.name(), request.description());
        return ResponseEntity.ok(CompetitionResponse.from(competition));
    }

    @PutMapping("/{id}/defaults")
    public ResponseEntity<CompetitionResponse> updateDefaults(
            @PathVariable UUID id, @Valid @RequestBody UpdateCompetitionDefaultsRequest request) {
        Competition competition = updateCompetitionDefaultsService.updateDefaults(
                id, request.defaultDays(), request.defaultHours(), request.defaultFieldCount());
        return ResponseEntity.ok(CompetitionResponse.from(competition));
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<CompetitionResponse> activate(@PathVariable UUID id) {
        return ResponseEntity.ok(CompetitionResponse.from(activateCompetitionService.activate(id)));
    }

    @PostMapping("/{id}/close")
    public ResponseEntity<CompetitionResponse> close(@PathVariable UUID id) {
        return ResponseEntity.ok(CompetitionResponse.from(closeCompetitionService.close(id)));
    }
}
