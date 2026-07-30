package com.orchedule.scheduling.infrastructure.integration;

import com.orchedule.scheduling.application.exception.InvalidScheduleException;
import com.orchedule.scheduling.application.port.TeamPreferencesPort;
import com.orchedule.scheduling.domain.DayOption;
import com.orchedule.scheduling.domain.HourOption;
import com.orchedule.scheduling.domain.SlotPriority;
import com.orchedule.scheduling.domain.TeamScheduleProfile;
import com.orchedule.team.application.GetTeamService;
import com.orchedule.team.application.ListTeamsBySeasonService;
import com.orchedule.team.domain.Team;
import com.orchedule.team.domain.TeamDayPreference;
import com.orchedule.team.domain.TeamHourPreference;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Anti-Corruption Layer adapter: translates team-module concepts into
 * scheduling's own TeamScheduleProfile. Depends only on team's PUBLIC
 * application services (GetTeamService, ListTeamsBySeasonService), never
 * on team's repositories or JPA entities.
 *
 * Fixed: getProfilesForSeason previously mapped every team unconditionally
 * via toProfile(), meaning a single team with empty preferences would blow
 * up the ENTIRE round generation with a generic exception, and it was
 * unclear which team caused it. Now each team is validated individually
 * with an explicit, actionable error message before the round is assembled.
 */
@Component
public class TeamPreferencesAdapter implements TeamPreferencesPort {

    private final GetTeamService getTeamService;
    private final ListTeamsBySeasonService listTeamsBySeasonService;

    public TeamPreferencesAdapter(GetTeamService getTeamService,
                                   ListTeamsBySeasonService listTeamsBySeasonService) {
        this.getTeamService = getTeamService;
        this.listTeamsBySeasonService = listTeamsBySeasonService;
    }

    @Override
    public TeamScheduleProfile getProfileFor(UUID teamId) {
        Team team = getTeamService.getById(teamId);
        return toProfile(team);
    }

    @Override
    public List<TeamScheduleProfile> getProfilesForSeason(UUID seasonId) {
        List<Team> teams = listTeamsBySeasonService.listBySeason(seasonId);
        if (teams.isEmpty()) {
            throw new InvalidScheduleException("Season " + seasonId + " has no registered teams to schedule");
        }
        return teams.stream().map(this::toProfileWithContext).toList();
    }

    private TeamScheduleProfile toProfileWithContext(Team team) {
        try {
            return toProfile(team);
        } catch (RuntimeException ex) {
            throw new InvalidScheduleException(
                    "Team " + team.getId() + " (" + team.getName() + ") has invalid or missing "
                            + "day/hour preferences and cannot be scheduled: " + ex.getMessage());
        }
    }

    private TeamScheduleProfile toProfile(Team team) {
        Objects.requireNonNull(team, "team must not be null");

        List<DayOption> dayOptions = team.getDayPreferences().stream()
                .map(this::toDayOption)
                .toList();
        List<HourOption> hourOptions = team.getHourPreferences().stream()
                .map(this::toHourOption)
                .toList();
        return new TeamScheduleProfile(team.getId(), dayOptions, hourOptions);
    }

    private DayOption toDayOption(TeamDayPreference preference) {
        return new DayOption(preference.getDay(), toSlotPriority(preference.isPrimary()));
    }

    private HourOption toHourOption(TeamHourPreference preference) {
        return new HourOption(preference.getHour(), toSlotPriority(preference.isPrimary()));
    }

    private SlotPriority toSlotPriority(boolean primary) {
        return primary ? SlotPriority.FIRST : SlotPriority.SECOND;
    }
}
