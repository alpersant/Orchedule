package com.orchedule.competition.application;

import com.orchedule.competition.application.exception.CompetitionNotFoundException;
import com.orchedule.competition.application.exception.InvalidCompetitionException;
import com.orchedule.competition.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateCompetitionDefaultsServiceTest {

    @Mock
    private CompetitionRepository competitionRepository;

    private UpdateCompetitionDefaultsService service;

    @BeforeEach
    void setUp() {
        service = new UpdateCompetitionDefaultsService(competitionRepository);
    }

    @Test
    void shouldUpdateDefaultsWhenValid() {
        UUID id = UUID.randomUUID();
        Competition competition = Competition.createWithDefaults("Liga", "Desc");
        when(competitionRepository.findById(id)).thenReturn(Optional.of(competition));
        when(competitionRepository.save(any(Competition.class))).thenAnswer(inv -> inv.getArgument(0));

        Set<CompetitionDay> days = Set.of(CompetitionDay.SATURDAY, CompetitionDay.SUNDAY);
        Set<CompetitionHour> hours = Set.of(CompetitionHour.H18, CompetitionHour.H19);

        Competition result = service.updateDefaults(id, days, hours, 2);

        assertThat(result.getDefaultDays()).isEqualTo(days);
        assertThat(result.getDefaultHours()).isEqualTo(hours);
        assertThat(result.getDefaultFieldCount()).isEqualTo(2);
    }

    @Test
    void shouldThrowWhenCompetitionNotFound() {
        UUID id = UUID.randomUUID();
        when(competitionRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateDefaults(
                id, Set.of(CompetitionDay.MONDAY), Set.of(CompetitionHour.H18), 1))
                .isInstanceOf(CompetitionNotFoundException.class);
    }

    @Test
    void shouldRejectEmptyDays() {
        UUID id = UUID.randomUUID();

        assertThatThrownBy(() -> service.updateDefaults(id, Set.of(), Set.of(CompetitionHour.H18), 1))
                .isInstanceOf(InvalidCompetitionException.class);
    }

    @Test
    void shouldRejectInvalidFieldCount() {
        UUID id = UUID.randomUUID();

        assertThatThrownBy(() -> service.updateDefaults(
                id, Set.of(CompetitionDay.MONDAY), Set.of(CompetitionHour.H18), 0))
                .isInstanceOf(InvalidCompetitionException.class);
    }
}
