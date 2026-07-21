package com.orchedule.team.application;

import com.orchedule.team.api.TeamMapper;
import com.orchedule.team.api.dto.SaveTeamPreferenceRequest;
import com.orchedule.team.api.dto.TeamPreferenceResponse;
import com.orchedule.team.application.exception.TeamNotFoundException;
import com.orchedule.team.domain.DayPreference;
import com.orchedule.team.domain.TeamPreference;
import com.orchedule.team.domain.TeamPreferenceRepository;
import com.orchedule.team.domain.TeamPreferenceValidator;
import com.orchedule.team.domain.TeamRepository;
import com.orchedule.team.domain.TimePreference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class SaveTeamPreferenceService {

    private final TeamRepository teamRepository;
    private final TeamPreferenceRepository teamPreferenceRepository;

    public SaveTeamPreferenceService(TeamRepository teamRepository,
                                     TeamPreferenceRepository teamPreferenceRepository) {
        this.teamRepository = teamRepository;
        this.teamPreferenceRepository = teamPreferenceRepository;
    }

    @Transactional
    public TeamPreferenceResponse save(UUID teamId, SaveTeamPreferenceRequest request) {
        teamRepository.findById(teamId)
                .orElseThrow(() -> new TeamNotFoundException("Team not found: " + teamId));

        List<DayPreference> dayPreferences = request.dayPreferences().stream()
                .map(p -> new DayPreference(p.day(), p.priority()))
                .toList();

        List<TimePreference> timePreferences = request.timePreferences().stream()
                .map(p -> new TimePreference(p.hour(), p.priority()))
                .toList();

        TeamPreferenceValidator.validate(
                request.restrictionType(),
                request.excludedDay(),
                request.excludedHour(),
                dayPreferences,
                timePreferences
        );

        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime createdAt = teamPreferenceRepository.findByTeamId(teamId)
                .map(TeamPreference::createdAt)
                .orElse(now);

        TeamPreference saved = teamPreferenceRepository.save(new TeamPreference(
                teamId,
                request.restrictionType(),
                request.excludedDay(),
                request.excludedHour(),
                dayPreferences,
                timePreferences,
                createdAt,
                now
        ));

        return TeamMapper.toResponse(saved);
    }
}