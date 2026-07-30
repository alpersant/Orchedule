package com.orchedule.team.application;

import com.orchedule.team.application.exception.TeamPreferenceNotFoundException;
import com.orchedule.team.domain.TeamPreference;
import com.orchedule.team.domain.TeamPreferenceRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class GetTeamPreferencesService {

    private final TeamPreferenceRepository teamPreferenceRepository;

    public GetTeamPreferencesService(TeamPreferenceRepository teamPreferenceRepository) {
        this.teamPreferenceRepository = teamPreferenceRepository;
    }

    @PreAuthorize("isAuthenticated()")
    public TeamPreference getByTeamAndCompetition(UUID teamId, UUID competitionId) {
        return teamPreferenceRepository.findByTeamIdAndCompetitionId(teamId, competitionId)
                .orElseThrow(() -> new TeamPreferenceNotFoundException(teamId, competitionId));
    }

    @PreAuthorize("isAuthenticated()")
    public List<TeamPreference> getByCompetition(UUID competitionId) {
        return teamPreferenceRepository.findByCompetitionId(competitionId);
    }
}
