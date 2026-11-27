package com.orchedule.scheduling.planning.partial;

import com.orchedule.scheduling.application.port.FieldAvailabilityPort;
import com.orchedule.scheduling.application.port.FieldScheduleSlot;
import com.orchedule.scheduling.application.port.FieldSchedulingContext;
import com.orchedule.scheduling.application.port.SeasonContextPort;
import com.orchedule.testsupport.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Timestamp;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Flujo completo asignar → confirmar → desconfirmar contra PostgreSQL real, incluidos los triggers
 * que protegen las reservas publicadas. Los servicios se construyen sin proxies de Spring, por lo
 * que las anotaciones de seguridad no se evalúan aquí: se cubren en los tests de la capa web.
 */
class GameAssignmentServiceIntegrationTest extends AbstractPostgresIntegrationTest {

    private static final LocalDate START = LocalDate.of(2035, 1, 1);
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2034-12-01T00:00:00Z"), ZoneOffset.UTC);
    private static final String TUESDAY_19 = "2035-01-02T19:00";
    private static final String MONDAY_19 = "2035-01-01T19:00";

    private GameAssignmentService service;
    private UUID season;
    private UUID field;
    private UUID round;
    private UUID gameOne;
    private UUID gameTwo;

    @BeforeEach
    void createScenario() {
        UUID competition = data.competition();
        season = data.season(competition, START, START.plusMonths(6));
        data.plan(season);
        field = data.field(data.venue());
        round = data.round(season, 1, 1, "UNSCHEDULED");
        gameOne = data.game(round, data.fullyAvailableTeam(competition, "A"), data.fullyAvailableTeam(competition, "B"));
        gameTwo = data.game(round, data.fullyAvailableTeam(competition, "C"), data.fullyAvailableTeam(competition, "D"));

        PartialPlanningRepository repo = new PartialPlanningRepository(db);
        SeasonContextPort seasons = new SeasonContextPort() {
            @Override
            public UUID getCompetitionIdForSeason(UUID seasonId) {
                return competition;
            }

            @Override
            public void assertSeasonIsSchedulable(UUID seasonId) {
                // La temporada de prueba siempre admite planificación.
            }
        };
        List<FieldScheduleSlot> slots = List.of(
                new FieldScheduleSlot(field, DayOfWeek.TUESDAY, LocalTime.of(19, 0)),
                new FieldScheduleSlot(field, DayOfWeek.TUESDAY, LocalTime.of(20, 0)),
                new FieldScheduleSlot(field, DayOfWeek.WEDNESDAY, LocalTime.of(19, 0)));
        FieldAvailabilityPort fields = (competitionId, seasonId, week) -> new FieldSchedulingContext(slots);

        service = new GameAssignmentService(db, repo, new PartialSlotResolver(repo, seasons, fields, CLOCK), seasons);
    }

    @Test
    void assign_storesFieldTimeAndAuditTrail() {
        tx.executeWithoutResult(status -> service.assign(season, gameOne, currentVersion(), TUESDAY_19, field));

        Map<String, Object> row = db.queryForMap("SELECT * FROM fixture_game WHERE id=:id", Map.of("id", gameOne));
        assertThat(row.get("field_id")).isEqualTo(field);
        assertThat(((Timestamp) row.get("scheduled_at")).toInstant()).isEqualTo(Instant.parse("2035-01-02T18:00:00Z"));
        assertThat(row.get("manual_assignment")).isEqualTo(true);
        assertThat(auditCount("ASSIGN_FIELD")).isEqualTo(1);
        assertThat(currentVersion()).isEqualTo(1L);
    }

    @Test
    void assign_rejectsSlotsOutsideTeamPreferences() {
        assertThatThrownBy(() -> tx.executeWithoutResult(
                status -> service.assign(season, gameOne, currentVersion(), MONDAY_19, field)))
                .isInstanceOfSatisfying(ResponseStatusException.class, e -> {
                    assertThat(e.getStatusCode().value()).isEqualTo(409);
                    assertThat(e.getBody().getDetail()).contains("preferencias");
                });

        assertThat(scheduledAt(gameOne)).isNull();
    }

    @Test
    void assign_rejectsAStaleRoundVersion() {
        tx.executeWithoutResult(status -> service.assign(season, gameOne, currentVersion(), TUESDAY_19, field));

        assertThatThrownBy(() -> tx.executeWithoutResult(
                status -> service.assign(season, gameTwo, 0L, "2035-01-02T20:00", field)))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode().value()).isEqualTo(409));
    }

    @Test
    void assign_rejectsDoubleBookingTheSameField() {
        tx.executeWithoutResult(status -> service.assign(season, gameOne, currentVersion(), TUESDAY_19, field));

        assertThatThrownBy(() -> tx.executeWithoutResult(
                status -> service.assign(season, gameTwo, currentVersion(), TUESDAY_19, field)))
                .isInstanceOfSatisfying(ResponseStatusException.class, e -> {
                    assertThat(e.getStatusCode().value()).isEqualTo(409);
                    assertThat(e.getBody().getDetail()).contains("otra reserva");
                });

        assertThat(scheduledAt(gameTwo)).isNull();
    }

    @Test
    void confirmAndUnconfirm_publishAndWithdrawTheMatch() {
        tx.executeWithoutResult(status -> service.assign(season, gameOne, currentVersion(), TUESDAY_19, field));

        tx.executeWithoutResult(status -> service.confirm(season, gameOne, null));

        assertThat(confirmationStatus(gameOne)).isEqualTo("CONFIRMED");
        Map<String, Object> match = db.queryForMap(
                "SELECT status, active FROM match_game WHERE id=:id", Map.of("id", gameOne));
        assertThat(match.get("status")).isEqualTo("CONFIRMED");
        assertThat(match.get("active")).isEqualTo(true);

        assertThatThrownBy(() -> tx.executeWithoutResult(
                status -> service.assign(season, gameOne, currentVersion(), "2035-01-02T20:00", field)))
                .isInstanceOfSatisfying(ResponseStatusException.class, e -> {
                    assertThat(e.getStatusCode().value()).isEqualTo(409);
                    assertThat(e.getBody().getDetail()).contains("Desconfirma");
                });

        tx.executeWithoutResult(status -> service.unconfirm(season, gameOne, null));

        assertThat(confirmationStatus(gameOne)).isEqualTo("PROVISIONAL");
        Map<String, Object> withdrawn = db.queryForMap(
                "SELECT status, active FROM match_game WHERE id=:id", Map.of("id", gameOne));
        assertThat(withdrawn.get("status")).isEqualTo("CANCELLED");
        assertThat(withdrawn.get("active")).isEqualTo(false);
    }

    private long currentVersion() {
        return data.roundVersion(round);
    }

    private Object scheduledAt(UUID game) {
        return db.queryForMap("SELECT scheduled_at FROM fixture_game WHERE id=:id", Map.of("id", game))
                .get("scheduled_at");
    }

    private String confirmationStatus(UUID game) {
        return db.queryForObject("SELECT confirmation_status FROM fixture_game WHERE id=:id",
                Map.of("id", game), String.class);
    }

    private int auditCount(String action) {
        Integer count = db.queryForObject(
                "SELECT count(*) FROM fixture_audit WHERE season_id=:season AND action=:action",
                Map.of("season", season, "action", action), Integer.class);
        return count == null ? 0 : count;
    }
}
