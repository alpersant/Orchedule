package com.orchedule.match.application;

import com.orchedule.match.api.dto.MatchResponse;
import com.orchedule.match.application.exception.MatchNotFoundException;
import com.orchedule.match.domain.MatchRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class GetMatchService {
    private final MatchRepository repo;
    public GetMatchService(MatchRepository repo){ this.repo=repo; }
    @Transactional(readOnly = true)
    public MatchResponse getById(UUID id){
        var m = repo.findById(id).orElseThrow(() -> new MatchNotFoundException(id));
        return new MatchResponse(m.id(),m.seasonId(),m.homeTeamId(),m.awayTeamId(),m.venueId(),m.scheduledAt(),m.status(),m.round(),m.notes(),m.active());
    }
    @Transactional(readOnly = true)
    public List<MatchResponse> getAll(){ return repo.findAll().stream().map(m -> new MatchResponse(m.id(),m.seasonId(),m.homeTeamId(),m.awayTeamId(),m.venueId(),m.scheduledAt(),m.status(),m.round(),m.notes(),m.active())).toList(); }
    @Transactional(readOnly = true)
    public List<MatchResponse> getBySeason(UUID seasonId){ return repo.findBySeasonId(seasonId).stream().map(m -> new MatchResponse(m.id(),m.seasonId(),m.homeTeamId(),m.awayTeamId(),m.venueId(),m.scheduledAt(),m.status(),m.round(),m.notes(),m.active())).toList(); }
}
