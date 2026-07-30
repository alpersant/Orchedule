package com.orchedule.competition.application;

import com.orchedule.competition.application.exception.CompetitionNotFoundException;
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
class CloseCompetitionServiceTest {

    @Mock
    private CompetitionRepository competitionRepository;

    private CloseCompetitionService closeCompetitionService;

    @BeforeEach
    void setUp() {
        closeCompetitionService = new CloseCompetitionService(competitionRepository);
    }

    @Test
    void shouldCloseActiveCompetition() {
        UUID id = UUID.randomUUID();
        Competition competition = Competition.createWithDefaults("Liga", "Desc");
        competition.activate();
        when(competitionRepository.findById(id)).thenReturn(Optional.of(competition));
        when(competitionRepository.save(any(Competition.class))).thenAnswer(inv -> inv.getArgument(0));

        Competition result = closeCompetitionService.close(id);

        assertThat(result.getStatus()).isEqualTo(CompetitionStatus.CLOSED);
    }

    @Test
    void shouldThrowWhenCompetitionNotFound() {
        UUID id = UUID.randomUUID();
        when(competitionRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> closeCompetitionService.close(id))
                .isInstanceOf(CompetitionNotFoundException.class);
    }
}
