package com.orchedule.team.application;

import com.orchedule.identity.application.exception.AlreadyExistsException;
import com.orchedule.team.api.dto.CreateTeamRequest;
import com.orchedule.team.domain.Team;
import com.orchedule.team.domain.TeamRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateTeamServiceTest {

    @Mock
    private TeamRepository teamRepository;

    @InjectMocks
    private CreateTeamService createTeamService;

    @Test
    void shouldCreateTeam() {
        UUID id = UUID.randomUUID();
        when(teamRepository.existsByNameIgnoreCase("Real Madrid")).thenReturn(false);
        when(teamRepository.create("Real Madrid")).thenReturn(id);
        when(teamRepository.findById(id)).thenReturn(Optional.of(new Team(
                id,
                "Real Madrid",
                true,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        )));

        var result = createTeamService.create(new CreateTeamRequest("Real Madrid"));

        assertEquals(id, result.id());
        assertEquals("Real Madrid", result.name());
        assertEquals(true, result.active());
    }

    @Test
    void shouldRejectDuplicatedTeamName() {
        when(teamRepository.existsByNameIgnoreCase(anyString())).thenReturn(true);

        assertThrows(AlreadyExistsException.class,
                () -> createTeamService.create(new CreateTeamRequest("Real Madrid")));
    }
}