package com.orchedule.field.application;

import com.orchedule.field.domain.FieldWeeklyAvailability;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
public class ClearWeeklyAvailabilityService {
    private final SetFieldAvailabilityForWeekService service;

    public ClearWeeklyAvailabilityService(SetFieldAvailabilityForWeekService service) {
        this.service = service;
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public List<FieldWeeklyAvailability> clear(UUID seasonId, int weekNumber) {
        return service.setForWeek(seasonId, weekNumber, List.of());
    }
}
