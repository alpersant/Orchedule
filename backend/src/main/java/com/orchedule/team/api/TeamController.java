package com.orchedule.team.api;

import com.orchedule.team.api.dto.*;
import com.orchedule.team.application.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

    private final CreateTeamService createTeamService;
    private final UpdateTeamService updateTeamService;
    private final GetTeamService getTeamService;
    private final SaveTeamPreferenceService saveTeamPreferenceService;
    private final GetTeamPreferenceService getTeamPreferenceService;
    private final DeleteTeamService deleteTeamService;
    private final DeleteTeamsBulkService deleteTeamsBulkService;

    public TeamController(CreateTeamService createTeamService,
                          UpdateTeamService updateTeamService,
                          GetTeamService getTeamService,
                          SaveTeamPreferenceService saveTeamPreferenceService,
                          GetTeamPreferenceService getTeamPreferenceService,
                          DeleteTeamService deleteTeamService,
                          DeleteTeamsBulkService deleteTeamsBulkService
                          ) {
        this.createTeamService = createTeamService;
        this.updateTeamService = updateTeamService;
        this.getTeamService = getTeamService;
        this.saveTeamPreferenceService = saveTeamPreferenceService;
        this.getTeamPreferenceService = getTeamPreferenceService;
        this.deleteTeamService = deleteTeamService;
        this.deleteTeamsBulkService = deleteTeamsBulkService;
    }

    @PostMapping
    public ResponseEntity<TeamResponse> create(@Valid @RequestBody CreateTeamRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(createTeamService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<TeamResponse>> getAll() {
        return ResponseEntity.ok(getTeamService.getAll());
    }

    @GetMapping("/{teamId}")
    public ResponseEntity<TeamResponse> getById(@PathVariable UUID teamId) {
        return ResponseEntity.ok(getTeamService.getById(teamId));
    }

    @PutMapping("/{teamId}")
    public ResponseEntity<TeamResponse> update(@PathVariable UUID teamId,
                                               @Valid @RequestBody UpdateTeamRequest request) {
        return ResponseEntity.ok(updateTeamService.update(teamId, request));
    }

    @PutMapping("/{teamId}/preferences")
    public ResponseEntity<TeamPreferenceResponse> savePreference(@PathVariable UUID teamId,
                                                                 @Valid @RequestBody SaveTeamPreferenceRequest request) {
        return ResponseEntity.ok(saveTeamPreferenceService.save(teamId, request));
    }

    @DeleteMapping("/{teamId}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID teamId
    ) {
        deleteTeamService.delete(teamId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/bulk")
    public ResponseEntity<Void> deleteBulk(
            @Valid @RequestBody DeleteTeamBulkRequest request
    ) {
        deleteTeamsBulkService.delete(request.teamIds());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{teamId}/preferences")
    public ResponseEntity<TeamPreferenceResponse> getPreference(
            @PathVariable UUID teamId,
            @RequestParam UUID competitionId) {
        return ResponseEntity.ok(getTeamPreferenceService.getByTeamIdAndCompetitionId(teamId, competitionId));
    }
}