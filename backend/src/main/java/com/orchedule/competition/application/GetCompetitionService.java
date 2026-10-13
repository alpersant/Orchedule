package com.orchedule.competition.application;

import com.orchedule.competition.application.exception.CompetitionNotFoundException;
import com.orchedule.competition.domain.Competition;
import com.orchedule.competition.domain.CompetitionRepository;
import com.orchedule.competition.domain.CompetitionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class GetCompetitionService {

    private static final int MAX_PAGE_SIZE = 100;
    private final CompetitionRepository competitionRepository;

    public GetCompetitionService(CompetitionRepository competitionRepository) {
        this.competitionRepository = competitionRepository;
    }

    @PreAuthorize("isAuthenticated()")
    public Competition getById(UUID id) {
        return competitionRepository.findById(id)
                .orElseThrow(() -> new CompetitionNotFoundException(id));
    }

    @PreAuthorize("isAuthenticated()")
    public List<Competition> getAll() {
        return competitionRepository.findAll();
    }

    @PreAuthorize("isAuthenticated()")
    public List<Competition> getByStatus(CompetitionStatus status) {
        return competitionRepository.findByStatus(status);
    }

    @PreAuthorize("isAuthenticated()")
    public Page<Competition> search(int page, int size, CompetitionStatus status, String search) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.clamp(size, 1, MAX_PAGE_SIZE);
        String normalizedSearch = search == null || search.isBlank() ? null : search.trim();

        return competitionRepository.search(
                status, normalizedSearch,
                PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt")));
    }
}
