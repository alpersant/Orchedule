package com.orchedule.scheduling.planning.audit;

import com.orchedule.scheduling.planning.audit.AuditModel.AuditInput;
import com.orchedule.scheduling.planning.audit.AuditModel.RuleResult;
import com.orchedule.scheduling.planning.audit.AuditModel.RuleStatus;
import com.orchedule.scheduling.planning.audit.AuditModel.TeamRule;
import com.orchedule.scheduling.planning.audit.AuditModel.Violation;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.orchedule.scheduling.planning.audit.AuditTestSupport.FIELD;
import static com.orchedule.scheduling.planning.audit.AuditTestSupport.FRI_19;
import static com.orchedule.scheduling.planning.audit.AuditTestSupport.TEAM_A;
import static com.orchedule.scheduling.planning.audit.AuditTestSupport.TEAM_B;
import static com.orchedule.scheduling.planning.audit.AuditTestSupport.THU_19;
import static com.orchedule.scheduling.planning.audit.AuditTestSupport.TUE_19;
import static com.orchedule.scheduling.planning.audit.AuditTestSupport.WED_19;
import static com.orchedule.scheduling.planning.audit.AuditTestSupport.game;
import static com.orchedule.scheduling.planning.audit.AuditTestSupport.input;
import static com.orchedule.scheduling.planning.audit.AuditTestSupport.preferences;
import static org.assertj.core.api.Assertions.assertThat;

class AuditRulesTest {

    private static UUID anotherTeam() {
        return UUID.randomUUID();
    }

    @Nested
    class RestPeriod {
        private final RestPeriodRule rule = new RestPeriodRule();

        @Test
        void flagsAFollowingGameBeforeTheMinimumRest() {
            AuditInput input = input(List.of(
                    game(1, TEAM_A, anotherTeam(), TUE_19),
                    game(2, anotherTeam(), TEAM_A, WED_19)));

            RuleResult result = rule.evaluate(input);

            assertThat(result.status()).isEqualTo(RuleStatus.FAIL);
            assertThat(result.violationCount()).isEqualTo(1);
            assertThat(result.violations().getFirst().teamId()).isEqualTo(TEAM_A);
            assertThat(result.violations().getFirst().roundNumber()).isEqualTo(2);
        }

        @Test
        void passesWhenGamesAreAWeekApart() {
            AuditInput input = input(List.of(
                    game(1, TEAM_A, anotherTeam(), TUE_19),
                    game(2, anotherTeam(), TEAM_A, "2035-01-09T18:00:00Z")));

            RuleResult result = rule.evaluate(input);

            assertThat(result.status()).isEqualTo(RuleStatus.PASS);
            assertThat(result.checked()).isEqualTo(1);
        }

        @Test
        void honoursATeamSpecificOverride() {
            AuditInput input = input(List.of(
                            game(1, TEAM_A, anotherTeam(), TUE_19),
                            game(2, anotherTeam(), TEAM_A, WED_19)),
                    Map.of(TEAM_A, new TeamRule(0, 2)), Map.of());

            assertThat(rule.evaluate(input).status()).isEqualTo(RuleStatus.PASS);
        }

        @Test
        void isNotApplicableWhenNoTeamPlaysTwice() {
            AuditInput input = input(List.of(game(1, TEAM_A, TEAM_B, TUE_19)));

            assertThat(rule.evaluate(input).status()).isEqualTo(RuleStatus.NOT_APPLICABLE);
        }
    }

    @Nested
    class MaxMatchesPerWeek {
        private final MaxMatchesPerWeekRule rule = new MaxMatchesPerWeekRule();

        @Test
        void flagsMoreMatchesThanAllowedInOneWeek() {
            AuditInput input = input(List.of(
                    game(1, TEAM_A, anotherTeam(), TUE_19),
                    game(2, TEAM_A, anotherTeam(), THU_19),
                    game(3, TEAM_A, anotherTeam(), FRI_19)));

            RuleResult result = rule.evaluate(input);

            assertThat(result.status()).isEqualTo(RuleStatus.FAIL);
            assertThat(result.violationCount()).isEqualTo(1);
            assertThat(result.violations().getFirst().teamId()).isEqualTo(TEAM_A);
        }

        @Test
        void treatsSundayAndMondayAsDifferentWeeks() {
            AuditInput input = input(List.of(
                            game(1, TEAM_A, anotherTeam(), "2035-01-07T19:00:00Z"),
                            game(2, TEAM_A, anotherTeam(), "2035-01-08T19:00:00Z")),
                    Map.of(TEAM_A, new TeamRule(0, 1)), Map.of());

            assertThat(rule.evaluate(input).status()).isEqualTo(RuleStatus.PASS);
        }
    }

    @Nested
    class RepetitionLimit {
        private final RepetitionLimitRule rule = new RepetitionLimitRule();

        @Test
        void flagsThreeConsecutiveRoundsOnTheSameWeekday() {
            AuditInput input = input(List.of(
                    game(1, TEAM_A, anotherTeam(), "2035-01-02T18:00:00Z"),
                    game(2, TEAM_A, anotherTeam(), "2035-01-09T19:00:00Z"),
                    game(3, TEAM_A, anotherTeam(), "2035-01-16T20:00:00Z")));

            RuleResult result = rule.evaluate(input);

            assertThat(result.status()).isEqualTo(RuleStatus.FAIL);
            assertThat(result.violationCount()).isEqualTo(1);
            assertThat(result.violations().getFirst().message()).contains("martes");
        }

        @Test
        void flagsThreeConsecutiveRoundsAtTheSameHour() {
            AuditInput input = input(List.of(
                    game(1, TEAM_A, anotherTeam(), TUE_19),
                    game(2, TEAM_A, anotherTeam(), WED_19),
                    game(3, TEAM_A, anotherTeam(), THU_19)));

            RuleResult result = rule.evaluate(input);

            assertThat(result.status()).isEqualTo(RuleStatus.FAIL);
            assertThat(result.violations().getFirst().message()).contains("19:00");
        }

        @Test
        void passesWhenDayAndHourKeepRotating() {
            AuditInput input = input(List.of(
                    game(1, TEAM_A, anotherTeam(), "2035-01-02T18:00:00Z"),
                    game(2, TEAM_A, anotherTeam(), "2035-01-10T19:00:00Z"),
                    game(3, TEAM_A, anotherTeam(), "2035-01-16T19:00:00Z")));

            assertThat(rule.evaluate(input).status()).isEqualTo(RuleStatus.PASS);
        }

        @Test
        void isNotApplicableWithFewerThanThreeGames() {
            AuditInput input = input(List.of(
                    game(1, TEAM_A, anotherTeam(), TUE_19),
                    game(2, TEAM_A, anotherTeam(), WED_19)));

            assertThat(rule.evaluate(input).status()).isEqualTo(RuleStatus.NOT_APPLICABLE);
        }
    }

    @Nested
    class PreferenceCompliance {
        private final PreferenceComplianceRule rule = new PreferenceComplianceRule();

        @Test
        void flagsGamesOutsideTheAllowedDaysAndReportsTeamsWithoutPreferences() {
            AuditInput input = input(
                    List.of(game(1, TEAM_A, TEAM_B, "2035-01-01T18:00:00Z")),
                    Map.of(),
                    Map.of(TEAM_A, preferences(
                            Set.of(DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY),
                            Set.of(LocalTime.of(19, 0), LocalTime.of(20, 0)))));

            RuleResult result = rule.evaluate(input);

            assertThat(result.status()).isEqualTo(RuleStatus.FAIL);
            assertThat(result.checked()).isEqualTo(1);
            assertThat(result.violations().getFirst().teamId()).isEqualTo(TEAM_A);
            assertThat(result.notes()).hasSize(1);
        }

        @Test
        void passesWhenEveryGameIsInsideThePreferences() {
            AuditInput input = input(
                    List.of(game(1, TEAM_A, TEAM_B, TUE_19)),
                    Map.of(),
                    Map.of(TEAM_A, preferences(Set.of(DayOfWeek.TUESDAY), Set.of(LocalTime.of(19, 0))),
                            TEAM_B, preferences(Set.of(DayOfWeek.TUESDAY), Set.of(LocalTime.of(19, 0)))));

            assertThat(rule.evaluate(input).status()).isEqualTo(RuleStatus.PASS);
        }

        @Test
        void isNotApplicableWithoutAnyValidPreferences() {
            AuditInput input = input(List.of(game(1, TEAM_A, TEAM_B, TUE_19)));

            RuleResult result = rule.evaluate(input);

            assertThat(result.status()).isEqualTo(RuleStatus.NOT_APPLICABLE);
            assertThat(result.notes()).isNotEmpty();
        }
    }

    @Nested
    class FieldOverlap {
        private final FieldOverlapRule rule = new FieldOverlapRule();

        @Test
        void flagsTwoGamesSharingAFieldAtTheSameTime() {
            AuditInput input = input(List.of(
                    game(1, TEAM_A, TEAM_B, FIELD, "2035-01-02T18:00:00Z"),
                    game(1, anotherTeam(), anotherTeam(), FIELD, "2035-01-02T18:30:00Z")));

            RuleResult result = rule.evaluate(input);

            assertThat(result.status()).isEqualTo(RuleStatus.FAIL);
            assertThat(result.violationCount()).isEqualTo(1);
        }

        @Test
        void allowsBackToBackGames() {
            AuditInput input = input(List.of(
                    game(1, TEAM_A, TEAM_B, FIELD, "2035-01-02T18:00:00Z"),
                    game(1, anotherTeam(), anotherTeam(), FIELD, "2035-01-02T19:00:00Z")));

            RuleResult result = rule.evaluate(input);

            assertThat(result.status()).isEqualTo(RuleStatus.PASS);
            assertThat(result.checked()).isEqualTo(1);
        }

        @Test
        void ignoresGamesWithoutField() {
            AuditInput input = input(List.of(
                    game(1, TEAM_A, TEAM_B, null, TUE_19),
                    game(1, anotherTeam(), anotherTeam(), null, TUE_19)));

            assertThat(rule.evaluate(input).status()).isEqualTo(RuleStatus.NOT_APPLICABLE);
        }
    }

    @Test
    void ruleResultKeepsTheRealCountButCapsTheDetail() {
        List<Violation> violations = new ArrayList<>();
        for (int i = 0; i < 60; i++) {
            violations.add(new Violation(null, null, i, "incumplimiento " + i));
        }

        RuleResult result = RuleResult.of("TEST", 100, violations, List.of());

        assertThat(result.violationCount()).isEqualTo(60);
        assertThat(result.violations()).hasSize(AuditModel.MAX_VIOLATIONS_PER_RULE);
        assertThat(result.status()).isEqualTo(RuleStatus.FAIL);
    }
}
