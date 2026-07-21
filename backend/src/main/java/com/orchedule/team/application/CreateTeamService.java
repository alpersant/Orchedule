package com.orchedule.team.application;

import com.orchedule.identity.application.exception.AlreadyExistsException;
import com.orchedule.team.api.TeamMapper;
import com.orchedule.team.api.dto.CreateTeamRequest;
import com.orchedule.team.api.dto.TeamResponse;
import com.orchedule.team.domain.Team;
import com.orchedule.team.domain.TeamRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CreateTeamService {

    private final TeamRepository teamRepository;

    public CreateTeamService(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    @Transactional
    public TeamResponse create(CreateTeamRequest request) {
        String normalizedName = request.name().trim();

        if (teamRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new AlreadyExistsException("Team already exists");
        }

        UUID teamId = teamRepository.create(normalizedName);
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalStateException("Team created but not found"));

        return TeamMapper.toResponse(team);
    }
}