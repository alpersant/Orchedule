package com.orchedule.competition.application;

import com.orchedule.competition.application.exception.CompetitionNotFoundException;
import com.orchedule.competition.domain.Competition;
import com.orchedule.competition.domain.CompetitionDay;
import com.orchedule.competition.domain.CompetitionHour;
import com.orchedule.competition.domain.CompetitionRepository;
import com.orchedule.competition.domain.CompetitionValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

/**
 * Updates the global scheduling defaults of a competition: which days of the
 * week are played by default (e.g. Monday-Friday) and which hours are open
 * by default (e.g. 18:00-22:00), plus the default number of fields.
 */
@Service
public class UpdateCompetitionDefaultsService {

    private final CompetitionRepository competitionRepository;

    public UpdateCompetitionDefaultsService(CompetitionRepository competitionRepository) {
        this.competitionRepository = competitionRepository;
    }

    @Transactional
    public Competition updateDefaults(UUID id, Set<CompetitionDay> defaultDays,
                                       Set<CompetitionHour> defaultHours, int defaultFieldCount) {
        CompetitionValidator.validateDefaultDays(defaultDays);
        CompetitionValidator.validateDefaultHours(defaultHours);
        CompetitionValidator.validateDefaultFieldCount(defaultFieldCount);

        Competition competition = competitionRepository.findById(id)
                .orElseThrow(() -> new CompetitionNotFoundException(id));

        competition.updateDefaultDays(defaultDays);
        competition.updateDefaultHours(defaultHours);
        competition.updateDefaultFieldCount(defaultFieldCount);

        return competitionRepository.save(competition);
    }
}
