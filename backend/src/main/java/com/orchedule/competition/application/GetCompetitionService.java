package com.orchedule.competition.application;

import com.orchedule.competition.application.exception.CompetitionNotFoundException;
import com.orchedule.competition.domain.Competition;
import com.orchedule.competition.domain.CompetitionRepository;
import com.orchedule.competition.domain.CompetitionStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class GetCompetitionService {

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
}
