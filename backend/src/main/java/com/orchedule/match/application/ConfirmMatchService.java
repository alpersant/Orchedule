package com.orchedule.match.application;

import com.orchedule.match.api.dto.MatchResponse;
import com.orchedule.match.application.exception.InvalidMatchException;
import com.orchedule.match.application.exception.MatchNotFoundException;
import com.orchedule.match.domain.Match;
import com.orchedule.match.domain.MatchRepository;
import com.orchedule.match.domain.MatchStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ConfirmMatchService {

    private final MatchRepository repo;

    public ConfirmMatchService(MatchRepository repo){ this.repo=repo; }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public MatchResponse confirm(UUID id){
        Match match = repo.findById(id).orElseThrow(() -> new MatchNotFoundException(id));
        if (match.status() == MatchStatus.CANCELLED) throw new InvalidMatchException("Cancelled match cannot be confirmed");
        Match updated = repo
                .update(
                        match.id(),
                        match.seasonId(),
                        match.homeTeamId(),
                        match.awayTeamId(),
                        match.venueId(),
                        match.scheduledAt(),
                        MatchStatus.CONFIRMED,
                        match.round(),
                        match.notes(),
                        true
                );
        return new MatchResponse(
                updated.id(),
                updated.seasonId(),
                updated.homeTeamId(),
                updated.awayTeamId(),
                updated.venueId(),
                updated.scheduledAt(),
                updated.status(),
                updated.round(),
                updated.notes(),
                updated.active()
        );
    }
}
