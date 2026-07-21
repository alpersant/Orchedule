package com.orchedule.team.application;

import com.orchedule.team.api.TeamMapper;
import com.orchedule.team.api.dto.TeamResponse;
import com.orchedule.team.application.exception.TeamNotFoundException;
import com.orchedule.team.domain.TeamRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class GetTeamService {

    private final TeamRepository teamRepository;

    public GetTeamService(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    @Transactional(readOnly = true)
    public TeamResponse getById(UUID teamId) {
        return teamRepository.findById(teamId)
                .map(TeamMapper::toResponse)
                .orElseThrow(() -> new TeamNotFoundException("Team not found: " + teamId));
    }

    @Transactional(readOnly = true)
    public List<TeamResponse> getAll() {
        return teamRepository.findAll().stream()
                .map(TeamMapper::toResponse)
                .toList();
    }
}