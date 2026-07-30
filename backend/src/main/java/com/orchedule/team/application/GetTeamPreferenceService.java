package com.orchedule.team.application;

import com.orchedule.team.api.TeamMapper;
import com.orchedule.team.api.dto.TeamPreferenceResponse;
import com.orchedule.team.application.exception.TeamPreferenceNotFoundException;
import com.orchedule.team.domain.TeamPreference;
import com.orchedule.team.domain.TeamPreferenceRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class GetTeamPreferenceService {

    private final TeamPreferenceRepository teamPreferenceRepository;

    public GetTeamPreferenceService(TeamPreferenceRepository teamPreferenceRepository) {
        this.teamPreferenceRepository = teamPreferenceRepository;
    }

    @PreAuthorize("isAuthenticated()")
    public TeamPreferenceResponse getByTeamIdAndCompetitionId(UUID teamId, UUID competitionId) {
        TeamPreference preference = teamPreferenceRepository.findByTeamIdAndCompetitionId(teamId, competitionId)
                .orElseThrow(() -> new TeamPreferenceNotFoundException(teamId, competitionId));
        return TeamMapper.toResponse(preference);
    }
}
