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

    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public com.orchedule.season.api.dto.SeasonPageResponse search(UUID competitionId, String search,
            com.orchedule.season.domain.SeasonStatus status, int page, int size) {
        if (page < 0 || size < 1 || size > 100 || (search != null && search.length() > 150)) {
            throw new com.orchedule.season.application.exception.InvalidSeasonException("Invalid pagination or search");
        }
        var result = seasonRepository.search(competitionId, search, status,
            org.springframework.data.domain.PageRequest.of(page, size,
                org.springframework.data.domain.Sort.by("startDate").descending().and(
                    org.springframework.data.domain.Sort.by("id"))));
        return new com.orchedule.season.api.dto.SeasonPageResponse(
            result.map(this::toResponse).getContent(), result.getNumber(), result.getSize(),
            result.getTotalElements(), result.getTotalPages(), result.isLast());
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
