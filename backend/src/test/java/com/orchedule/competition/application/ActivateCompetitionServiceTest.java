package com.orchedule.competition.application;

import com.orchedule.competition.application.exception.CompetitionNotFoundException;
import com.orchedule.competition.application.exception.InvalidCompetitionStateException;
import com.orchedule.competition.domain.Competition;
import com.orchedule.competition.domain.CompetitionRepository;
import com.orchedule.competition.domain.CompetitionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActivateCompetitionServiceTest {

    @Mock
    private CompetitionRepository competitionRepository;

    private ActivateCompetitionService activateCompetitionService;

    @BeforeEach
    void setUp() {
        activateCompetitionService = new ActivateCompetitionService(competitionRepository);
    }

    @Test
    void shouldActivateDraftCompetition() {
        UUID id = UUID.randomUUID();
        Competition competition = Competition.createWithDefaults("Liga", "Desc");
        when(competitionRepository.findById(id)).thenReturn(Optional.of(competition));
        when(competitionRepository.save(any(Competition.class))).thenAnswer(inv -> inv.getArgument(0));

        Competition result = activateCompetitionService.activate(id);

        assertThat(result.getStatus()).isEqualTo(CompetitionStatus.ACTIVE);
    }

    @Test
    void shouldThrowWhenCompetitionNotFound() {
        UUID id = UUID.randomUUID();
        when(competitionRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> activateCompetitionService.activate(id))
                .isInstanceOf(CompetitionNotFoundException.class);
    }

    @Test
    void shouldTranslateDomainStateErrorIntoApplicationException() {
        UUID id = UUID.randomUUID();
        Competition competition = Competition.createWithDefaults("Liga", "Desc");
        competition.close();
        when(competitionRepository.findById(id)).thenReturn(Optional.of(competition));

        assertThatThrownBy(() -> activateCompetitionService.activate(id))
                .isInstanceOf(InvalidCompetitionStateException.class)
                .hasMessageContaining("closed");
    }
}
