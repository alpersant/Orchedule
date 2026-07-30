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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SetFieldAvailabilityForWeekServiceTest {

    @Mock
    private FieldWeeklyAvailabilityRepository availabilityRepository;

    private SetFieldAvailabilityForWeekService service;

    private UUID seasonId;
    private UUID fieldA;
    private UUID fieldB;
    private UUID fieldC;

    @BeforeEach
    void setUp() {
        service = new SetFieldAvailabilityForWeekService(availabilityRepository);
        seasonId = UUID.randomUUID();
        fieldA = UUID.randomUUID();
        fieldB = UUID.randomUUID();
        fieldC = UUID.randomUUID();
    }

    @Test
    void shouldEnableOnlyRequestedFieldsWhenNoneTrackedYet() {
        when(availabilityRepository.findBySeasonIdAndWeekNumber(seasonId, 1)).thenReturn(List.of());
        when(availabilityRepository.save(any(FieldWeeklyAvailability.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        service.setForWeek(seasonId, 1, List.of(fieldA, fieldB));

        verify(availabilityRepository, times(2)).save(any(FieldWeeklyAvailability.class));
    }

    @Test
    void shouldDisablePreviouslyEnabledFieldNotInNewSelection() {
        FieldWeeklyAvailability existingA = FieldWeeklyAvailability.create(seasonId, 2, fieldA, true);
        FieldWeeklyAvailability existingB = FieldWeeklyAvailability.create(seasonId, 2, fieldB, true);
        when(availabilityRepository.findBySeasonIdAndWeekNumber(seasonId, 2))
                .thenReturn(List.of(existingA, existingB));
        when(availabilityRepository.save(any(FieldWeeklyAvailability.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // Only fieldA should remain enabled -> fieldB gets disabled
        service.setForWeek(seasonId, 2, List.of(fieldA));

        assertThat(existingA.isEnabled()).isTrue();
        assertThat(existingB.isEnabled()).isFalse();
        verify(availabilityRepository, atLeast(2)).save(any(FieldWeeklyAvailability.class));
    }

    @Test
    void shouldSupportOneTwoOrThreeFieldsEnabledPerWeek() {
        when(availabilityRepository.findBySeasonIdAndWeekNumber(seasonId, 5)).thenReturn(List.of());
        when(availabilityRepository.save(any(FieldWeeklyAvailability.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        service.setForWeek(seasonId, 5, List.of(fieldA, fieldB, fieldC));

        verify(availabilityRepository, times(3)).save(any(FieldWeeklyAvailability.class));
    }
}
