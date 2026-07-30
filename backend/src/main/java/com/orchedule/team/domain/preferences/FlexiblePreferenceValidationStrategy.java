package com.orchedule.team.domain.preferences;

import com.orchedule.team.application.exception.InvalidTeamPreferenceException;

/**
 * Default, generic policy — the DB-level CHECK (chk_team_preference_exclusion)
 * already guarantees exactly one of excludedDay/excludedHour is set, so this
 * strategy only guarantees the RotationEngine has enough data to work with:
 * at least one day and one hour preference recorded. No FIRST/SECOND minimums
 * enforced — keeps the platform usable for any competition shape.
 */
public class FlexiblePreferenceValidationStrategy implements TeamPreferenceValidationStrategy {

    @Override
    public void validate(TeamPreference preference) {
        if (preference.getDayPreferences().isEmpty()) {
            throw new InvalidTeamPreferenceException("At least one day preference is required");
        }
        if (preference.getHourPreferences().isEmpty()) {
            throw new InvalidTeamPreferenceException("At least one hour preference is required");
        }
    }
}
