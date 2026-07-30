package com.orchedule.team.application.port;

import com.orchedule.competition.domain.PreferencePolicy;

import java.util.UUID;

public interface CompetitionPreferencePolicyPort {
    PreferencePolicy getPolicyForCompetition(UUID competitionId);
}
