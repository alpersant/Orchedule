package com.orchedule.scheduling.planning.update;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Component
public class SchedulingRuleValidator {

    private final NamedParameterJdbcTemplate db;

    public SchedulingRuleValidator(NamedParameterJdbcTemplate db) {
        this.db = db;
    }

    public void assertConfirmable(UUID gameId) {
        var game = db.queryForMap("""
            SELECT g.id, g.round_id, g.scheduled_at, g.home_team_id, g.away_team_id,
                   r.season_id, r.week_number
              FROM fixture_game g
              JOIN fixture_round r ON r.id = g.round_id
             WHERE g.id = :gameId
            """, Map.of("gameId", gameId));

        if (game.get("scheduled_at") == null) {
            throw new IllegalArgumentException(
                    "A game must have a provisional time before confirmation"
            );
        }

        UUID seasonId = UUID.fromString(
                Objects.requireNonNull(game.get("season_id")).toString()
        );

        UUID homeId = UUID.fromString(
                Objects.requireNonNull(game.get("home_team_id")).toString()
        );

        UUID awayId = UUID.fromString(
                Objects.requireNonNull(game.get("away_team_id")).toString()
        );

        int week = ((Number) Objects.requireNonNull(game.get("week_number"))).intValue();
        Instant at = ((Timestamp) Objects.requireNonNull(game.get("scheduled_at"))).toInstant();

        assertRest(seasonId, homeId, gameId, at);
        assertRest(seasonId, awayId, gameId, at);
        assertWeeklyLimit(seasonId, homeId, week, gameId);
        assertWeeklyLimit(seasonId, awayId, week, gameId);
    }

    private void assertRest(UUID seasonId, UUID teamId, UUID gameId, Instant proposedAt) {
        int minRestHours = effectiveRule(seasonId, teamId)[0];

        var rows = db.queryForList("""
            SELECT g.id, g.scheduled_at
              FROM fixture_game g
              JOIN fixture_round r ON r.id = g.round_id
             WHERE r.season_id = :seasonId
               AND g.id <> :gameId
               AND (g.home_team_id = :teamId OR g.away_team_id = :teamId)
               AND g.scheduled_at IS NOT NULL
            """, Map.of(
                "seasonId", seasonId,
                "gameId", gameId,
                "teamId", teamId
        ));

        for (var row : rows) {
            Instant other = ((Timestamp) Objects.requireNonNull(row.get("scheduled_at"))).toInstant();
            Duration gap = Duration.between(proposedAt, other).abs();

            if (gap.compareTo(Duration.ofHours(minRestHours)) < 0) {
                throw new IllegalArgumentException(
                        "Team does not meet the minimum rest period of "
                                + minRestHours + " hours"
                );
            }
        }
    }

    private void assertWeeklyLimit(UUID seasonId, UUID teamId, int week, UUID gameId) {
        int max = effectiveRule(seasonId, teamId)[1];

        Integer count = db.queryForObject("""
            SELECT COUNT(*)
              FROM fixture_game g
              JOIN fixture_round r ON r.id = g.round_id
             WHERE r.season_id = :seasonId
               AND r.week_number = :week
               AND g.id <> :gameId
               AND (g.home_team_id = :teamId OR g.away_team_id = :teamId)
            """, Map.of(
                "seasonId", seasonId,
                "week", week,
                "gameId", gameId,
                "teamId", teamId
        ), Integer.class);

        if (count != null && count >= max) {
            throw new IllegalArgumentException(
                    "Team already has " + count + " games in week " + week
                            + "; maximum is " + max
            );
        }
    }

    private int[] effectiveRule(UUID seasonId, UUID teamId) {
        var rows = db.queryForList("""
            SELECT p.default_min_rest_hours,
                   p.default_max_matches_per_week,
                   t.min_rest_hours AS team_rest,
                   t.max_matches_per_week AS team_week
              FROM fixture_plan p
              LEFT JOIN fixture_team_rule t
                ON t.season_id = p.season_id
               AND t.team_id = :teamId
             WHERE p.season_id = :seasonId
            """, Map.of("seasonId", seasonId, "teamId", teamId));

        if (rows.isEmpty()) {
            throw new IllegalArgumentException("Season has no planning calendar");
        }

        var row = rows.getFirst();

        int minRestHours = row.get("team_rest") == null
                ? ((Number) Objects.requireNonNull(row.get("default_min_rest_hours"))).intValue()
                : ((Number) Objects.requireNonNull(row.get("team_rest"))).intValue();

        int maxMatchesPerWeek = row.get("team_week") == null
                ? ((Number) Objects.requireNonNull(row.get("default_max_matches_per_week"))).intValue()
                : ((Number) Objects.requireNonNull(row.get("team_week"))).intValue();

        return new int[] {minRestHours, maxMatchesPerWeek};
    }
}
