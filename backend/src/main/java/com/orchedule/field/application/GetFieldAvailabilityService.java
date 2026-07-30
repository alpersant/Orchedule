package com.orchedule.field.application;

import com.orchedule.field.domain.FieldWeeklyAvailability;
import com.orchedule.field.domain.FieldWeeklyAvailabilityRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class GetFieldAvailabilityService {

    private final FieldWeeklyAvailabilityRepository availabilityRepository;

    public GetFieldAvailabilityService(FieldWeeklyAvailabilityRepository availabilityRepository) {
        this.availabilityRepository = availabilityRepository;
    }

    public List<FieldWeeklyAvailability> getForWeek(UUID seasonId, int weekNumber) {
        return availabilityRepository.findBySeasonIdAndWeekNumber(seasonId, weekNumber);
    }

    public List<FieldWeeklyAvailability> getForSeason(UUID seasonId) {
        return availabilityRepository.findBySeasonId(seasonId);
    }
}
