package com.orchedule.team.domain;

import java.util.Optional;
import java.util.UUID;

public interface TeamPreferenceRepository {

    TeamPreference save(TeamPreference preference);

    Optional<TeamPreference> findByTeamId(UUID teamId);
}