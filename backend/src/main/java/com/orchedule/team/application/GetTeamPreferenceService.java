package com.orchedule.team.application;

import com.orchedule.team.api.TeamMapper;
import com.orchedule.team.api.dto.TeamPreferenceResponse;
import com.orchedule.team.application.exception.TeamNotFoundException;
import com.orchedule.team.domain.TeamPreferenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class GetTeamPreferenceService {

    private final TeamPreferenceRepository teamPreferenceRepository;

    public GetTeamPreferenceService(TeamPreferenceRepository teamPreferenceRepository) {
        this.teamPreferenceRepository = teamPreferenceRepository;
    }

    @Transactional(readOnly = true)
    public TeamPreferenceResponse getByTeamId(UUID teamId) {
        return teamPreferenceRepository.findByTeamId(teamId)
                .map(TeamMapper::toResponse)
                .orElseThrow(() -> new TeamNotFoundException("Team preference not found for team: " + teamId));
    }
}