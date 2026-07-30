package com.orchedule.team.domain.preferences;

/**
 * Strategy pattern: how a TeamPreference is validated depends on the
 * competition's PreferencePolicy (see com.orchedule.competition.domain.PreferencePolicy).
 * TeamPreference itself stays a plain data aggregate with no policy branching.
 */
public interface TeamPreferenceValidationStrategy {

    void validate(TeamPreference preference);
}
