package com.orchedule.scheduling.planning.audit;

import com.orchedule.scheduling.application.port.SeasonContextPort;
import com.orchedule.scheduling.planning.audit.AuditModel.AuditInput;
import com.orchedule.scheduling.planning.audit.AuditModel.ScheduledGame;
import com.orchedule.scheduling.planning.audit.AuditModel.TeamRule;
import com.orchedule.scheduling.planning.partial.PartialPlanningRepository;
import com.orchedule.scheduling.planning.partial.PlanningData.Preferences;
import com.orchedule.shared.jdbc.JdbcValues;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Adaptador JDBC de solo lectura. Todas las consultas están parametrizadas y no usan bloqueos,
 * de modo que pueden ejecutarse en una transacción {@code readOnly}.
 */
@Repository
public class JdbcAuditData implements AuditDataPort {

    private static final String CONFIRMED = "CONFIRMED";
    private static final String DRAFT = "DRAFT";

    private final NamedParameterJdbcTemplate db;
    private final PartialPlanningRepository preferencesRepository;
    private final SeasonContextPort seasons;

    public JdbcAuditData(NamedParameterJdbcTemplate db, PartialPlanningRepository preferencesRepository,
                         SeasonContextPort seasons) {
        this.db = db;
        this.preferencesRepository = preferencesRepository;
        this.seasons = seasons;
    }

    @Override
    public AuditInput load(UUID seasonId, boolean includeDraft) {
        var plans = db.queryForList("""
                SELECT zone_id, duration_minutes, default_min_rest_hours, default_max_matches_per_week
                FROM fixture_plan WHERE season_id=:season
                """, Map.of("season", seasonId));
        if (plans.isEmpty()) {
            throw new SeasonCalendarNotFoundException(seasonId);
        }

        var plan = plans.getFirst();
        ZoneId zone = ZoneId.of((String) plan.get("zone_id"));
        int duration = JdbcValues.requiredInt(plan, "duration_minutes");
        TeamRule defaultRule = new TeamRule(
                JdbcValues.requiredInt(plan, "default_min_rest_hours"),
                JdbcValues.requiredInt(plan, "default_max_matches_per_week"));

        List<ScheduledGame> games = readGames(seasonId, includeDraft);

        Map<UUID, String> participants = readParticipants(seasonId);
        for (ScheduledGame game : games) {
            participants.putIfAbsent(game.home(), game.homeName());
            participants.putIfAbsent(game.away(), game.awayName());
        }

        Map<UUID, Preferences> preferences = participants.isEmpty()
                ? Map.of()
                : preferencesRepository.readPreferences(
                seasons.getCompetitionIdForSeason(seasonId), Set.copyOf(participants.keySet()));

        return new AuditInput(seasonId, zone, duration, defaultRule, readTeamRules(seasonId),
                participants, preferences, games, countPending(seasonId), includeDraft);
    }

    private List<ScheduledGame> readGames(UUID seasonId, boolean includeDraft) {
        List<String> statuses = includeDraft ? List.of(CONFIRMED, DRAFT) : List.of(CONFIRMED);

        var rows = db.queryForList("""
                SELECT g.id AS game_id, r.round_number, r.status AS round_status,
                       g.home_team_id, g.away_team_id, h.name AS home_name, a.name AS away_name,
                       g.field_id, g.scheduled_at
                FROM fixture_game g
                JOIN fixture_round r ON r.id = g.round_id
                JOIN team h ON h.id = g.home_team_id
                JOIN team a ON a.id = g.away_team_id
                WHERE r.season_id = :season AND r.status IN (:statuses) AND g.scheduled_at IS NOT NULL
                ORDER BY r.round_number, g.scheduled_at, g.id
                """, Map.of("season", seasonId, "statuses", statuses));

        List<ScheduledGame> games = new ArrayList<>(rows.size());
        for (var row : rows) {
            games.add(new ScheduledGame(
                    (UUID) row.get("game_id"),
                    JdbcValues.requiredInt(row, "round_number"),
                    (String) row.get("round_status"),
                    (UUID) row.get("home_team_id"),
                    (UUID) row.get("away_team_id"),
                    (String) row.get("home_name"),
                    (String) row.get("away_name"),
                    (UUID) row.get("field_id"),
                    PartialPlanningRepository.instant(row.get("scheduled_at"))));
        }
        return games;
    }

    private Map<UUID, String> readParticipants(UUID seasonId) {
        Map<UUID, String> participants = new LinkedHashMap<>();
        db.queryForList("""
                SELECT p.team_id, t.name FROM fixture_participant p
                JOIN team t ON t.id = p.team_id WHERE p.season_id = :season ORDER BY t.name
                """, Map.of("season", seasonId))
                .forEach(row -> participants.put((UUID) row.get("team_id"), (String) row.get("name")));
        return participants;
    }

    private Map<UUID, TeamRule> readTeamRules(UUID seasonId) {
        Map<UUID, TeamRule> rules = new HashMap<>();
        db.queryForList("""
                SELECT team_id, min_rest_hours, max_matches_per_week
                FROM fixture_team_rule WHERE season_id = :season
                """, Map.of("season", seasonId))
                .forEach(row -> rules.put((UUID) row.get("team_id"), new TeamRule(
                        JdbcValues.requiredInt(row, "min_rest_hours"),
                        JdbcValues.requiredInt(row, "max_matches_per_week"))));
        return rules;
    }

    private int countPending(UUID seasonId) {
        Integer pending = db.queryForObject("""
                SELECT count(*) FROM fixture_game g JOIN fixture_round r ON r.id = g.round_id
                WHERE r.season_id = :season AND r.status <> 'CANCELLED' AND g.scheduled_at IS NULL
                """, Map.of("season", seasonId), Integer.class);
        return pending == null ? 0 : pending;
    }
}
