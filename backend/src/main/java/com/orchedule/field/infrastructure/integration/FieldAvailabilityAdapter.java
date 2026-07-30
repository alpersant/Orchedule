package com.orchedule.field.infrastructure.integration;

import com.orchedule.field.domain.Field;
import com.orchedule.field.domain.FieldRepository;
import com.orchedule.field.domain.FieldWeeklyAvailability;
import com.orchedule.field.domain.FieldWeeklyAvailabilityRepository;
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

@Component
public class FieldAvailabilityAdapter implements FieldAvailabilityPort {

    private final FieldRepository fieldRepository;
    private final FieldWeeklyAvailabilityRepository weeklyAvailabilityRepository;

    public FieldAvailabilityAdapter(FieldRepository fieldRepository,
                                     FieldWeeklyAvailabilityRepository weeklyAvailabilityRepository) {
        this.fieldRepository = fieldRepository;
        this.weeklyAvailabilityRepository = weeklyAvailabilityRepository;
    }

    @Override
    public FieldSchedulingContext getSchedulingContext(UUID competitionId, UUID seasonId, int weekNumber) {
        List<Field> activeFields = fieldRepository.findAllActive();
        if (activeFields.isEmpty()) {
            throw new InvalidScheduleException(
                    "No active fields exist — cannot schedule competition " + competitionId);
        }

        Set<UUID> enabledFieldIdsThisWeek = weeklyAvailabilityRepository
                .findBySeasonIdAndWeekNumber(seasonId, weekNumber).stream()
                .filter(FieldWeeklyAvailability::isEnabled)
                .map(FieldWeeklyAvailability::getFieldId)
                .collect(Collectors.toUnmodifiableSet());

        List<Field> availableFields = activeFields.stream()
                .filter(field -> enabledFieldIdsThisWeek.contains(field.getId()))
                .toList();

        if (availableFields.isEmpty()) {
            throw new InvalidScheduleException(
                    "Season " + seasonId + " week " + weekNumber + " has no fields enabled — cannot schedule");
        }

        List<UUID> fieldIds = availableFields.stream().map(Field::getId).toList();

        Set<DayOfWeek> openDays = availableFields.stream()
                .flatMap(field -> field.getOpenDays().stream())
                .collect(Collectors.toUnmodifiableSet());

        Set<LocalTime> openHours = availableFields.stream()
                .flatMap(field -> field.getOpenHours().stream())
                .collect(Collectors.toUnmodifiableSet());

        return new FieldSchedulingContext(fieldIds, openDays, openHours);
    }
}
