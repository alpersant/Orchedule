package com.orchedule.competition.application;

import com.orchedule.competition.application.exception.CompetitionAlreadyExistsException;
import com.orchedule.competition.application.exception.CompetitionNotFoundException;
import com.orchedule.competition.domain.Competition;
import com.orchedule.competition.domain.CompetitionRepository;
import com.orchedule.competition.domain.CompetitionValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UpdateCompetitionService {

    private final CompetitionRepository competitionRepository;

    public UpdateCompetitionService(CompetitionRepository competitionRepository) {
        this.competitionRepository = competitionRepository;
    }

    @Transactional
    public Competition update(UUID id, String name, String description) {
        CompetitionValidator.validateName(name);
        CompetitionValidator.validateDescription(description);

        Competition competition = competitionRepository.findById(id)
                .orElseThrow(() -> new CompetitionNotFoundException(id));

        if (!competition.getName().equals(name) && competitionRepository.existsByName(name)) {
            throw new CompetitionAlreadyExistsException(name);
        }

        competition.updateDetails(name, description);
        return competitionRepository.save(competition);
    }
}
