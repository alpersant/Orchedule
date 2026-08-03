package com.orchedule.season.application;

import com.orchedule.season.api.dto.SeasonResponse;
import com.orchedule.season.application.exception.SeasonNotFoundException;
import com.orchedule.season.domain.Season;
import com.orchedule.season.domain.SeasonRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class GetSeasonService {

    private final SeasonRepository seasonRepository;

    public GetSeasonService(SeasonRepository seasonRepository) {
        this.seasonRepository = seasonRepository;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public SeasonResponse getById(UUID seasonId) {
        Season season = seasonRepository.findById(seasonId)
                .orElseThrow(() -> new SeasonNotFoundException(seasonId));

        return toResponse(season);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public List<SeasonResponse> getAll() {
        return seasonRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
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