package com.orchedule.scheduling.planning.update;

import com.orchedule.scheduling.planning.update.SchedulingUpdateModels.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/planning")
public class SchedulingUpdateController {
    private final SchedulingUpdateService service;

    public SchedulingUpdateController(SchedulingUpdateService service) {
        this.service = service;
    }

    @GetMapping("/seasons/{seasonId}/rules")
    public ResponseEntity<SeasonRules> seasonRules(@PathVariable UUID seasonId) {
        return ResponseEntity.ok(service.seasonRules(seasonId));
    }

    @PutMapping("/seasons/{seasonId}/rules")
    public ResponseEntity<SeasonRules> updateSeasonRules(
        @PathVariable UUID seasonId,
        @Valid @RequestBody UpdateSeasonRulesRequest request
    ) {
        return ResponseEntity.ok(service.updateSeasonRules(seasonId, request));
    }

    @PutMapping("/seasons/{seasonId}/teams/{teamId}/rules")
    public ResponseEntity<TeamRule> saveTeamRule(
        @PathVariable UUID seasonId,
        @PathVariable UUID teamId,
        @Valid @RequestBody UpdateTeamRuleRequest request
    ) {
        return ResponseEntity.ok(service.saveTeamRule(seasonId, teamId, request));
    }

    @PatchMapping("/games/{gameId}/confirm")
    public ResponseEntity<GameConfirmation> confirm(@PathVariable UUID gameId) {
        return ResponseEntity.ok(service.confirm(gameId));
    }

    @PatchMapping("/games/{gameId}/unconfirm")
    public ResponseEntity<GameConfirmation> unconfirm(@PathVariable UUID gameId) {
        return ResponseEntity.ok(service.unconfirm(gameId));
    }

    @GetMapping("/games/{gameId}/alternatives")
    public ResponseEntity<List<Alternative>> alternatives(@PathVariable UUID gameId) {
        return ResponseEntity.ok(service.alternatives(gameId));
    }

    @PostMapping("/games/{gameId}/alternatives")
    public ResponseEntity<Alternative> createAlternative(
        @PathVariable UUID gameId,
        @Valid @RequestBody CreateAlternativeRequest request
    ) {
        var created = service.createAlternative(gameId, request);
        return ResponseEntity.created(
            java.net.URI.create("/api/v1/planning/alternatives/" + created.id())
        ).body(created);
    }

    @PatchMapping("/alternatives/{alternativeId}/accept")
    public ResponseEntity<Alternative> accept(@PathVariable UUID alternativeId) {
        return ResponseEntity.ok(service.decide(alternativeId, true));
    }

    @PatchMapping("/alternatives/{alternativeId}/reject")
    public ResponseEntity<Alternative> reject(@PathVariable UUID alternativeId) {
        return ResponseEntity.ok(service.decide(alternativeId, false));
    }

    @GetMapping("/seasons/{seasonId}/history")
    public ResponseEntity<List<HistoryEntry>> history(@PathVariable UUID seasonId) {
        return ResponseEntity.ok(service.history(seasonId));
    }
}
