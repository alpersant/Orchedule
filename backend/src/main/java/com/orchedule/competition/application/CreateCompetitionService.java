package com.orchedule.competition.application;

import com.orchedule.competition.application.exception.CompetitionAlreadyExistsException;
import com.orchedule.competition.domain.Competition;
import com.orchedule.competition.domain.CompetitionRepository;
import com.orchedule.competition.domain.CompetitionValidator;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateCompetitionService {

    private final CompetitionRepository competitionRepository;

    public CreateCompetitionService(CompetitionRepository competitionRepository) {
        this.competitionRepository = competitionRepository;
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public Competition create(String name, String description) {
        CompetitionValidator.validateName(name);
        CompetitionValidator.validateDescription(description);

        if (competitionRepository.existsByName(name)) {
            throw new CompetitionAlreadyExistsException(name);
        }

        Competition competition = Competition.createWithDefaults(name, description);
        return competitionRepository.save(competition);
    }
}
