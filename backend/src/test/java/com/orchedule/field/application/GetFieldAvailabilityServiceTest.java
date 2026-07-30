package com.orchedule.field.application;

import com.orchedule.field.domain.FieldWeeklyAvailability;
import com.orchedule.field.domain.FieldWeeklyAvailabilityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetFieldAvailabilityServiceTest {

    @Mock
    private FieldWeeklyAvailabilityRepository availabilityRepository;

    private GetFieldAvailabilityService service;

    @BeforeEach
    void setUp() {
        service = new GetFieldAvailabilityService(availabilityRepository);
    }

    @Test
    void shouldReturnAvailabilityForSpecificWeek() {
        UUID seasonId = UUID.randomUUID();
        FieldWeeklyAvailability availability =
                FieldWeeklyAvailability.create(seasonId, 4, UUID.randomUUID(), true);
        when(availabilityRepository.findBySeasonIdAndWeekNumber(seasonId, 4))
                .thenReturn(List.of(availability));

        List<FieldWeeklyAvailability> result = service.getForWeek(seasonId, 4);

        assertThat(result).containsExactly(availability);
    }

    @Test
    void shouldReturnAvailabilityForWholeSeason() {
        UUID seasonId = UUID.randomUUID();
        FieldWeeklyAvailability availability =
                FieldWeeklyAvailability.create(seasonId, 1, UUID.randomUUID(), true);
        when(availabilityRepository.findBySeasonId(seasonId)).thenReturn(List.of(availability));

        List<FieldWeeklyAvailability> result = service.getForSeason(seasonId);

        assertThat(result).containsExactly(availability);
    }
}
