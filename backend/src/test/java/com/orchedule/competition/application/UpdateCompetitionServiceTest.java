package com.orchedule.competition.application;

import com.orchedule.competition.application.exception.CompetitionAlreadyExistsException;
import com.orchedule.competition.application.exception.CompetitionNotFoundException;
import com.orchedule.competition.application.exception.InvalidCompetitionException;
import com.orchedule.competition.domain.Competition;
import com.orchedule.competition.domain.CompetitionRepository;
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
class UpdateCompetitionServiceTest {

    @Mock
    private CompetitionRepository competitionRepository;

    private UpdateCompetitionService updateCompetitionService;

    @BeforeEach
    void setUp() {
        updateCompetitionService = new UpdateCompetitionService(competitionRepository);
    }

    @Test
    void shouldUpdateNameAndDescription() {
        UUID id = UUID.randomUUID();
        Competition competition = Competition.createWithDefaults("Old Name", "Old Desc");
        when(competitionRepository.findById(id)).thenReturn(Optional.of(competition));
        when(competitionRepository.existsByName("New Name")).thenReturn(false);
        when(competitionRepository.save(any(Competition.class))).thenAnswer(inv -> inv.getArgument(0));

        Competition result = updateCompetitionService.update(id, "New Name", "New Desc");

        assertThat(result.getName()).isEqualTo("New Name");
        assertThat(result.getDescription()).isEqualTo("New Desc");
    }

    @Test
    void shouldAllowKeepingSameName() {
        UUID id = UUID.randomUUID();
        Competition competition = Competition.createWithDefaults("Same Name", "Old Desc");
        when(competitionRepository.findById(id)).thenReturn(Optional.of(competition));
        when(competitionRepository.save(any(Competition.class))).thenAnswer(inv -> inv.getArgument(0));

        Competition result = updateCompetitionService.update(id, "Same Name", "New Desc");

        assertThat(result.getDescription()).isEqualTo("New Desc");
    }

    @Test
    void shouldThrowWhenCompetitionNotFound() {
        UUID id = UUID.randomUUID();
        when(competitionRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> updateCompetitionService.update(id, "Name", "Desc"))
                .isInstanceOf(CompetitionNotFoundException.class);
    }

    @Test
    void shouldRejectRenameToExistingName() {
        UUID id = UUID.randomUUID();
        Competition competition = Competition.createWithDefaults("Old Name", "Desc");
        when(competitionRepository.findById(id)).thenReturn(Optional.of(competition));
        when(competitionRepository.existsByName("Taken Name")).thenReturn(true);

        assertThatThrownBy(() -> updateCompetitionService.update(id, "Taken Name", "Desc"))
                .isInstanceOf(CompetitionAlreadyExistsException.class);
    }

    @Test
    void shouldRejectBlankName() {
        UUID id = UUID.randomUUID();

        assertThatThrownBy(() -> updateCompetitionService.update(id, "", "Desc"))
                .isInstanceOf(InvalidCompetitionException.class);
    }
}
