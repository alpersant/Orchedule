package com.orchedule.team.application;

import com.orchedule.team.application.exception.TeamNotFoundException;
import com.orchedule.team.domain.TeamRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;

@Service
public class DeleteTeamsBulkService {

    private final TeamRepository teamRepository;

    public DeleteTeamsBulkService(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public void delete(List<UUID> teamIds) {
        var uniqueIds = new LinkedHashSet<>(teamIds);

        for (UUID teamId : uniqueIds) {
            int deletedRows = teamRepository.deleteById(teamId);

            if (deletedRows == 0) {
                throw new TeamNotFoundException("Team not found: " + teamId);
            }
        }
    }
}
