package com.orchedule.match.application;

import com.orchedule.match.api.dto.MatchResponse;
import com.orchedule.match.api.dto.UpdateMatchRequest;
import com.orchedule.match.application.exception.MatchNotFoundException;
import com.orchedule.match.domain.Match;
import com.orchedule.match.domain.MatchRepository;
import com.orchedule.match.domain.MatchStatus;
import com.orchedule.match.domain.MatchValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UpdateMatchService {
    private final MatchRepository repo;
    public UpdateMatchService(MatchRepository repo){ this.repo=repo; }
    @Transactional
    public MatchResponse update(UUID id, UpdateMatchRequest request) {
        Match current = repo.findById(id).orElseThrow(() -> new MatchNotFoundException(id));
        MatchStatus status = MatchStatus.valueOf(request.status().trim().toUpperCase());
        MatchValidator.validate(request.seasonId(), request.homeTeamId(), request.awayTeamId(), request.venueId(), request.scheduledAt(), status, request.round(), request.notes());
        Match updated = repo.update(id, request.seasonId(), request.homeTeamId(), request.awayTeamId(), request.venueId(), request.scheduledAt(), status, request.round(), clean(request.notes()), request.active());
        return toResponse(updated);
    }
    private String clean(String n){ return n==null?null:n.trim(); }
    private MatchResponse toResponse(Match m){ return new MatchResponse(m.id(),m.seasonId(),m.homeTeamId(),m.awayTeamId(),m.venueId(),m.scheduledAt(),m.status(),m.round(),m.notes(),m.active()); }
}
