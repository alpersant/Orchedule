package com.orchedule.team.application;

import com.orchedule.team.api.TeamMapper;
import com.orchedule.team.api.dto.TeamResponse;
import com.orchedule.team.api.dto.UpdateTeamRequest;
import com.orchedule.team.domain.Team;
import com.orchedule.team.domain.TeamRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UpdateTeamService {

    private final TeamRepository teamRepository;

    public UpdateTeamService(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    @Transactional
    public TeamResponse update(UUID teamId, UpdateTeamRequest request) {
        Team updated = teamRepository.update(teamId, request.name().trim(), request.active());
        return TeamMapper.toResponse(updated);
    }
}