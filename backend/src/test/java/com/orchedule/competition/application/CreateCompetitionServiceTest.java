package com.orchedule.competition.application;

import com.orchedule.competition.application.exception.CompetitionAlreadyExistsException;
import com.orchedule.competition.application.exception.InvalidCompetitionException;
import com.orchedule.competition.domain.Competition;
import com.orchedule.competition.domain.CompetitionRepository;
import com.orchedule.competition.domain.CompetitionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateCompetitionServiceTest {

    @Mock
    private CompetitionRepository competitionRepository;

    private CreateCompetitionService createCompetitionService;

    @BeforeEach
    void setUp() {
        createCompetitionService = new CreateCompetitionService(competitionRepository);
    }

    @Test
    void shouldCreateCompetitionWhenNameIsUnique() {
        when(competitionRepository.existsByName("Liga Local")).thenReturn(false);
        when(competitionRepository.save(any(Competition.class))).thenAnswer(inv -> inv.getArgument(0));

        Competition result = createCompetitionService.create("Liga Local", "Desc");

        assertThat(result.getName()).isEqualTo("Liga Local");
        assertThat(result.getStatus()).isEqualTo(CompetitionStatus.DRAFT);

        ArgumentCaptor<Competition> captor = ArgumentCaptor.forClass(Competition.class);
        verify(competitionRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("Liga Local");
    }

    @Test
    void shouldRejectDuplicateName() {
        when(competitionRepository.existsByName("Liga Local")).thenReturn(true);

        assertThatThrownBy(() -> createCompetitionService.create("Liga Local", "Desc"))
                .isInstanceOf(CompetitionAlreadyExistsException.class);

        verify(competitionRepository, never()).save(any());
    }

    @Test
    void shouldRejectBlankName() {
        assertThatThrownBy(() -> createCompetitionService.create("  ", "Desc"))
                .isInstanceOf(InvalidCompetitionException.class);

        verify(competitionRepository, never()).save(any());
        verify(competitionRepository, never()).existsByName(any());
    }
}
