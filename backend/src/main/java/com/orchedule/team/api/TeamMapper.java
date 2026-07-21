package com.orchedule.team.api;

import com.orchedule.team.api.dto.DayPreferenceDto;
import com.orchedule.team.api.dto.TeamPreferenceResponse;
import com.orchedule.team.api.dto.TeamResponse;
import com.orchedule.team.api.dto.TimePreferenceDto;
import com.orchedule.team.domain.Team;
import com.orchedule.team.domain.TeamPreference;

public final class TeamMapper {

    private TeamMapper() {
    }

    public static TeamResponse toResponse(Team team) {
        return new TeamResponse(team.id(), team.name(), team.active());
    }

    public static TeamPreferenceResponse toResponse(TeamPreference preference) {
        return new TeamPreferenceResponse(
                preference.teamId(),
                preference.restrictionType(),
                preference.excludedDay(),
                preference.excludedHour(),
                preference.dayPreferences().stream()
                        .map(p -> new DayPreferenceDto(p.day(), p.priority()))
                        .toList(),
                preference.timePreferences().stream()
                        .map(p -> new TimePreferenceDto(p.hour(), p.priority()))
                        .toList()
        );
    }
}