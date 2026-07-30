package com.orchedule.team.application;

import com.orchedule.team.application.exception.TeamNotFoundException;
import com.orchedule.team.application.port.CompetitionPreferencePolicyPort;
import com.orchedule.team.domain.*;
import com.orchedule.team.domain.event.TeamPreferenceChangedEvent;
import com.orchedule.team.domain.preferences.TeamPreferenceValidationStrategy;
import com.orchedule.team.domain.preferences.TeamPreferenceValidatorResolver;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Creates or updates a team's day/hour preferences for a specific
 * competition. Team existence is now verified against the real
 * TeamRepository contract (findById(UUID) -> Optional<Team>) before any
 * preference is persisted, closing the gap flagged in the previous
 * revision. Team itself has no competitionId — a team is a global entity
 * that can, in principle, participate in more than one competition, with
 * one TeamPreferences row per (teamId, competitionId) pair.
 */
@Service
public class SetTeamPreferenceService {

    private final TeamRepository teamRepository;
    private final TeamPreferenceRepository teamPreferenceRepository;
    private final CompetitionPreferencePolicyPort competitionPreferencePolicyPort;
    private final TeamPreferenceValidatorResolver validatorResolver;
    private final ApplicationEventPublisher eventPublisher;

    public SetTeamPreferenceService(TeamRepository teamRepository,
                                    TeamPreferenceRepository teamPreferenceRepository,
                                    CompetitionPreferencePolicyPort competitionPreferencePolicyPort,
                                    TeamPreferenceValidatorResolver validatorResolver,
                                    ApplicationEventPublisher eventPublisher) {
        this.teamRepository = teamRepository;
        this.teamPreferenceRepository = teamPreferenceRepository;
        this.competitionPreferencePolicyPort = competitionPreferencePolicyPort;
        this.validatorResolver = validatorResolver;
        this.eventPublisher = eventPublisher;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER', 'TEAM_MANAGER')")
    @Transactional
    public TeamPreference setPreferences(UUID teamId, UUID competitionId,
                                         List<TeamDayPreference> dayPreferences,
                                         List<TeamHourPreference> hourPreferences) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new TeamNotFoundException("Team not found: " + teamId));

        if (!team.active()) {
            throw new IllegalStateException("Cannot set preferences for an inactive team: " + teamId);
        }

        var policy = competitionPreferencePolicyPort.getPolicyForCompetition(competitionId);
        TeamPreferenceValidationStrategy validator = validatorResolver.resolve(policy);
        validator.validate(dayPreferences, hourPreferences);

        TeamPreference preferences = teamPreferenceRepository
                .findByTeamIdAndCompetitionId(teamId, competitionId)
                .orElseGet(() -> TeamPreference.create(teamId, competitionId, dayPreferences, hourPreferences));

        preferences.update(dayPreferences, hourPreferences);
        TeamPreference saved = teamPreferenceRepository.save(preferences);

        eventPublisher.publishEvent(TeamPreferenceChangedEvent.of(teamId, competitionId));

        return saved;
    }
}
