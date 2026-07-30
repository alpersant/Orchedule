package com.orchedule.team.domain.preferences;

import com.orchedule.competition.domain.PreferencePolicy;
import org.springframework.stereotype.Component;

@Component
public class TeamPreferenceValidatorResolver {

    private final FlexiblePreferenceValidationStrategy flexibleStrategy = new FlexiblePreferenceValidationStrategy();
    private final StrictSingleEliminationValidationStrategy strictStrategy = new StrictSingleEliminationValidationStrategy();

    public TeamPreferenceValidationStrategy resolve(PreferencePolicy policy) {
        return switch (policy) {
            case FLEXIBLE -> flexibleStrategy;
            case STRICT_SINGLE_ELIMINATION -> strictStrategy;
        };
    }
}
