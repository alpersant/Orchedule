package com.orchedule.scheduling.planning.partial;

import com.orchedule.scheduling.application.port.FieldAvailabilityPort;
import com.orchedule.scheduling.application.port.FieldScheduleSlot;
import com.orchedule.scheduling.application.port.FieldSchedulingContext;
import com.orchedule.scheduling.application.port.SeasonContextPort;
import com.orchedule.scheduling.planning.partial.PlanningData.Counts;
import com.orchedule.scheduling.planning.partial.PlanningData.Options;
import com.orchedule.scheduling.planning.partial.PlanningData.Round;
import com.orchedule.testsupport.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/** Verifica el motor de huecos contra consultas SQL reales (bloqueos, reservas, rotación). */
class PartialSlotResolverIntegrationTest extends AbstractPostgresIntegrationTest {

    private static final LocalDate START = LocalDate.of(2035, 1, 1);
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2034-12-01T00:00:00Z"), ZoneOffset.UTC);

    private static final Instant TUESDAY_19 = Instant.parse("2035-01-02T18:00:00Z");
    private static final Instant TUESDAY_20 = Instant.parse("2035-01-02T19:00:00Z");
    private static final Instant WEDNESDAY_19 = Instant.parse("2035-01-03T18:00:00Z");

    private UUID competition;
    private UUID season;
    private UUID field;
    private UUID teamA;
    private UUID teamB;

    @BeforeEach
    void createScenario() {
        competition = data.competition();
        season = data.season(competition, START, START.plusMonths(6));
        data.plan(season);
        field = data.field(data.venue());
        teamA = data.fullyAvailableTeam(competition, "A");
        teamB = data.fullyAvailableTeam(competition, "B");
    }

    @Test
    void resolve_returnsEveryCompatibleSlot() {
        UUID game = createRoundOneGame();

        Options options = resolve(1, standardSlots());

        assertThat(instants(options, game)).containsExactlyInAnyOrder(TUESDAY_19, TUESDAY_20, WEDNESDAY_19);
        Counts counts = options.counts().get(game);
        assertThat(counts.total()).isEqualTo(3);
        assertThat(counts.dates()).isEqualTo(3);
        assertThat(counts.compatible()).isEqualTo(3);
        assertThat(counts.rotation()).isEqualTo(3);
        assertThat(counts.free()).isEqualTo(3);
    }

    @Test
    void resolve_respectsTeamPreferenceExclusions() {
        UUID restrictedTeam = data.team("R", true);
        data.preference(restrictedTeam, competition, "WEDNESDAY", null);
        data.dayPreference(restrictedTeam, competition, DayOfWeek.TUESDAY, "FIRST");
        data.dayPreference(restrictedTeam, competition, DayOfWeek.WEDNESDAY, "FIRST");
        data.hourPreference(restrictedTeam, competition, "19:00", "FIRST");
        data.hourPreference(restrictedTeam, competition, "20:00", "FIRST");
        UUID round = data.round(season, 1, 1, "UNSCHEDULED");
        UUID game = data.game(round, teamA, restrictedTeam);

        Options options = resolve(1, standardSlots());

        assertThat(instants(options, game)).containsExactlyInAnyOrder(TUESDAY_19, TUESDAY_20);
        assertThat(options.counts().get(game).compatible()).isEqualTo(2);
    }

    @Test
    void resolve_appliesTheRotationLimitUsingRealHistory() {
        UUID opponent = data.fullyAvailableTeam(competition, "C");
        UUID first = data.round(season, 1, 1, "DRAFT");
        UUID second = data.round(season, 2, 2, "DRAFT");
        data.scheduledGame(first, teamA, opponent, field, Instant.parse("2035-01-02T18:00:00Z"));
        data.scheduledGame(second, opponent, teamA, field, Instant.parse("2035-01-09T19:00:00Z"));
        UUID current = data.round(season, 3, 3, "UNSCHEDULED");
        UUID game = data.game(current, teamA, teamB);

        Options options = resolve(3, standardSlots());

        // Dos martes seguidos de A: se descartan los martes; el miércoles a las 19:00 sigue siendo válido.
        assertThat(instants(options, game)).containsExactly(Instant.parse("2035-01-17T18:00:00Z"));
        assertThat(options.counts().get(game).compatible()).isEqualTo(3);
        assertThat(options.counts().get(game).rotation()).isEqualTo(1);
    }

    @Test
    void resolve_discardsSlotsCoveredByATeamBlock() {
        UUID game = createRoundOneGame();
        data.blockRule("TEAM", teamA, Instant.parse("2035-01-02T17:30:00Z"),
                Instant.parse("2035-01-02T18:30:00Z"), "BLOCKED", true);

        Options options = resolve(1, standardSlots());

        assertThat(instants(options, game)).containsExactlyInAnyOrder(TUESDAY_20, WEDNESDAY_19);
        assertThat(options.counts().get(game).free()).isEqualTo(2);
    }

    @Test
    void resolve_discardsSlotsReservedByAnotherSeasonOnTheSameField() {
        UUID game = createRoundOneGame();
        UUID otherSeason = data.season(competition, START, START.plusMonths(6));
        data.plan(otherSeason);
        UUID otherRound = data.round(otherSeason, 1, 1, "DRAFT");
        data.scheduledGame(otherRound, data.team("E", true), data.team("F", true), field, TUESDAY_19);

        Options options = resolve(1, standardSlots());

        assertThat(instants(options, game)).containsExactlyInAnyOrder(TUESDAY_20, WEDNESDAY_19);
        assertThat(options.counts().get(game).compatible()).isEqualTo(3);
        assertThat(options.counts().get(game).free()).isEqualTo(2);
    }

    @Test
    void resolve_reportsAnIssueWhenTheWeekHasNoFieldSlots() {
        UUID game = createRoundOneGame();

        Options options = resolve(1, List.of());

        assertThat(options.candidates().get(game)).isEmpty();
        assertThat(options.issues().get(game).code()).isEqualTo("NO_FIELDS_ENABLED");
    }

    private UUID createRoundOneGame() {
        UUID round = data.round(season, 1, 1, "UNSCHEDULED");
        return data.game(round, teamA, teamB);
    }

    private List<FieldScheduleSlot> standardSlots() {
        return List.of(
                new FieldScheduleSlot(field, DayOfWeek.TUESDAY, LocalTime.of(19, 0)),
                new FieldScheduleSlot(field, DayOfWeek.TUESDAY, LocalTime.of(20, 0)),
                new FieldScheduleSlot(field, DayOfWeek.WEDNESDAY, LocalTime.of(19, 0)));
    }

    private Options resolve(int roundNumber, List<FieldScheduleSlot> slots) {
        return tx.execute(status -> {
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
            FieldAvailabilityPort fields = (competitionId, seasonId, week) -> new FieldSchedulingContext(slots);
            PartialSlotResolver resolver = new PartialSlotResolver(repo, seasons, fields, CLOCK);

            Round round = repo.round(season, roundNumber);
            return resolver.resolve(round, repo.games(round));
        });
    }

    private static List<Instant> instants(Options options, UUID game) {
        return options.candidates().get(game).stream().map(slot -> slot.at()).toList();
    }
}
