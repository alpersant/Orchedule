package com.orchedule.scheduling.infrastructure.integration;

import com.orchedule.scheduling.application.exception.InvalidScheduleException;
import com.orchedule.scheduling.application.port.SeasonContextPort;
import com.orchedule.scheduling.application.port.TeamPreferencesPort;
import com.orchedule.scheduling.domain.DayOption;
import com.orchedule.scheduling.domain.HourOption;
import com.orchedule.scheduling.domain.SlotPriority;
import com.orchedule.scheduling.domain.TeamScheduleProfile;
import com.orchedule.team.application.GetAllTeamPreferencesService;
import com.orchedule.team.domain.DayPreference;
import com.orchedule.team.domain.MatchDay;
import com.orchedule.team.domain.MatchHour;
import com.orchedule.team.domain.PreferencePriority;
import com.orchedule.team.domain.TeamPreference;
import com.orchedule.team.domain.TimePreference;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Component
public class SchedulingTeamPreferencesAdapter implements TeamPreferencesPort {

    private final GetAllTeamPreferencesService getAllTeamPreferencesService;
    private final SeasonContextPort seasonContextPort;

    public SchedulingTeamPreferencesAdapter(GetAllTeamPreferencesService getAllTeamPreferencesService,
                                            SeasonContextPort seasonContextPort) {
        this.getAllTeamPreferencesService = getAllTeamPreferencesService;
        this.seasonContextPort = seasonContextPort;
    }

    @Override
    public TeamScheduleProfile getProfileFor(UUID teamId) {
        throw new UnsupportedOperationException(
                "getProfileFor(teamId) requires a competitionId to resolve TeamPreference; "
                        + "use getProfilesForSeason(seasonId) instead");
    }

    @Override
    public List<TeamScheduleProfile> getProfilesForSeason(UUID seasonId) {
        UUID competitionId = seasonContextPort.getCompetitionIdForSeason(seasonId);
        List<TeamPreference> allPreferences = getAllTeamPreferencesService.getForAllActiveTeams(competitionId);
        if (allPreferences.isEmpty()) {
            throw new InvalidScheduleException(
                    "No team preferences registered — cannot schedule season " + seasonId);
        }
        return allPreferences.stream().map(this::toProfileWithContext).toList();
    }

    private TeamScheduleProfile toProfileWithContext(TeamPreference preference) {
        try {
            return toProfile(preference);
        } catch (RuntimeException ex) {
            throw new InvalidScheduleException(
                    "Team " + preference.getTeamId() + " has invalid or missing day/hour preferences "
                            + "and cannot be scheduled: " + ex.getMessage());
        }
    }

    private TeamScheduleProfile toProfile(TeamPreference preference) {
        List<DayOption> dayOptions = preference.getDayPreferences().stream()
                .map(this::toDayOption)
                .toList();
        List<HourOption> hourOptions = preference.getTimePreferences().stream()
                .map(this::toHourOption)
                .toList();
        return new TeamScheduleProfile(preference.getTeamId(), dayOptions, hourOptions);
    }

    private DayOption toDayOption(DayPreference preference) {
        return new DayOption(toDayOfWeek(preference.day()), toSlotPriority(preference.priority()));
    }

    private HourOption toHourOption(TimePreference preference) {
        return new HourOption(toLocalTime(preference.hour()), toSlotPriority(preference.priority()));
    }

    private DayOfWeek toDayOfWeek(MatchDay matchDay) {
        return DayOfWeek.valueOf(matchDay.name());
    }

    private LocalTime toLocalTime(MatchHour matchHour) {
        return switch (matchHour) {
            case H18_00 -> LocalTime.of(18, 0);
            case H19_00 -> LocalTime.of(19, 0);
            case H20_00 -> LocalTime.of(20, 0);
            case H21_00 -> LocalTime.of(21, 0);
            case H22_00 -> LocalTime.of(22, 0);
        };
    }

    private SlotPriority toSlotPriority(PreferencePriority priority) {
        return switch (priority) {
            case FIRST -> SlotPriority.FIRST;
            case SECOND -> SlotPriority.SECOND;
        };
    }
}
