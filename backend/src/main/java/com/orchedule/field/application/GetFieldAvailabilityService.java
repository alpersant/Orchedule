package com.orchedule.field.application;

import com.orchedule.field.domain.FieldWeeklyAvailability;
import com.orchedule.field.domain.FieldWeeklyAvailabilityRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.orchedule.field.application.port.WeeklyAvailabilityPolicy;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class GetFieldAvailabilityService {

    private final FieldWeeklyAvailabilityRepository availabilityRepository;

    private final WeeklyAvailabilityPolicy policy;

    public GetFieldAvailabilityService(FieldWeeklyAvailabilityRepository availabilityRepository,
                                        WeeklyAvailabilityPolicy policy) {
        this.availabilityRepository = availabilityRepository;
        this.policy = policy;
    }

    @PreAuthorize("isAuthenticated()")
    public List<FieldWeeklyAvailability> getForWeek(UUID seasonId, int weekNumber) {
        policy.validateRead(seasonId, weekNumber);
        return availabilityRepository.findBySeasonIdAndWeekNumber(seasonId, weekNumber);
    }

    @PreAuthorize("isAuthenticated()")
    public List<FieldWeeklyAvailability> getForSeason(UUID seasonId) {
        policy.validateRead(seasonId, null);
        return availabilityRepository.findBySeasonId(seasonId);
    }
}
