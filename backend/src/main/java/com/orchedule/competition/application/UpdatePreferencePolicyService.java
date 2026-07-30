package com.orchedule.competition.application;

import com.orchedule.competition.application.exception.CompetitionNotFoundException;
import com.orchedule.competition.application.exception.InvalidCompetitionException;
import com.orchedule.competition.domain.Competition;
import com.orchedule.competition.domain.CompetitionRepository;
import com.orchedule.competition.domain.PreferencePolicy;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Lets an organizer explicitly opt a competition into the strict
 * single-elimination preference rule, or keep the flexible default. This
 * is the ONLY place that rule can be turned on — never inferred or
 * defaulted to STRICT, keeping the platform generic out of the box.
 */
@Service
public class UpdatePreferencePolicyService {

    private final CompetitionRepository competitionRepository;

    public UpdatePreferencePolicyService(CompetitionRepository competitionRepository) {
        this.competitionRepository = competitionRepository;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    @Transactional
    public Competition updatePolicy(UUID competitionId, String policyName) {
        Competition competition = competitionRepository.findById(competitionId)
                .orElseThrow(() -> new CompetitionNotFoundException(competitionId));

        PreferencePolicy policy;
        try {
            policy = PreferencePolicy.valueOf(policyName.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new InvalidCompetitionException(
                    "Unknown preference policy: " + policyName + ". Valid values: FLEXIBLE, STRICT_SINGLE_ELIMINATION");
        }

        competition.updatePreferencePolicy(policy);
        return competitionRepository.save(competition);
    }
}
