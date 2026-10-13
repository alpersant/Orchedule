package com.orchedule.competition.application;

import com.orchedule.competition.application.exception.CompetitionNotFoundException;
import com.orchedule.competition.domain.Competition;
import com.orchedule.competition.domain.CompetitionRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Deletes a competition and all data that belongs exclusively to it:
 * teams, fields/availability, generated rounds, matches and related records.
 *
 * The deletion strategy must be configured in the persistence layer:
 * either JPA cascade/orphanRemoval or database ON DELETE CASCADE.
 */

@Service
public class DeleteCompetitionService {

    private final CompetitionRepository competitionRepository;

    public DeleteCompetitionService(
            CompetitionRepository competitionRepository
    ) {
        this.competitionRepository = competitionRepository;
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public void delete(UUID id) {
        Competition competition = competitionRepository.findById(id)
                .orElseThrow(() -> new CompetitionNotFoundException(id));

        competitionRepository.delete(competition);
    }
}