package com.orchedule.scheduling.application.port;

import com.orchedule.scheduling.domain.TeamScheduleProfile;

import java.util.List;
import java.util.UUID;

/**
 * Anti-Corruption Layer port: scheduling depends on this narrow interface,
 * never on team's internal domain types (Team, TeamPreference, etc). The
 * implementing adapter lives in infrastructure.integration and is
 * responsible for translating team-module concepts into
 * {@link TeamScheduleProfile}. This keeps the two Spring Modulith modules
 * decoupled — team can evolve its internal model freely as long as this
 * contract holds.
 */
public interface TeamPreferencesPort {

    /**
     * Builds a schedule profile for a single team by id.
     * @throws com.orchedule.scheduling.application.exception.InvalidScheduleException
     *         if the team does not exist or has no usable preferences
     */
    TeamScheduleProfile getProfileFor(UUID teamId);

    /** Builds schedule profiles for all teams registered in a competition/season. */
    List<TeamScheduleProfile> getProfilesForSeason(UUID seasonId);
}
