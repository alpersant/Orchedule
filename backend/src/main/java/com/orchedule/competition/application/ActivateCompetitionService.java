package com.orchedule.competition.application;

import com.orchedule.competition.application.exception.CompetitionNotFoundException;
import com.orchedule.competition.application.exception.InvalidCompetitionStateException;
import com.orchedule.competition.domain.Competition;
import com.orchedule.competition.domain.CompetitionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ActivateCompetitionService {

    private final CompetitionRepository competitionRepository;

    public ActivateCompetitionService(CompetitionRepository competitionRepository) {
        this.competitionRepository = competitionRepository;
    }

    @Transactional
    public Competition activate(UUID id) {
        Competition competition = competitionRepository.findById(id)
                .orElseThrow(() -> new CompetitionNotFoundException(id));

        try {
            competition.activate();
        } catch (IllegalStateException ex) {
            throw new InvalidCompetitionStateException(ex.getMessage());
        }

        return competitionRepository.save(competition);
    }
}
