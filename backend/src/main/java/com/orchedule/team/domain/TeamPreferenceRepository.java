package com.orchedule.team.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TeamPreferenceRepository {

    TeamPreference save(TeamPreference preferences);

    Optional<TeamPreference> findByTeamIdAndCompetitionId(UUID teamId, UUID competitionId);

    List<TeamPreference> findByCompetitionId(UUID competitionId);

    boolean existsByTeamIdAndCompetitionId(UUID teamId, UUID competitionId);
}
