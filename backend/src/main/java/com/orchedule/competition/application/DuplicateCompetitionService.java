package com.orchedule.competition.application;

import com.orchedule.competition.application.exception.CompetitionAlreadyExistsException;
import com.orchedule.competition.application.exception.CompetitionNotFoundException;
import com.orchedule.competition.domain.Competition;
import com.orchedule.competition.domain.CompetitionRepository;
import com.orchedule.competition.domain.CompetitionValidator;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class DuplicateCompetitionService {

    private final CompetitionRepository competitionRepository;

    public DuplicateCompetitionService(
            CompetitionRepository competitionRepository) {
        this.competitionRepository = competitionRepository;
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public Competition duplicate(UUID sourceId, String newName) {
        CompetitionValidator.validateName(newName);

        Competition source = competitionRepository.findById(sourceId)
                .orElseThrow(() -> new CompetitionNotFoundException(sourceId));

        CompetitionValidator.validateDescription(source.getDescription());

        if (competitionRepository.existsByName(newName)) {
            throw new CompetitionAlreadyExistsException(newName);
        }

        Competition copy = Competition.createWithDefaults(
                newName,
                source.getDescription()
        );

        copy.updateDefaultDays(source.getDefaultDays());
        copy.updateDefaultHours(source.getDefaultHours());
        copy.updateDefaultFieldCount(source.getDefaultFieldCount());
        copy.updatePreferencePolicy(source.getPreferencePolicy());

        return competitionRepository.save(copy);
    }
}