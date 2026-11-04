package com.orchedule.scheduling.planning.update;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orchedule.scheduling.planning.update.SchedulingUpdateModels.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class SchedulingUpdateService {
    private final SchedulingUpdateRepository repository;
    private final SchedulingRuleValidator validator;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SchedulingUpdateService(
        SchedulingUpdateRepository repository,
        SchedulingRuleValidator validator
    ) {
        this.repository = repository;
        this.validator = validator;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public SeasonRules seasonRules(UUID seasonId) {
        return repository.seasonRules(seasonId);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public SeasonRules updateSeasonRules(UUID seasonId, UpdateSeasonRulesRequest request) {
        validate(request.defaultMinRestHours(), request.defaultMaxMatchesPerWeek());
        var previous = repository.seasonRules(seasonId);
        var updated = repository.updateSeasonRules(seasonId, request);
        history(seasonId, null, "UPDATE_SEASON_RULES", "Actualizar reglas de temporada",
            "SUCCESS", "Reglas de temporada actualizadas", previous, updated);
        return updated;
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public TeamRule saveTeamRule(UUID seasonId, UUID teamId, UpdateTeamRuleRequest request) {
        validate(request.minRestHours(), request.maxMatchesPerWeek());
        var updated = repository.saveTeamRule(seasonId, teamId, request);
        history(seasonId, null, "SAVE_TEAM_RULE", "Guardar regla de equipo",
            "SUCCESS", "Regla específica guardada para el equipo", null, updated);
        return updated;
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public GameConfirmation confirm(UUID gameId) {
        var seasonId = requireSeason(gameId);
        validator.assertConfirmable(gameId);
        var updated = repository.confirm(gameId, actor());
        history(seasonId, gameId, "CONFIRM_GAME", "Confirmar partido",
            "SUCCESS", "Partido confirmado individualmente", null, updated);
        return updated;
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public GameConfirmation unconfirm(UUID gameId) {
        var seasonId = requireSeason(gameId);
        var previous = repository.confirmation(gameId);
        var updated = repository.unconfirm(gameId, actor());
        history(seasonId, gameId, "UNCONFIRM_GAME", "Desconfirmar partido",
            "SUCCESS", "Partido devuelto a provisional", previous, updated);
        return updated;
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public Alternative createAlternative(UUID gameId, CreateAlternativeRequest request) {
        var seasonId = requireSeason(gameId);
        if (repository.isConfirmed(gameId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "A confirmed game cannot receive an alternative");
        }
        if (request.alternativeWeek() < 1) {
            throw new IllegalArgumentException("Alternative week must be positive");
        }
        if (request.proposedDay() == null || request.proposedDay().isBlank()) {
            throw new IllegalArgumentException("Proposed day is required");
        }
        if (request.proposedTime() == null) {
            throw new IllegalArgumentException("Proposed time is required");
        }
        var created = repository.createAlternative(gameId, request, actor());
        history(seasonId, gameId, "CREATE_ALTERNATIVE", "Proponer alternativa",
            "SUCCESS", "Alternativa creada en otra semana", null, created);
        return created;
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public Alternative decide(UUID alternativeId, boolean accepted) {
        var alternative = repository.alternative(alternativeId);
        var seasonId = requireSeason(alternative.gameId());
        var updated = repository.decide(alternativeId, accepted, actor());
        history(seasonId, alternative.gameId(),
            accepted ? "ACCEPT_ALTERNATIVE" : "REJECT_ALTERNATIVE",
            accepted ? "Aceptar alternativa" : "Rechazar alternativa",
            "SUCCESS",
            accepted ? "Alternativa aceptada" : "Alternativa rechazada",
            alternative, updated);
        return updated;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public List<Alternative> alternatives(UUID gameId) {
        return repository.alternatives(gameId);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public List<HistoryEntry> history(UUID seasonId) {
        return repository.history(seasonId, 200);
    }

    private void validate(Integer rest, Integer week) {
        if (rest != null && rest < 0) {
            throw new IllegalArgumentException("Rest hours cannot be negative");
        }
        if (week != null && week < 1) {
            throw new IllegalArgumentException("Matches per week must be at least 1");
        }
    }

    private UUID requireSeason(UUID gameId) {
        return repository.gameId(gameId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));
    }

    private String actor() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication == null ? "system" : authentication.getName();
    }

    private void history(
        UUID seasonId,
        UUID gameId,
        String action,
        String operation,
        String result,
        String detail,
        Object previous,
        Object next
    ) {
        repository.history(
            seasonId,
            gameId,
            action,
            operation,
            result,
            actor(),
            detail,
            toJson(previous),
            toJson(next)
        );
    }

    private JsonNode toJson(Object value) {
        return value == null ? null : objectMapper.valueToTree(value);
    }
}
