package com.orchedule.scheduling.infrastructure.integration;

import com.orchedule.scheduling.application.exception.InvalidScheduleException;
import com.orchedule.scheduling.application.port.TeamPreferencesPort;
import com.orchedule.scheduling.domain.DayOption;
import com.orchedule.scheduling.domain.HourOption;
import com.orchedule.scheduling.domain.SlotPriority;
import com.orchedule.scheduling.domain.TeamScheduleProfile;
import com.orchedule.team.application.GetTeamPreferencesService;
import com.orchedule.team.domain.TeamDayPreference;
import com.orchedule.team.domain.TeamHourPreference;
import com.orchedule.team.domain.TeamPreference;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Anti-Corruption Layer adapter: translates team-module TeamPreferences
 * (day/hour + primary flag) into scheduling's own TeamScheduleProfile.
 * Depends only on team's PUBLIC application service (GetTeamPreferencesService),
 * never on team's repositories or JPA entities.
 *
 * Fixed: previously assumed Team itself carried getDayPreferences() /
 * getHourPreferences() — Team.java is actually a minimal identity record.
 * Preferences live in the separate TeamPreferences aggregate, keyed by
 * (teamId, competitionId), which is what this adapter now queries.
 */
@Component
public class TeamPreferencesAdapter implements TeamPreferencesPort {

    private final GetTeamPreferencesService getTeamPreferencesService;

    public TeamPreferencesAdapter(GetTeamPreferencesService getTeamPreferencesService) {
        this.getTeamPreferencesService = getTeamPreferencesService;
    }

    @Override
    public TeamScheduleProfile getProfileFor(UUID teamId) {
        throw new UnsupportedOperationException(
                "getProfileFor(teamId) requires a competitionId to resolve TeamPreferences; "
                        + "use getProfilesForSeason(seasonId) instead");
    }

    @Override
    public List<TeamScheduleProfile> getProfilesForSeason(UUID seasonId) {
        // seasonId == competitionId in the current model (see SeasonContextAdapter note)
        List<TeamPreference> allPreferences = getTeamPreferencesService.getByCompetition(seasonId);
        if (allPreferences.isEmpty()) {
            throw new InvalidScheduleException(
                    "Competition " + seasonId + " has no team preferences registered — cannot schedule");
        }
        return allPreferences.stream().map(this::toProfileWithContext).toList();
    }

    private TeamScheduleProfile toProfileWithContext(TeamPreference preferences) {
        try {
            return toProfile(preferences);
        } catch (RuntimeException ex) {
            throw new InvalidScheduleException(
                    "Team " + preferences.getTeamId() + " has invalid or missing day/hour preferences "
                            + "and cannot be scheduled: " + ex.getMessage());
        }
    }

    private TeamScheduleProfile toProfile(TeamPreference preferences) {
        List<DayOption> dayOptions = preferences.getDayPreferences().stream()
                .map(this::toDayOption)
                .toList();
        List<HourOption> hourOptions = preferences.getHourPreferences().stream()
                .map(this::toHourOption)
                .toList();
        return new TeamScheduleProfile(preferences.getTeamId(), dayOptions, hourOptions);
    }

    private DayOption toDayOption(TeamDayPreference preference) {
        return new DayOption(preference.day(), toSlotPriority(preference.primary()));
    }

    private HourOption toHourOption(TeamHourPreference preference) {
        return new HourOption(preference.hour(), toSlotPriority(preference.primary()));
    }

    private SlotPriority toSlotPriority(boolean primary) {
        return primary ? SlotPriority.FIRST : SlotPriority.SECOND;
    }
}
