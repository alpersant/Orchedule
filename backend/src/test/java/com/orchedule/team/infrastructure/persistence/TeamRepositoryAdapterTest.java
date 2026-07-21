package com.orchedule.team.infrastructure.persistence;

import com.orchedule.team.application.exception.TeamNotFoundException;
import com.orchedule.team.domain.Team;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Import(TeamRepositoryAdapter.class)
class TeamRepositoryAdapterTest {

    @Autowired
    private TeamRepositoryAdapter adapter;

    @Test
    void shouldCreateAndFindTeam() {
        UUID teamId = adapter.create("Real Madrid");

        Team team = adapter.findById(teamId).orElseThrow();

        assertEquals("Real Madrid", team.name());
        assertTrue(team.active());
    }

    @Test
    void shouldUpdateTeam() {
        UUID teamId = adapter.create("Real Madrid");

        Team updated = adapter.update(teamId, "FC Barcelona", false);

        assertEquals("FC Barcelona", updated.name());
        assertFalse(updated.active());
    }

    @Test
    void shouldFailUpdatingUnknownTeam() {
        assertThrows(TeamNotFoundException.class,
                () -> adapter.update(UUID.randomUUID(), "Unknown", true));
    }

    @Test
    void shouldCheckExistsByNameIgnoreCase() {
        adapter.create("Real Madrid");

        assertTrue(adapter.existsByNameIgnoreCase("real madrid"));
    }
}