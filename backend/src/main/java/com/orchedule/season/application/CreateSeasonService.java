package com.orchedule.season.application;

import com.orchedule.identity.application.exception.AlreadyExistsException;
import com.orchedule.season.api.dto.CreateSeasonRequest;
import com.orchedule.season.api.dto.SeasonResponse;
import com.orchedule.season.domain.Season;
import com.orchedule.season.domain.SeasonRepository;
import com.orchedule.season.domain.SeasonStatus;
import com.orchedule.season.domain.SeasonValidator;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateSeasonService {

    private final SeasonRepository seasonRepository;
    private final com.orchedule.season.domain.CompetitionLookup competitionLookup;

    public CreateSeasonService(SeasonRepository seasonRepository, com.orchedule.season.domain.CompetitionLookup competitionLookup) {
        this.seasonRepository = seasonRepository;
        this.competitionLookup = competitionLookup;
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public SeasonResponse create(CreateSeasonRequest request) {
        if (request.competitionId() == null || !competitionLookup.exists(request.competitionId())) {
            throw new com.orchedule.season.application.exception.InvalidSeasonException("Competition does not exist");
        }
        SeasonValidator.validate(
                request.name(),
                request.startDate(),
                request.endDate(),
                SeasonStatus.DRAFT
        );

        String normalizedName = request.name().trim();

        if (seasonRepository.existsByCompetitionIdAndNameIgnoreCase(request.competitionId(), normalizedName)) {
            throw new AlreadyExistsException("Season already exists with name: " + normalizedName);
        }

        var seasonId = seasonRepository.create(
                request.competitionId(),
                normalizedName,
                request.startDate(),
                request.endDate(),
                SeasonStatus.DRAFT,
                false
        );

        Season season = seasonRepository.findById(seasonId)
                .orElseThrow(() -> new IllegalStateException("Created season could not be loaded: " + seasonId));

        return toResponse(season);
    }

    private SeasonResponse toResponse(Season season) {
        return new SeasonResponse(
                season.id(),
                season.competitionId(),
                season.name(),
                season.startDate(),
                season.endDate(),
                season.status(),
                season.active()
        );
    }
}
