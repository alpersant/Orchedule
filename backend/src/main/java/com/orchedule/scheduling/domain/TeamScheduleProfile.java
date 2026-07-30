package com.orchedule.scheduling.domain;

import com.orchedule.scheduling.application.exception.InvalidScheduleException;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

/**
 * Input value object representing a team's rotation preferences for a
 * scheduling run: which days/hours it favors (FIRST) and which are only
 * acceptable as fallback (SECOND). Built by the caller from team module
 * data — scheduling never depends on team's domain types directly.
 */
public final class TeamScheduleProfile {

    private final UUID teamId;
    private final List<DayOption> dayOptions;
    private final List<HourOption> hourOptions;

    public TeamScheduleProfile(UUID teamId, List<DayOption> dayOptions, List<HourOption> hourOptions) {
        if (teamId == null) {
            throw new InvalidScheduleException("Team id must not be null in schedule profile");
        }
        if (dayOptions == null || dayOptions.isEmpty()) {
            throw new InvalidScheduleException("Team " + teamId + " must have at least one available day");
        }
        if (hourOptions == null || hourOptions.isEmpty()) {
            throw new InvalidScheduleException("Team " + teamId + " must have at least one available hour");
        }
        this.teamId = teamId;
        this.dayOptions = List.copyOf(dayOptions);
        this.hourOptions = List.copyOf(hourOptions);
    }

    public List<DayOfWeek> daysWithPriority(SlotPriority priority) {
        return dayOptions.stream()
                .filter(option -> option.priority() == priority)
                .map(DayOption::day)
                .toList();
    }

    public List<LocalTime> hoursWithPriority(SlotPriority priority) {
        return hourOptions.stream()
                .filter(option -> option.priority() == priority)
                .map(HourOption::hour)
                .toList();
    }

    public UUID getTeamId() { return teamId; }
    public List<DayOption> getDayOptions() { return dayOptions; }
    public List<HourOption> getHourOptions() { return hourOptions; }
}
