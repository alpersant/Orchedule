package com.orchedule.field.application;

import com.orchedule.field.application.exception.InvalidFieldException;
import com.orchedule.field.domain.FieldWeeklyAvailability;
import com.orchedule.field.domain.FieldWeeklyAvailabilityRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/** Applies one availability configuration to several weeks atomically. */
@Service
public class ApplyWeeklyAvailabilityService {

    private final SetFieldAvailabilityForWeekService setForWeekService;
    private final FieldWeeklyAvailabilityRepository repository;

    public ApplyWeeklyAvailabilityService(SetFieldAvailabilityForWeekService setForWeekService,
                                          FieldWeeklyAvailabilityRepository repository) {
        this.setForWeekService = setForWeekService;
        this.repository = repository;
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public List<FieldWeeklyAvailability> apply(UUID seasonId,
                                               List<Integer> weekNumbers,
                                               List<UUID> enabledFieldIds) {
        if (seasonId == null || weekNumbers == null || weekNumbers.isEmpty() || weekNumbers.size() > 60) {
            throw new InvalidFieldException("Season id and at least one week are required");
        }
        if (new HashSet<>(weekNumbers).size() != weekNumbers.size()) {
            throw new InvalidFieldException("Week numbers must not contain duplicates");
        }

        List<FieldWeeklyAvailability> result = new ArrayList<>();
        for (Integer weekNumber : weekNumbers) {
            if (weekNumber == null) {
                throw new InvalidFieldException("Week number is required");
            }
            result.addAll(setForWeekService.setForWeek(seasonId, weekNumber, enabledFieldIds));
        }
        return result;
    }
}
