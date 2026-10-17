package com.orchedule.field.application;

import com.orchedule.field.application.port.WeeklyAvailabilityPolicy;
import com.orchedule.field.domain.FieldWeeklyAvailability;
import com.orchedule.field.domain.FieldWeeklyAvailabilityRepository;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WeeklyAvailabilityServicesTest {
    @Test
    void anUnconfiguredWeekReturnsAnEmptyList() {
        var repo = mock(FieldWeeklyAvailabilityRepository.class);
        var policy = mock(WeeklyAvailabilityPolicy.class);
        var season = UUID.randomUUID();
        when(repo.findBySeasonIdAndWeekNumber(season, 1)).thenReturn(List.of());
        assertTrue(new GetFieldAvailabilityService(repo, policy).getForWeek(season, 1).isEmpty());
        verify(policy).validateRead(season, 1);
    }

    @Test
    void savingNoFieldsDisablesPreviouslyEnabledFields() {
        var repo = mock(FieldWeeklyAvailabilityRepository.class);
        var policy = mock(WeeklyAvailabilityPolicy.class);
        var season = UUID.randomUUID();
        var record = FieldWeeklyAvailability.create(season, 1, UUID.randomUUID(), true);
        when(repo.findBySeasonIdAndWeekNumber(season, 1)).thenReturn(List.of(record));
        new SetFieldAvailabilityForWeekService(repo, policy).setForWeek(season, 1, List.of());
        assertFalse(record.isEnabled());
        verify(policy).lockAndValidateWrite(season, 1, List.of());
        verify(repo).save(record);
    }

    @Test
    void rejectedWriteNeverChangesAvailability() {
        var repo = mock(FieldWeeklyAvailabilityRepository.class);
        var policy = mock(WeeklyAvailabilityPolicy.class);
        var season = UUID.randomUUID();
        doThrow(new IllegalStateException("rejected"))
            .when(policy).lockAndValidateWrite(season, 1, List.of());
        assertThrows(IllegalStateException.class, () ->
            new SetFieldAvailabilityForWeekService(repo, policy).setForWeek(season, 1, List.of()));
        verifyNoInteractions(repo);
    }

    @Test
    void repeatedIdenticalSelectionDoesNotWrite() {
        var repo = mock(FieldWeeklyAvailabilityRepository.class);
        var policy = mock(WeeklyAvailabilityPolicy.class);
        var season = UUID.randomUUID();
        var field = UUID.randomUUID();
        var record = FieldWeeklyAvailability.create(season, 1, field, true);
        when(repo.findBySeasonIdAndWeekNumber(season, 1)).thenReturn(List.of(record));
        new SetFieldAvailabilityForWeekService(repo, policy).setForWeek(season, 1, List.of(field));
        verify(repo, never()).save(any());
    }
}
