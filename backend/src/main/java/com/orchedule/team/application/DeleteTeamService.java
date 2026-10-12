package com.orchedule.team.application;

import com.orchedule.team.application.exception.TeamNotFoundException;
import com.orchedule.team.domain.TeamRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class DeleteTeamService {

    private final TeamRepository teamRepository;

    public DeleteTeamService(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public void delete(UUID teamId) {
        int deletedRows = teamRepository.deleteById(teamId);

        if (deletedRows == 0) {
            throw new TeamNotFoundException("Team not found: " + teamId);
        }
    }
}
