package com.orchedule.field.application;

import com.orchedule.field.domain.FieldWeeklyAvailability;
import com.orchedule.field.domain.FieldWeeklyAvailabilityRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class GetFieldAvailabilityService {

    private final FieldWeeklyAvailabilityRepository availabilityRepository;

    public GetFieldAvailabilityService(FieldWeeklyAvailabilityRepository availabilityRepository) {
        this.availabilityRepository = availabilityRepository;
    }

    @PreAuthorize("isAuthenticated()")
    public List<FieldWeeklyAvailability> getForWeek(UUID seasonId, int weekNumber) {
        return availabilityRepository.findBySeasonIdAndWeekNumber(seasonId, weekNumber);
    }

    @PreAuthorize("isAuthenticated()")
    public List<FieldWeeklyAvailability> getForSeason(UUID seasonId) {
        return availabilityRepository.findBySeasonId(seasonId);
    }
}
