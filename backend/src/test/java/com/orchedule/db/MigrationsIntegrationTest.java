package com.orchedule.db;

import com.orchedule.testsupport.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessException;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Comprueba que las migraciones Flyway se aplican en PostgreSQL y que sus invariantes se cumplen. */
class MigrationsIntegrationTest extends AbstractPostgresIntegrationTest {

    @Test
    void allMigrationsAreAppliedSuccessfully() {
        Integer failed = db.queryForObject(
                "SELECT count(*) FROM flyway_schema_history WHERE success = false", Map.of(), Integer.class);
        Integer applied = db.queryForObject(
                "SELECT count(*) FROM flyway_schema_history WHERE success = true", Map.of(), Integer.class);

        assertThat(failed).isZero();
        assertThat(applied).isNotNull().isGreaterThanOrEqualTo(9);
    }

    @Test
    void planningTablesExist() {
        List<String> tables = db.getJdbcTemplate().queryForList(
                "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public'", String.class);

        assertThat(tables).contains(
                "season", "team", "field", "match_game", "fixture_plan", "fixture_round",
                "fixture_game", "fixture_game_issue", "fixture_audit", "fixture_team_rule",
                "fixture_game_alternative");
    }

    @Test
    void legacyMatchWritesAreBlockedForSeasonsManagedByThePlanner() {
        UUID competition = data.competition();
        UUID season = data.season(competition, LocalDate.of(2035, 1, 1), LocalDate.of(2035, 6, 30));
        data.plan(season);
        UUID venue = data.venue();
        UUID home = data.team("A", true);
        UUID away = data.team("B", true);

        assertThatThrownBy(() -> db.update("""
                INSERT INTO match_game(id,season_id,home_team_id,away_team_id,venue_id,scheduled_at,status,round_number,notes,active,created_at,updated_at)
                VALUES(:id,:season,:home,:away,:venue,:at,'CONFIRMED',1,NULL,true,now(),now())
                """, Map.of("id", UUID.randomUUID(), "season", season, "home", home, "away", away,
                "venue", venue, "at", Timestamp.from(Instant.parse("2035-01-02T18:00:00Z")))))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining("Use the fixture planner");
    }

    @Test
    void confirmedGamesCannotChangeTheirAssignment() {
        UUID competition = data.competition();
        UUID season = data.season(competition, LocalDate.of(2035, 1, 1), LocalDate.of(2035, 6, 30));
        data.plan(season);
        UUID round = data.round(season, 1, 1, "DRAFT");
        UUID field = data.field(data.venue());
        UUID game = data.scheduledGame(round, data.team("A", true), data.team("B", true), field,
                Instant.parse("2035-01-02T18:00:00Z"));

        db.update("UPDATE fixture_game SET confirmation_status='CONFIRMED' WHERE id=:id", Map.of("id", game));

        assertThatThrownBy(() -> db.update("UPDATE fixture_game SET scheduled_at=:at WHERE id=:id",
                Map.of("id", game, "at", Timestamp.from(Instant.parse("2035-01-02T19:00:00Z")))))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining("Desconfirma el partido");
    }
}
