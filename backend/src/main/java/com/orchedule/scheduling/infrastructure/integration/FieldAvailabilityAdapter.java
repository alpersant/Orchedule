package com.orchedule.scheduling.infrastructure.integration;

import com.orchedule.field.application.ListFieldsByCompetitionService;
import com.orchedule.field.domain.Field;
import com.orchedule.scheduling.application.exception.InvalidScheduleException;
import com.orchedule.scheduling.application.port.FieldAvailabilityPort;
import com.orchedule.scheduling.application.port.FieldSchedulingContext;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * ACL adapter translating field-module data into the shape scheduling
 * needs. Fetches the active field list exactly ONCE per call and derives
 * ids/days/hours from that single result set (fixes a previous N+1: the
 * old three-method port issued the same query three times).
 */
@Component
public class FieldAvailabilityAdapter implements FieldAvailabilityPort {

    private final ListFieldsByCompetitionService listFieldsByCompetitionService;

    public FieldAvailabilityAdapter(ListFieldsByCompetitionService listFieldsByCompetitionService) {
        this.listFieldsByCompetitionService = listFieldsByCompetitionService;
    }

    @Override
    public FieldSchedulingContext getSchedulingContext(UUID competitionId) {
        List<Field> fields = listFieldsByCompetitionService.listActiveByCompetition(competitionId);
        if (fields.isEmpty()) {
            throw new InvalidScheduleException(
                    "Competition " + competitionId + " has no active fields available for scheduling");
        }

        List<UUID> fieldIds = fields.stream().map(Field::getId).toList();

        Set<DayOfWeek> openDays = fields.stream()
                .flatMap(field -> field.getWeeklyAvailability().getOpenDays().stream())
                .collect(Collectors.toUnmodifiableSet());

        Set<LocalTime> openHours = fields.stream()
                .flatMap(field -> field.getWeeklyAvailability().getOpenHours().stream())
                .collect(Collectors.toUnmodifiableSet());

        return new FieldSchedulingContext(fieldIds, openDays, openHours);
    }
}
