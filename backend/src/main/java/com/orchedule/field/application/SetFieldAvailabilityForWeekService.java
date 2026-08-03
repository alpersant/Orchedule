package com.orchedule.field.application;

import com.orchedule.field.domain.FieldValidator;
import com.orchedule.field.domain.FieldWeeklyAvailability;
import com.orchedule.field.domain.FieldWeeklyAvailabilityRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;


@Service
public class SetFieldAvailabilityForWeekService {

    private final FieldWeeklyAvailabilityRepository availabilityRepository;

    public SetFieldAvailabilityForWeekService(FieldWeeklyAvailabilityRepository availabilityRepository) {
        this.availabilityRepository = availabilityRepository;
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public List<FieldWeeklyAvailability> setForWeek(UUID seasonId, int weekNumber, List<UUID> enabledFieldIds) {
        FieldValidator.validateWeekNumber(weekNumber);

        List<FieldWeeklyAvailability> existing =
                availabilityRepository.findBySeasonIdAndWeekNumber(seasonId, weekNumber);

        for (FieldWeeklyAvailability availability : existing) {
            if (enabledFieldIds.contains(availability.getFieldId())) {
                availability.enable();
            } else {
                availability.disable();
            }
            availabilityRepository.save(availability);
        }

        for (UUID fieldId : enabledFieldIds) {
            boolean alreadyTracked = existing.stream()
                    .anyMatch(a -> a.getFieldId().equals(fieldId));
            if (!alreadyTracked) {
                FieldWeeklyAvailability created =
                        FieldWeeklyAvailability.create(seasonId, weekNumber, fieldId, true);
                availabilityRepository.save(created);
            }
        }

        return availabilityRepository.findBySeasonIdAndWeekNumber(seasonId, weekNumber);
    }
}
