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

    public CreateSeasonService(SeasonRepository seasonRepository) {
        this.seasonRepository = seasonRepository;
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public SeasonResponse create(CreateSeasonRequest request) {
        SeasonValidator.validate(
                request.name(),
                request.startDate(),
                request.endDate(),
                SeasonStatus.DRAFT
        );

        String normalizedName = request.name().trim();

        if (seasonRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new AlreadyExistsException("Season already exists with name: " + normalizedName);
        }

        var seasonId = seasonRepository.create(
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
                season.name(),
                season.startDate(),
                season.endDate(),
                season.status(),
                season.active()
        );
    }
}