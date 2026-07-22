package com.orchedule.match.application;

import com.orchedule.match.api.dto.MatchResponse;
import com.orchedule.match.application.exception.InvalidMatchException;
import com.orchedule.match.application.exception.MatchNotFoundException;
import com.orchedule.match.domain.Match;
import com.orchedule.match.domain.MatchRepository;
import com.orchedule.match.domain.MatchStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ConfirmMatchService {
    private final MatchRepository repo;
    public ConfirmMatchService(MatchRepository repo){ this.repo=repo; }
    @Transactional
    public MatchResponse confirm(UUID id){
        Match m = repo.findById(id).orElseThrow(() -> new MatchNotFoundException(id));
        if (m.status() == MatchStatus.CANCELLED) throw new InvalidMatchException("Cancelled match cannot be confirmed");
        Match updated = repo.update(m.id(), m.seasonId(), m.homeTeamId(), m.awayTeamId(), m.venueId(), m.scheduledAt(), MatchStatus.CONFIRMED, m.round(), m.notes(), true);
        return new MatchResponse(updated.id(),updated.seasonId(),updated.homeTeamId(),updated.awayTeamId(),updated.venueId(),updated.scheduledAt(),updated.status(),updated.round(),updated.notes(),updated.active());
    }
}
