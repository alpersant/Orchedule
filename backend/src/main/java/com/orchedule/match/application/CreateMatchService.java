package com.orchedule.match.application;

import com.orchedule.match.api.dto.CreateMatchRequest;
import com.orchedule.match.api.dto.MatchResponse;
import com.orchedule.match.domain.Match;
import com.orchedule.match.domain.MatchRepository;
import com.orchedule.match.domain.MatchStatus;
import com.orchedule.match.domain.MatchValidator;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateMatchService {

    private final MatchRepository repo;

    public CreateMatchService(MatchRepository repo) { this.repo = repo; }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public MatchResponse create(CreateMatchRequest request) {

        MatchStatus status = MatchStatus.valueOf(request.status().trim().toUpperCase());
        MatchValidator
                .validate(
                        request.seasonId(),
                        request.homeTeamId(),
                        request.awayTeamId(),
                        request.venueId(),
                        request.scheduledAt(),
                        status,
                        request.round(),
                        request.notes()
                );
        if (repo
                .existsBySeasonIdAndHomeTeamIdAndAwayTeamIdAndScheduledAt(
                        request.seasonId(),
                        request.homeTeamId(),
                        request.awayTeamId(),
                        request.scheduledAt()
                )
        ) {
            throw new IllegalStateException("Match already exists for this season, teams and schedule");
        }
        var id = repo.create(
                request.seasonId(),
                request.homeTeamId(),
                request.awayTeamId(),
                request.venueId(),
                request.scheduledAt(),
                status,
                request.round(),
                clean(request.notes()),
                true
        );
        Match m = repo.findById(id).orElseThrow(() -> new IllegalStateException("Created match could not be loaded: " + id));
        return toResponse(m);
    }

    private String clean(String n){ return n==null?null:n.trim(); }

    private MatchResponse toResponse(Match m){
        return new MatchResponse(
                m.id(),
                m.seasonId(),
                m.homeTeamId(),
                m.awayTeamId(),
                m.venueId(),
                m.scheduledAt(),
                m.status(),
                m.round(),
                m.notes(),
                m.active()
        );
    }
}
