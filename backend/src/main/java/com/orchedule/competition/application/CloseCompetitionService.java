package com.orchedule.competition.application;

import com.orchedule.competition.application.exception.CompetitionNotFoundException;
import com.orchedule.competition.domain.Competition;
import com.orchedule.competition.domain.CompetitionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CloseCompetitionService {

    private final CompetitionRepository competitionRepository;

    public CloseCompetitionService(CompetitionRepository competitionRepository) {
        this.competitionRepository = competitionRepository;
    }

    @Transactional
    public Competition close(UUID id) {
        Competition competition = competitionRepository.findById(id)
                .orElseThrow(() -> new CompetitionNotFoundException(id));

        competition.close();
        return competitionRepository.save(competition);
    }
}
