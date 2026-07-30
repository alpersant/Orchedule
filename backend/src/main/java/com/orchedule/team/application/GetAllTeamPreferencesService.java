package com.orchedule.team.application;

import com.orchedule.team.domain.Team;
import com.orchedule.team.domain.TeamPreference;
import com.orchedule.team.domain.TeamPreferenceRepository;
import com.orchedule.team.domain.TeamRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class GetAllTeamPreferencesService {

    private final TeamRepository teamRepository;
    private final TeamPreferenceRepository teamPreferenceRepository;

    public GetAllTeamPreferencesService(TeamRepository teamRepository,
                                         TeamPreferenceRepository teamPreferenceRepository) {
        this.teamRepository = teamRepository;
        this.teamPreferenceRepository = teamPreferenceRepository;
    }

    @PreAuthorize("isAuthenticated()")
    public List<TeamPreference> getForCompetition(UUID competitionId) {
        return teamPreferenceRepository.findByCompetitionId(competitionId);
    }

    @PreAuthorize("isAuthenticated()")
    public List<TeamPreference> getForTeamsAndCompetition(List<UUID> teamIds, UUID competitionId) {
        return teamIds.stream()
                .map(teamId -> teamPreferenceRepository.findByTeamIdAndCompetitionId(teamId, competitionId))
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .toList();
    }

    @PreAuthorize("isAuthenticated()")
    public List<TeamPreference> getForAllActiveTeams(UUID competitionId) {
        List<UUID> activeTeamIds = teamRepository.findAll().stream()
                .filter(Team::active)
                .map(Team::id)
                .toList();
        return getForTeamsAndCompetition(activeTeamIds, competitionId);
    }
}
