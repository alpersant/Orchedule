package com.orchedule.scheduling.planning.update;

import com.fasterxml.jackson.databind.JsonNode;
import com.orchedule.scheduling.planning.update.SchedulingUpdateModels.*;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import java.time.ZoneOffset;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.*;


@Repository
public class SchedulingUpdateRepository {

    private final NamedParameterJdbcTemplate db;

    public SchedulingUpdateRepository(NamedParameterJdbcTemplate db) {
        this.db = db;
    }

    @Transactional(readOnly = true)
    public SeasonRules seasonRules(UUID seasonId) {
        var rows = db.queryForList("""
            SELECT default_min_rest_hours, default_max_matches_per_week
              FROM fixture_plan
             WHERE season_id = :seasonId
            """, Map.of("seasonId", seasonId));

        if (rows.isEmpty()) {
            throw new IllegalArgumentException("Season has no planning calendar");
        }

        var row = rows.getFirst();

        return new SeasonRules(
                seasonId,
                ((Number) Objects.requireNonNull(row.get("default_min_rest_hours"))).intValue(),
                ((Number) Objects.requireNonNull(row.get("default_max_matches_per_week"))).intValue()
        );
    }

    @Transactional
    public SeasonRules updateSeasonRules(UUID seasonId, UpdateSeasonRulesRequest request) {
        var params = new HashMap<String, Object>();
        params.put("seasonId", seasonId);
        params.put("rest", request.defaultMinRestHours());
        params.put("week", request.defaultMaxMatchesPerWeek());

        db.update("""
            UPDATE fixture_plan
               SET default_min_rest_hours = COALESCE(:rest, default_min_rest_hours),
                   default_max_matches_per_week = COALESCE(:week, default_max_matches_per_week)
             WHERE season_id = :seasonId
            """, params);

        return seasonRules(seasonId);
    }

    @Transactional
    public TeamRule saveTeamRule(UUID seasonId, UUID teamId, UpdateTeamRuleRequest request) {
        var season = seasonRules(seasonId);

        int rest = Optional.ofNullable(request.minRestHours())
                .orElse(season.defaultMinRestHours());

        int week = Optional.ofNullable(request.maxMatchesPerWeek())
                .orElse(season.defaultMaxMatchesPerWeek());

        db.update("""
            INSERT INTO fixture_team_rule
                (season_id, team_id, min_rest_hours, max_matches_per_week)
            VALUES (:seasonId, :teamId, :rest, :week)
            ON CONFLICT (season_id, team_id) DO UPDATE
               SET min_rest_hours = EXCLUDED.min_rest_hours,
                   max_matches_per_week = EXCLUDED.max_matches_per_week,
                   updated_at = now()
            """, Map.of(
                "seasonId", seasonId,
                "teamId", teamId,
                "rest", rest,
                "week", week
        ));

        return new TeamRule(seasonId, teamId, rest, week);
    }

    @Transactional(readOnly = true)
    public Optional<TeamRule> teamRule(UUID seasonId, UUID teamId) {
        var rows = db.queryForList("""
            SELECT min_rest_hours, max_matches_per_week
              FROM fixture_team_rule
             WHERE season_id = :seasonId
               AND team_id = :teamId
            """, Map.of("seasonId", seasonId, "teamId", teamId));

        if (rows.isEmpty()) {
            return Optional.empty();
        }

        var row = rows.getFirst();

        return Optional.of(new TeamRule(
                seasonId,
                teamId,
                ((Number) Objects.requireNonNull(row.get("min_rest_hours"))).intValue(),
                ((Number) Objects.requireNonNull(row.get("max_matches_per_week"))).intValue()
        ));
    }

    @Transactional(readOnly = true)
    public Optional<UUID> gameId(UUID gameId) {
        var rows = db.queryForList(
                "SELECT r.season_id FROM fixture_game g JOIN fixture_round r ON r.id=g.round_id WHERE g.id = :id",
                Map.of("id", gameId)
        );

        if (rows.isEmpty()) {
            return Optional.empty();
        }

        Object seasonId = Objects.requireNonNull(rows.getFirst().get("season_id"));

        return Optional.of(UUID.fromString(seasonId.toString()));
    }

    @Transactional(readOnly = true)
    public boolean isConfirmed(UUID gameId) {
        Boolean confirmed = db.queryForObject("""
            SELECT confirmation_status = 'CONFIRMED'
              FROM fixture_game
             WHERE id = :id
            """, Map.of("id", gameId), Boolean.class);

        return Boolean.TRUE.equals(confirmed);
    }

    @Transactional
    public GameConfirmation confirm(UUID gameId, String actor) {
        db.update("""
            UPDATE fixture_game
               SET confirmation_status = 'CONFIRMED',
                   confirmed_at = now(),
                   confirmed_by = :actor
             WHERE id = :id
            """, Map.of("id", gameId, "actor", actor));

        return confirmation(gameId);
    }

    @Transactional
    public GameConfirmation unconfirm(UUID gameId, String actor) {
        db.update("""
            UPDATE fixture_game
               SET confirmation_status = 'PROVISIONAL',
                   confirmed_at = NULL,
                   confirmed_by = NULL
             WHERE id = :id
            """, Map.of("id", gameId, "actor", actor));

        return confirmation(gameId);
    }

    @Transactional(readOnly = true)
    public GameConfirmation confirmation(UUID gameId) {
        var row = db.queryForMap("""
            SELECT confirmation_status, confirmed_at, confirmed_by
              FROM fixture_game
             WHERE id = :id
            """, Map.of("id", gameId));

        return new GameConfirmation(
                gameId,
                String.valueOf(row.get("confirmation_status")),
                toOffset(row.get("confirmed_at")),
                (String) row.get("confirmed_by")
        );
    }

    @Transactional
    public Alternative createAlternative(UUID gameId, CreateAlternativeRequest request, String actor) {
        var id = UUID.randomUUID();

        db.update("""
            INSERT INTO fixture_game_alternative
                (id, game_id, alternative_week, proposed_day, proposed_time, status, reason, created_by)
            VALUES
                (:id, :gameId, :week, :day, :time, 'PROPOSED', :reason, :actor)
            """, new MapSqlParameterSource()
                .addValue("id", id).addValue("gameId", gameId)
                .addValue("week", request.alternativeWeek())
                .addValue("day", request.proposedDay())
                .addValue("time", request.proposedTime())
                .addValue("reason", request.reason())
                .addValue("actor", actor));

        return alternative(id);
    }

    @Transactional(readOnly = true)
    public Alternative alternative(UUID id) {
        var row = db.queryForMap("""
            SELECT *
              FROM fixture_game_alternative
             WHERE id = :id
            """, Map.of("id", id));

        return toAlternative(row);
    }

    @Transactional(readOnly = true)
    public List<Alternative> alternatives(UUID gameId) {
        return db.queryForList("""
            SELECT *
              FROM fixture_game_alternative
             WHERE game_id = :id
             ORDER BY created_at DESC
            """, Map.of("id", gameId))
                .stream()
                .map(this::toAlternative)
                .toList();
    }

    @Transactional
    public Alternative decide(UUID alternativeId, boolean accepted, String actor) {
        db.update("""
            UPDATE fixture_game_alternative
               SET status = :status,
                   decided_by = :actor,
                   decided_at = now()
             WHERE id = :id
               AND status = 'PROPOSED'
            """, Map.of(
                "id", alternativeId,
                "status", accepted ? "ACCEPTED" : "REJECTED",
                "actor", actor
        ));

        return alternative(alternativeId);
    }

    @Transactional
    public void history(
            UUID seasonId,
            UUID gameId,
            String action,
            String operation,
            String result,
            String actor,
            String detail,
            JsonNode previousValue,
            JsonNode newValue
    ) {
        db.update("""
            INSERT INTO fixture_audit
                (id, season_id, fixture_game_id, action, operation, result, actor, detail, previous_value, new_value)
            VALUES
                (:id, :seasonId, :gameId, :action, :operation, :result, :actor, :detail, CAST(:previous AS jsonb), CAST(:new AS jsonb))
            """, new MapSqlParameterSource()
                .addValue("id", UUID.randomUUID())
                .addValue("seasonId", seasonId)
                .addValue("gameId", gameId)
                .addValue("action", action)
                .addValue("operation", operation)
                .addValue("result", result)
                .addValue("actor", actor)
                .addValue("detail", detail)
                .addValue("previous", previousValue == null ? null : previousValue.toString())
                .addValue("new", newValue == null ? null : newValue.toString()));
    }

    @Transactional(readOnly = true)
    public List<HistoryEntry> history(UUID seasonId, int limit) {
        return db.queryForList("""
            SELECT id, season_id, fixture_game_id, action, operation, result,
                   actor, created_at, detail
              FROM fixture_audit
             WHERE season_id = :seasonId
             ORDER BY created_at DESC
             LIMIT :limit
            """, Map.of("seasonId", seasonId, "limit", limit))
                .stream()
                .map(row -> new HistoryEntry(
                        UUID.fromString(Objects.requireNonNull(row.get("id")).toString()),
                        UUID.fromString(Objects.requireNonNull(row.get("season_id")).toString()),
                        row.get("fixture_game_id") == null
                                ? null
                                : UUID.fromString(Objects.requireNonNull(row.get("fixture_game_id")).toString()),
                        (String) row.get("action"),
                        (String) row.get("operation"),
                        (String) row.get("result"),
                        (String) row.get("actor"),
                        toOffset(row.get("created_at")),
                        (String) row.get("detail")
                ))
                .toList();
    }

    private Alternative toAlternative(Map<String, Object> row) {
        return new Alternative(
                UUID.fromString(row.get("id").toString()),
                UUID.fromString(row.get("game_id").toString()),
                ((Number) row.get("alternative_week")).intValue(),
                String.valueOf(row.get("proposed_day")),
                LocalTime.parse(String.valueOf(row.get("proposed_time"))),
                String.valueOf(row.get("status")),
                (String) row.get("reason"),
                (String) row.get("created_by"),
                toOffset(row.get("created_at")),
                (String) row.get("decided_by"),
                toOffset(row.get("decided_at"))
        );
    }

    private OffsetDateTime toOffset(Object value) {
        return switch (value) {
            case null -> null;
            case OffsetDateTime offsetDateTime -> offsetDateTime;
            case Timestamp timestamp -> timestamp.toInstant().atOffset(ZoneOffset.UTC);
            case Instant instant -> instant.atOffset(ZoneOffset.UTC);
            default -> OffsetDateTime.parse(value.toString());
        };
    }
}
