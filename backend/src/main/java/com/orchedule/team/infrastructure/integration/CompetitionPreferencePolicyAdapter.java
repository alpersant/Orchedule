package com.orchedule.team.infrastructure.integration;

import com.orchedule.competition.application.GetCompetitionService;
import com.orchedule.competition.domain.PreferencePolicy;
import com.orchedule.team.application.port.CompetitionPreferencePolicyPort;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CompetitionPreferencePolicyAdapter implements CompetitionPreferencePolicyPort {

    private final GetCompetitionService getCompetitionService;

    public CompetitionPreferencePolicyAdapter(GetCompetitionService getCompetitionService) {
        this.getCompetitionService = getCompetitionService;
    }

    @Override
    public PreferencePolicy getPolicyForCompetition(UUID competitionId) {
        return getCompetitionService.getById(competitionId).getPreferencePolicy();
    }
}
