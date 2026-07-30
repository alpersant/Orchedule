package com.orchedule.scheduling.api;

import com.orchedule.scheduling.api.dto.DayOptionDto;
import com.orchedule.scheduling.api.dto.HourOptionDto;
import com.orchedule.scheduling.api.dto.TeamScheduleProfileDto;
import com.orchedule.scheduling.application.exception.InvalidScheduleException;
import com.orchedule.scheduling.domain.DayOption;
import com.orchedule.scheduling.domain.HourOption;
import com.orchedule.scheduling.domain.SlotPriority;
import com.orchedule.scheduling.domain.TeamScheduleProfile;

import java.util.List;

public final class ScheduleMapper {

    private ScheduleMapper() {}

    public static TeamScheduleProfile toDomain(TeamScheduleProfileDto dto) {
        List<DayOption> dayOptions = dto.dayOptions().stream()
                .map(ScheduleMapper::toDayOption)
                .toList();
        List<HourOption> hourOptions = dto.hourOptions().stream()
                .map(ScheduleMapper::toHourOption)
                .toList();
        return new TeamScheduleProfile(dto.teamId(), dayOptions, hourOptions);
    }

    private static DayOption toDayOption(DayOptionDto dto) {
        return new DayOption(dto.day(), parsePriority(dto.priority()));
    }

    private static HourOption toHourOption(HourOptionDto dto) {
        return new HourOption(dto.hour(), parsePriority(dto.priority()));
    }

    private static SlotPriority parsePriority(String value) {
        try {
            return SlotPriority.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new InvalidScheduleException("Priority must be FIRST or SECOND, got: " + value);
        }
    }
}
