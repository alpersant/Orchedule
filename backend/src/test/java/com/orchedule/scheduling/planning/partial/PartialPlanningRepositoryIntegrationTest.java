package com.orchedule.scheduling.planning.partial;

import com.orchedule.scheduling.planning.partial.PlanningData.Block;
import com.orchedule.scheduling.planning.partial.PlanningData.History;
import com.orchedule.scheduling.planning.partial.PlanningData.Preferences;
import com.orchedule.scheduling.planning.partial.PlanningData.Round;
import com.orchedule.testsupport.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.orchedule.testsupport.PlanningFixtures.ZONE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PartialPlanningRepositoryIntegrationTest extends AbstractPostgresIntegrationTest {

    private static final LocalDate START = LocalDate.of(2035, 1, 1);

    private PartialPlanningRepository repo;

    @BeforeEach
    void createRepository() {
        repo = new PartialPlanningRepository(db);
    }

    @Test
    void preferences_acceptFirstAndSecondPriorities() {
        UUID competition = data.competition();
        UUID team = data.team("A", true);
        data.preference(team, competition, null, null);
        data.dayPreference(team, competition, DayOfWeek.TUESDAY, "FIRST");
        data.dayPreference(team, competition, DayOfWeek.MONDAY, "SECOND");
        data.hourPreference(team, competition, "H19_00", "FIRST");
        data.hourPreference(team, competition, "20:00", "SECOND");

        Preferences result = repo.preferences(competition, Set.of(team)).get(team);

        assertThat(result.valid()).isTrue();
        assertThat(result.days()).containsExactlyInAnyOrder(DayOfWeek.TUESDAY, DayOfWeek.MONDAY);
        assertThat(result.firstDays()).containsExactly(DayOfWeek.TUESDAY);
        assertThat(result.hours()).containsExactlyInAnyOrder(LocalTime.of(19, 0), LocalTime.of(20, 0));
        assertThat(result.firstHours()).containsExactly(LocalTime.of(19, 0));
    }

    @Test
    void preferences_rejectUnknownPriority() {
        UUID competition = data.competition();
        UUID team = data.team("A", true);
        data.preference(team, competition, null, null);
        data.dayPreference(team, competition, DayOfWeek.TUESDAY, "THIRD");
        data.hourPreference(team, competition, "19:00", "FIRST");

        Preferences result = repo.preferences(competition, Set.of(team)).get(team);

        assertThat(result.valid()).isFalse();
    }

    @Test
    void preferences_applyExcludedDayAndHour() {
        UUID competition = data.competition();
        UUID team = data.team("A", true);
        data.preference(team, competition, "MONDAY", "H19_00");
        data.dayPreference(team, competition, DayOfWeek.TUESDAY, "FIRST");
        data.dayPreference(team, competition, DayOfWeek.MONDAY, "SECOND");
        data.hourPreference(team, competition, "19:00", "FIRST");
        data.hourPreference(team, competition, "20:00", "FIRST");

        Preferences result = repo.preferences(competition, Set.of(team)).get(team);

        assertThat(result.valid()).isTrue();
        assertThat(result.days()).containsExactly(DayOfWeek.TUESDAY);
        assertThat(result.hours()).containsExactly(LocalTime.of(20, 0));
        assertThat(result.firstHours()).containsExactly(LocalTime.of(20, 0));
    }

    @Test
    void preferences_inactiveTeamIsNotValid() {
        UUID competition = data.competition();
        UUID team = data.team("A", false);
        data.preference(team, competition, null, null);
        data.dayPreference(team, competition, DayOfWeek.TUESDAY, "FIRST");
        data.hourPreference(team, competition, "19:00", "FIRST");

        assertThat(repo.preferences(competition, Set.of(team)).get(team).valid()).isFalse();
    }

    @Test
    void preferences_teamWithoutDaysIsNotValid() {
        UUID competition = data.competition();
        UUID team = data.team("A", true);
        data.preference(team, competition, null, null);
        data.hourPreference(team, competition, "19:00", "FIRST");

        assertThat(repo.preferences(competition, Set.of(team)).get(team).valid()).isFalse();
    }

    @Test
    void preferences_emptyTeamSetReturnsEmptyMap() {
        assertThat(repo.preferences(data.competition(), Set.of())).isEmpty();
    }

    @Test
    void lock_requiresAnActiveTransaction() {
        UUID season = seasonWithPlan();

        assertThatThrownBy(() -> repo.lock(season)).isInstanceOf(IllegalStateException.class);
        assertThatCode(() -> tx.executeWithoutResult(status -> repo.lock(season))).doesNotThrowAnyException();
    }

    @Test
    void updateStatus_rejectsStaleVersionWithConflict() {
        UUID season = seasonWithPlan();
        data.round(season, 1, 1, "UNSCHEDULED");

        tx.executeWithoutResult(status -> {
            Round round = repo.round(season, 1);
            repo.updateStatus(round, "DRAFT", false, false);

            assertThatThrownBy(() -> repo.updateStatus(round, "CONFIRMED", false, false))
                    .isInstanceOfSatisfying(ResponseStatusException.class,
                            e -> assertThat(e.getStatusCode().value()).isEqualTo(409));
        });
    }

    @Test
    void blocks_returnOnlyActiveBlockedRulesOverlappingTheWindow() {
        UUID team = data.team("A", true);
        Instant from = Instant.parse("2035-01-02T00:00:00Z");
        Instant to = from.plus(Duration.ofDays(1));

        data.blockRule("TEAM", team, from.plusSeconds(3600), from.plusSeconds(7200), "BLOCKED", true);
        data.blockRule("TEAM", team, from.plusSeconds(3600), from.plusSeconds(7200), "BLOCKED", false);
        data.blockRule("TEAM", team, from.plusSeconds(3600), from.plusSeconds(7200), "AVAILABLE", true);
        data.blockRule("TEAM", team, from.minus(Duration.ofDays(3)), from.minus(Duration.ofDays(2)), "BLOCKED", true);

        List<Block> blocks = repo.blocks(from, to);

        assertThat(blocks).hasSize(1);
        assertThat(blocks.getFirst().scope()).isEqualTo("TEAM");
        assertThat(blocks.getFirst().reference()).isEqualTo(team);
    }

    @Test
    void history_returnsScheduledDraftOrConfirmedRoundsInDescendingOrder() {
        UUID season = seasonWithPlan();
        UUID field = data.field(data.venue());
        UUID a = data.team("A", true);
        UUID b = data.team("B", true);
        Instant base = ZonedDateTime.of(START.plusDays(1), LocalTime.of(19, 0), ZONE).toInstant();

        UUID cancelled = data.round(season, 1, 1, "CANCELLED");
        UUID draft = data.round(season, 2, 2, "DRAFT");
        UUID confirmed = data.round(season, 3, 3, "CONFIRMED");
        data.round(season, 4, 4, "UNSCHEDULED");
        data.scheduledGame(cancelled, a, b, field, base);
        data.scheduledGame(draft, a, b, field, base.plus(Duration.ofDays(7)));
        data.scheduledGame(confirmed, a, b, field, base.plus(Duration.ofDays(14)));

        List<History> history = tx.execute(status -> repo.history(repo.round(season, 4)));

        assertThat(history).hasSize(4);
        assertThat(history.getFirst().number()).isEqualTo(3);
        assertThat(history.get(2).number()).isEqualTo(2);
        assertThat(history).extracting(History::number).containsOnly(2, 3);
    }

    private UUID seasonWithPlan() {
        UUID competition = data.competition();
        UUID season = data.season(competition, START, START.plusMonths(6));
        data.plan(season);
        return season;
    }
}
