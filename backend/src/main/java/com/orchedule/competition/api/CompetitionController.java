package com.orchedule.competition.api;

import com.orchedule.competition.api.dto.*;
import com.orchedule.competition.application.*;
import com.orchedule.competition.domain.CompetitionStatus;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    private final ArchiveCompetitionService archiveCompetitionService;
    private final DuplicateCompetitionService duplicateCompetitionService;
    private final DeleteCompetitionService deleteCompetitionService;
    private final BatchDeleteCompetitionsService batchDeleteCompetitionsService;

    public CompetitionController(
            CreateCompetitionService createCompetitionService,
            GetCompetitionService getCompetitionService,
            UpdateCompetitionService updateCompetitionService,
            UpdateCompetitionDefaultsService updateCompetitionDefaultsService,
            ActivateCompetitionService activateCompetitionService,
            CloseCompetitionService closeCompetitionService,
            ArchiveCompetitionService archiveCompetitionService,
            DuplicateCompetitionService duplicateCompetitionService,
            DeleteCompetitionService deleteCompetitionService,
            BatchDeleteCompetitionsService batchDeleteCompetitionsService) {
        this.createCompetitionService = createCompetitionService;
        this.getCompetitionService = getCompetitionService;
        this.updateCompetitionService = updateCompetitionService;
        this.updateCompetitionDefaultsService = updateCompetitionDefaultsService;
        this.activateCompetitionService = activateCompetitionService;
        this.closeCompetitionService = closeCompetitionService;
        this.archiveCompetitionService = archiveCompetitionService;
        this.duplicateCompetitionService = duplicateCompetitionService;
        this.deleteCompetitionService = deleteCompetitionService;
        this.batchDeleteCompetitionsService = batchDeleteCompetitionsService;
    }

    @PostMapping
    public ResponseEntity<CompetitionResponse> create(@Valid @RequestBody CreateCompetitionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                CompetitionResponse.from(createCompetitionService.create(request.name(), request.description())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompetitionResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(CompetitionResponse.from(getCompetitionService.getById(id)));
    }

    @GetMapping
    public ResponseEntity<CompetitionPageResponse> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) CompetitionStatus status,
            @RequestParam(required = false) String search) {
        Page<CompetitionResponse> result = getCompetitionService
                .search(page, size, status, search)
                .map(CompetitionResponse::from);
        return ResponseEntity.ok(CompetitionPageResponse.from(result));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CompetitionResponse> update(
            @PathVariable UUID id, @Valid @RequestBody UpdateCompetitionRequest request) {
        return ResponseEntity.ok(CompetitionResponse.from(
                updateCompetitionService.update(id, request.name(), request.description())));
    }

    @PutMapping("/{id}/defaults")
    public ResponseEntity<CompetitionResponse> updateDefaults(
            @PathVariable UUID id, @Valid @RequestBody UpdateCompetitionDefaultsRequest request) {
        return ResponseEntity.ok(CompetitionResponse.from(
                updateCompetitionDefaultsService.updateDefaults(
                        id, request.defaultDays(), request.defaultHours(), request.defaultFieldCount())));
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<CompetitionResponse> activate(@PathVariable UUID id) {
        return ResponseEntity.ok(CompetitionResponse.from(activateCompetitionService.activate(id)));
    }

    @PostMapping("/{id}/close")
    public ResponseEntity<CompetitionResponse> close(@PathVariable UUID id) {
        return ResponseEntity.ok(CompetitionResponse.from(closeCompetitionService.close(id)));
    }

    @PostMapping("/{id}/archive")
    public ResponseEntity<CompetitionResponse> archive(@PathVariable UUID id) {
        return ResponseEntity.ok(CompetitionResponse.from(archiveCompetitionService.archive(id)));
    }

    @PostMapping("/{id}/duplicate")
    public ResponseEntity<CompetitionResponse> duplicate(
            @PathVariable UUID id, @Valid @RequestBody DuplicateCompetitionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                CompetitionResponse.from(duplicateCompetitionService.duplicate(id, request.newName())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        deleteCompetitionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/batch-delete")
    public ResponseEntity<BatchDeleteCompetitionsResponse> batchDelete(
            @Valid @RequestBody BatchDeleteCompetitionsRequest request) {
        return ResponseEntity.ok(batchDeleteCompetitionsService.deleteAll(request.competitionIds()));
    }
}
