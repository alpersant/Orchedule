package com.orchedule.scheduling.planning.audit;

import com.orchedule.scheduling.planning.audit.AuditModel.AuditInput;
import com.orchedule.scheduling.planning.audit.AuditModel.FairnessRating;
import com.orchedule.scheduling.planning.audit.AuditModel.LeagueFairness;
import com.orchedule.scheduling.planning.audit.AuditModel.TeamFairness;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.orchedule.scheduling.planning.audit.AuditTestSupport.TEAM_A;
import static com.orchedule.scheduling.planning.audit.AuditTestSupport.TEAM_B;
import static com.orchedule.scheduling.planning.audit.AuditTestSupport.game;
import static com.orchedule.scheduling.planning.audit.AuditTestSupport.input;
import static com.orchedule.scheduling.planning.audit.AuditTestSupport.preferences;
import static org.assertj.core.api.Assertions.assertThat;

class FairnessCalculatorTest {

    private static final Set<DayOfWeek> TUE_WED = Set.of(DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY);
    private static final Set<LocalTime> SEVEN_EIGHT = Set.of(LocalTime.of(19, 0), LocalTime.of(20, 0));

    private final FairnessCalculator calculator = new FairnessCalculator();

    @Test
    void aPerfectlyRotatedTeamScoresOneHundred() {
        AuditInput input = input(rotatedGames(), Map.of(),
                Map.of(TEAM_A, preferences(TUE_WED, SEVEN_EIGHT)));

        TeamFairness team = teamA(input);

        assertThat(team.games()).isEqualTo(4);
        assertThat(team.dayEvenness()).isEqualTo(1.0);
        assertThat(team.hourEvenness()).isEqualTo(1.0);
        assertThat(team.streakViolations()).isZero();
        assertThat(team.firstOptionShare()).isEqualTo(1.0);
        assertThat(team.score()).isEqualTo(100.0);
        assertThat(team.gamesByDay()).containsEntry("TUESDAY", 2).containsEntry("WEDNESDAY", 2);
        assertThat(team.gamesByHour()).containsEntry("19:00", 2).containsEntry("20:00", 2);
    }

    @Test
    void aTeamThatAlwaysPlaysAtTheSameSlotScoresLow() {
        AuditInput input = input(List.of(
                        game(1, TEAM_A, TEAM_B, "2035-01-02T18:00:00Z"),
                        game(2, TEAM_A, TEAM_B, "2035-01-09T18:00:00Z"),
                        game(3, TEAM_A, TEAM_B, "2035-01-16T18:00:00Z"),
                        game(4, TEAM_A, TEAM_B, "2035-01-23T18:00:00Z")),
                Map.of(), Map.of(TEAM_A, preferences(TUE_WED, SEVEN_EIGHT)));

        TeamFairness team = teamA(input);

        assertThat(team.dayEvenness()).isZero();
        assertThat(team.hourEvenness()).isZero();
        assertThat(team.maxDayStreak()).isEqualTo(4);
        assertThat(team.maxHourStreak()).isEqualTo(4);
        assertThat(team.streakViolations()).isEqualTo(2);
        assertThat(team.score()).isEqualTo(15.0);
    }

    @Test
    void aSingleGameIsNotPenalisedForLackOfVariety() {
        AuditInput input = input(List.of(game(1, TEAM_A, TEAM_B, "2035-01-02T18:00:00Z")),
                Map.of(), Map.of(TEAM_A, preferences(TUE_WED, SEVEN_EIGHT)));

        assertThat(teamA(input).score()).isEqualTo(100.0);
    }

    @Test
    void withoutPreferencesTheFirstOptionComponentIsLeftOut() {
        AuditInput input = input(rotatedGames());

        TeamFairness team = teamA(input);

        assertThat(team.firstOptionShare()).isNull();
        assertThat(team.score()).isEqualTo(100.0);
    }

    @Test
    void aTeamWithoutGamesHasNoScoreAndDoesNotAffectTheLeague() {
        UUID idle = UUID.randomUUID();
        AuditInput base = input(rotatedGames());
        AuditInput withIdleTeam = new AuditInput(base.seasonId(), base.zone(), base.durationMinutes(),
                base.defaultRule(), base.teamRules(),
                withParticipant(base, idle, "Equipo inactivo"), base.preferences(), base.games(), 0, false);

        List<TeamFairness> teams = calculator.teams(withIdleTeam);
        TeamFairness idleTeam = teams.stream().filter(t -> t.teamId().equals(idle)).findFirst().orElseThrow();

        assertThat(idleTeam.games()).isZero();
        assertThat(idleTeam.score()).isNull();
        assertThat(calculator.league(teams).teamsEvaluated()).isEqualTo(2);
    }

    @Test
    void theLeagueSummaryUsesTheWorstTreatedTeamForTheRating() {
        List<TeamFairness> teams = List.of(team(90.0), team(60.0));

        LeagueFairness league = calculator.league(teams);

        assertThat(league.meanScore()).isEqualTo(75.0);
        assertThat(league.minScore()).isEqualTo(60.0);
        assertThat(league.maxScore()).isEqualTo(90.0);
        assertThat(league.gap()).isEqualTo(30.0);
        assertThat(league.standardDeviation()).isEqualTo(15.0);
        assertThat(league.rating()).isEqualTo(FairnessRating.ACCEPTABLE);
    }

    @Test
    void ratingThresholdsAreInclusive() {
        assertThat(FairnessCalculator.rating(85.0)).isEqualTo(FairnessRating.EXCELLENT);
        assertThat(FairnessCalculator.rating(70.0)).isEqualTo(FairnessRating.GOOD);
        assertThat(FairnessCalculator.rating(50.0)).isEqualTo(FairnessRating.ACCEPTABLE);
        assertThat(FairnessCalculator.rating(49.9)).isEqualTo(FairnessRating.NEEDS_ATTENTION);
    }

    @Test
    void anEmptyLeagueHasNoRating() {
        assertThat(calculator.league(List.of()).rating()).isEqualTo(FairnessRating.NOT_AVAILABLE);
    }

    private TeamFairness teamA(AuditInput input) {
        return calculator.teams(input).stream()
                .filter(t -> t.teamId().equals(TEAM_A)).findFirst().orElseThrow();
    }

    private static List<AuditModel.ScheduledGame> rotatedGames() {
        return List.of(
                game(1, TEAM_A, TEAM_B, "2035-01-02T18:00:00Z"),
                game(2, TEAM_A, TEAM_B, "2035-01-10T19:00:00Z"),
                game(3, TEAM_A, TEAM_B, "2035-01-16T19:00:00Z"),
                game(4, TEAM_A, TEAM_B, "2035-01-24T18:00:00Z"));
    }

    private static Map<UUID, String> withParticipant(AuditInput base, UUID team, String name) {
        Map<UUID, String> participants = new java.util.LinkedHashMap<>(base.participants());
        participants.put(team, name);
        return participants;
    }

    private static TeamFairness team(double score) {
        return new TeamFairness(UUID.randomUUID(), "Equipo", 4, Map.of(), Map.of(), 1, 1, 0,
                1.0, 1.0, 1.0, score);
    }
}
