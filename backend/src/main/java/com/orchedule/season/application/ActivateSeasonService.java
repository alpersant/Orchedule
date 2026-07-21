package com.orchedule.season.application;

import com.orchedule.season.api.dto.SeasonResponse;
import com.orchedule.season.application.exception.InvalidSeasonException;
import com.orchedule.season.application.exception.SeasonNotFoundException;
import com.orchedule.season.domain.Season;
import com.orchedule.season.domain.SeasonRepository;
import com.orchedule.season.domain.SeasonStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ActivateSeasonService {

    private final SeasonRepository seasonRepository;

    public ActivateSeasonService(SeasonRepository seasonRepository) {
        this.seasonRepository = seasonRepository;
    }

    @Transactional
    public SeasonResponse activate(UUID seasonId) {
        Season season = seasonRepository.findById(seasonId)
                .orElseThrow(() -> new SeasonNotFoundException(seasonId));

        if (season.status() == SeasonStatus.CLOSED) {
            throw new InvalidSeasonException("A closed season cannot be activated");
        }

        if (season.status() == SeasonStatus.ACTIVE && season.active()) {
            throw new InvalidSeasonException("Season is already active");
        }

        Season updatedSeason = seasonRepository.update(
                season.id(),
                season.name(),
                season.startDate(),
                season.endDate(),
                SeasonStatus.ACTIVE,
                true
        );

        return toResponse(updatedSeason);
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