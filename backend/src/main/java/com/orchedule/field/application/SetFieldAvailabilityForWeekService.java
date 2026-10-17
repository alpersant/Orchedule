package com.orchedule.field.application;

import com.orchedule.field.application.exception.InvalidFieldException;
import com.orchedule.field.domain.FieldValidator;
import com.orchedule.field.application.port.WeeklyAvailabilityPolicy;
import com.orchedule.field.domain.FieldWeeklyAvailability;
import com.orchedule.field.domain.FieldWeeklyAvailabilityRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SetFieldAvailabilityForWeekService {

    private final FieldWeeklyAvailabilityRepository repository;

    private final WeeklyAvailabilityPolicy policy;

    public SetFieldAvailabilityForWeekService(FieldWeeklyAvailabilityRepository repository,
                                              WeeklyAvailabilityPolicy policy) {
        this.repository = repository;
        this.policy = policy;
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public List<FieldWeeklyAvailability> setForWeek(UUID seasonId,
                                                    int weekNumber,
                                                    List<UUID> enabledFieldIds) {
        validate(seasonId, weekNumber, enabledFieldIds);
        policy.lockAndValidateWrite(seasonId, weekNumber, enabledFieldIds);

        List<FieldWeeklyAvailability> existing =
                repository.findBySeasonIdAndWeekNumber(seasonId, weekNumber);
        Set<UUID> enabled = new LinkedHashSet<>(enabledFieldIds);

        for (FieldWeeklyAvailability availability : existing) {
            boolean shouldBeEnabled = enabled.contains(availability.getFieldId());
            if (availability.isEnabled() != shouldBeEnabled) {
                if (shouldBeEnabled) {
                    availability.enable();
                } else {
                    availability.disable();
                }
                repository.save(availability);
            }
        }

        Set<UUID> tracked = existing.stream()
                .map(FieldWeeklyAvailability::getFieldId)
                .collect(Collectors.toSet());

        for (UUID fieldId : enabled) {
            if (!tracked.contains(fieldId)) {
                repository.save(FieldWeeklyAvailability.create(seasonId, weekNumber, fieldId, true));
            }
        }

        return repository.findBySeasonIdAndWeekNumber(seasonId, weekNumber);
    }

    private void validate(UUID seasonId, int weekNumber, List<UUID> enabledFieldIds) {
        if (seasonId == null) {
            throw new InvalidFieldException("Season id is required");
        }
        FieldValidator.validateWeekNumber(weekNumber);
        if (enabledFieldIds == null) {
            throw new InvalidFieldException("Enabled field ids are required");
        }
        if (enabledFieldIds.size() != new HashSet<>(enabledFieldIds).size()) {
            throw new InvalidFieldException("Enabled field ids must not contain duplicates");
        }
    }
}
