package com.orchedule.infrastructure.integration;

import com.orchedule.field.domain.Field;
import com.orchedule.field.domain.FieldRepository;
import com.orchedule.field.domain.FieldWeeklyAvailabilityRepository;
import com.orchedule.scheduling.application.exception.InvalidScheduleException;
import com.orchedule.scheduling.application.port.FieldAvailabilityPort;
import com.orchedule.scheduling.application.port.FieldScheduleSlot;
import com.orchedule.scheduling.application.port.FieldSchedulingContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Builds the scheduling context from fields enabled for one season week.
 * Each field contributes only its own opening day/hour combinations.
 */
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
    public FieldSchedulingContext getSchedulingContext(UUID competitionId,
                                                       UUID seasonId,
                                                       int weekNumber) {
        List<Field> activeFields = fieldRepository.findEnabledForWeek(seasonId, weekNumber);

        if (activeFields.isEmpty()) {
            throw new InvalidScheduleException(
                    "No active fields exist — cannot schedule competition " + competitionId);
        }

        List<FieldScheduleSlot> slots = new ArrayList<>();

        for (Field field : activeFields) {
            for (var day : field.getOpenDays()) {
                for (var hour : field.getOpenHours()) {
                    slots.add(new FieldScheduleSlot(field.getId(), day, hour));
                }
            }
        }

        if (slots.isEmpty()) {
            throw new InvalidScheduleException(
                    "Season " + seasonId + " week " + weekNumber
                            + " has no valid field opening slots — cannot schedule");
        }

        return new FieldSchedulingContext(slots);
    }
}