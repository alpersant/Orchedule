package com.orchedule.match.api;

import com.orchedule.match.api.dto.CreateMatchRequest;
import com.orchedule.match.api.dto.MatchResponse;
import com.orchedule.match.api.dto.UpdateMatchRequest;
import com.orchedule.match.application.CancelMatchService;
import com.orchedule.match.application.ConfirmMatchService;
import com.orchedule.match.application.CreateMatchService;
import com.orchedule.match.application.GetMatchService;
import com.orchedule.match.application.UpdateMatchService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/matches")
public class MatchController {
    private final CreateMatchService createMatchService;
    private final UpdateMatchService updateMatchService;
    private final GetMatchService getMatchService;
    private final ConfirmMatchService confirmMatchService;
    private final CancelMatchService cancelMatchService;

    public MatchController(CreateMatchService createMatchService, UpdateMatchService updateMatchService, GetMatchService getMatchService, ConfirmMatchService confirmMatchService, CancelMatchService cancelMatchService) {
        this.createMatchService = createMatchService; this.updateMatchService = updateMatchService; this.getMatchService = getMatchService; this.confirmMatchService = confirmMatchService; this.cancelMatchService = cancelMatchService;
    }

    @PostMapping
    public ResponseEntity<MatchResponse> create(@Valid @RequestBody CreateMatchRequest request){
        MatchResponse response = createMatchService.create(request);
        return ResponseEntity.created(URI.create("/api/v1/matches/" + response.id())).body(response);
    }

    @PutMapping("/{matchId}")
    public ResponseEntity<MatchResponse> update(@PathVariable UUID matchId, @Valid @RequestBody UpdateMatchRequest request){
        return ResponseEntity.ok(updateMatchService.update(matchId, request));
    }

    @GetMapping("/{matchId}")
    public ResponseEntity<MatchResponse> getById(@PathVariable UUID matchId){ return ResponseEntity.ok(getMatchService.getById(matchId)); }

    @GetMapping
    public ResponseEntity<List<MatchResponse>> getAll(){ return ResponseEntity.ok(getMatchService.getAll()); }

    @GetMapping("/season/{seasonId}")
    public ResponseEntity<List<MatchResponse>> getBySeason(@PathVariable UUID seasonId){ return ResponseEntity.ok(getMatchService.getBySeason(seasonId)); }

    @PatchMapping("/{matchId}/confirm")
    public ResponseEntity<MatchResponse> confirm(@PathVariable UUID matchId){ return ResponseEntity.ok(confirmMatchService.confirm(matchId)); }

    @PatchMapping("/{matchId}/cancel")
    public ResponseEntity<MatchResponse> cancel(@PathVariable UUID matchId){ return ResponseEntity.ok(cancelMatchService.cancel(matchId)); }
}
