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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetCompetitionServiceTest {

    @Mock
    private CompetitionRepository competitionRepository;

    private GetCompetitionService getCompetitionService;

    @BeforeEach
    void setUp() {
        getCompetitionService = new GetCompetitionService(competitionRepository);
    }

    @Test
    void shouldReturnCompetitionWhenFound() {
        UUID id = UUID.randomUUID();
        Competition competition = Competition.createWithDefaults("Liga", "Desc");
        when(competitionRepository.findById(id)).thenReturn(Optional.of(competition));

        Competition result = getCompetitionService.getById(id);

        assertThat(result).isEqualTo(competition);
    }

    @Test
    void shouldThrowCompetitionNotFoundExceptionWithCorrectCode() {
        UUID id = UUID.randomUUID();
        when(competitionRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> getCompetitionService.getById(id))
                .isInstanceOf(CompetitionNotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    @Test
    void shouldReturnAllCompetitions() {
        Competition competition = Competition.createWithDefaults("Liga", "Desc");
        when(competitionRepository.findAll()).thenReturn(List.of(competition));

        List<Competition> result = getCompetitionService.getAll();

        assertThat(result).containsExactly(competition);
    }

    @Test
    void shouldReturnCompetitionsByStatus() {
        Competition competition = Competition.createWithDefaults("Liga", "Desc");
        when(competitionRepository.findByStatus(CompetitionStatus.DRAFT)).thenReturn(List.of(competition));

        List<Competition> result = getCompetitionService.getByStatus(CompetitionStatus.DRAFT);

        assertThat(result).containsExactly(competition);
    }
}
