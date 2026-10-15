package com.orchedule.season.application;

import com.orchedule.identity.application.exception.AlreadyExistsException;
import com.orchedule.season.api.dto.SeasonResponse;
import com.orchedule.season.api.dto.UpdateSeasonRequest;
import com.orchedule.season.application.exception.SeasonNotFoundException;
import com.orchedule.season.domain.Season;
import com.orchedule.season.domain.SeasonRepository;
import com.orchedule.season.domain.SeasonValidator;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UpdateSeasonService {

    private final SeasonRepository seasonRepository;

    public UpdateSeasonService(SeasonRepository seasonRepository) {
        this.seasonRepository = seasonRepository;
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public SeasonResponse update(UUID seasonId, UpdateSeasonRequest request) {
        Season currentSeason = seasonRepository.findById(seasonId)
                .orElseThrow(() -> new SeasonNotFoundException(seasonId));

        if (request.active() == null || (currentSeason.status() == com.orchedule.season.domain.SeasonStatus.CLOSED && request.active())) {
            throw new com.orchedule.season.application.exception.InvalidSeasonException("Closed seasons cannot be enabled");
        }
        SeasonValidator.validate(
                request.name(),
                request.startDate(),
                request.endDate(),
                currentSeason.status()
        );

        String normalizedName = request.name().trim();

        boolean nameChanged = !currentSeason.name().equalsIgnoreCase(normalizedName);
        if (nameChanged && seasonRepository.existsByCompetitionIdAndNameIgnoreCase(currentSeason.competitionId(), normalizedName)) {
            throw new AlreadyExistsException("Season already exists with name: " + normalizedName);
        }

        Season updatedSeason = seasonRepository.update(
                seasonId,
                normalizedName,
                request.startDate(),
                request.endDate(),
                currentSeason.status(),
                request.active()
        );

        return toResponse(updatedSeason);
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
