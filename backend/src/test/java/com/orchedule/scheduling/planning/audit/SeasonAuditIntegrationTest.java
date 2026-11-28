package com.orchedule.scheduling.planning.audit;

import com.orchedule.scheduling.application.port.SeasonContextPort;
import com.orchedule.scheduling.planning.audit.AuditModel.AuditReport;
import com.orchedule.scheduling.planning.audit.AuditModel.RuleResult;
import com.orchedule.scheduling.planning.audit.AuditModel.RuleStatus;
import com.orchedule.scheduling.planning.partial.PartialPlanningRepository;
import com.orchedule.testsupport.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** La auditoría completa contra PostgreSQL real: lectura, reglas y equidad. */
class SeasonAuditIntegrationTest extends AbstractPostgresIntegrationTest {

    private static final LocalDate START = LocalDate.of(2035, 1, 1);

    // Madrid en enero es UTC+1: 19:00 locales = 18:00Z.
    private static final Instant MON_19 = Instant.parse("2035-01-01T18:00:00Z");
    private static final Instant TUE_19 = Instant.parse("2035-01-02T18:00:00Z");
    private static final Instant WED_19 = Instant.parse("2035-01-03T18:00:00Z");
    private static final Instant TUE_20_NEXT_WEEK = Instant.parse("2035-01-09T19:00:00Z");
    private static final Instant WED_20_NEXT_WEEK = Instant.parse("2035-01-10T19:00:00Z");

    private UUID competition;
    private UUID season;
    private UUID field;
    private UUID teamA;
    private UUID teamB;
    private UUID teamC;
    private UUID teamD;
    private SeasonAuditService service;

    @BeforeEach
    void createScenario() {
        competition = data.competition();
        season = data.season(competition, START, START.plusMonths(6));
        data.plan(season);
        field = data.field(data.venue());
        teamA = data.fullyAvailableTeam(competition, "A");
        teamB = data.fullyAvailableTeam(competition, "B");
        teamC = data.fullyAvailableTeam(competition, "C");
        teamD = data.fullyAvailableTeam(competition, "D");
        for (UUID team : List.of(teamA, teamB, teamC, teamD)) {
            data.participant(season, team);
        }

        SeasonContextPort seasons = new SeasonContextPort() {
            @Override
            public UUID getCompetitionIdForSeason(UUID seasonId) {
                return competition;
            }

            @Override
            public void assertSeasonIsSchedulable(UUID seasonId) {
                // No se usa en la auditoría.
            }
        };
        service = new SeasonAuditService(
                new JdbcAuditData(db, new PartialPlanningRepository(db), seasons),
                List.of(new RestPeriodRule(), new MaxMatchesPerWeekRule(), new RepetitionLimitRule(),
                        new PreferenceComplianceRule(), new FieldOverlapRule()),
                new FairnessCalculator());
    }

    @Test
    void aCompliantCalendarPassesEveryApplicableRule() {
        UUID first = data.round(season, 1, 1, "CONFIRMED");
        data.scheduledGame(first, teamA, teamB, field, TUE_19);
        data.scheduledGame(first, teamC, teamD, field, WED_19);
        UUID second = data.round(season, 2, 2, "CONFIRMED");
        data.scheduledGame(second, teamA, teamC, field, TUE_20_NEXT_WEEK);
        data.scheduledGame(second, teamB, teamD, field, WED_20_NEXT_WEEK);

        AuditReport report = service.audit(season, false);

        assertThat(report.compliant()).isTrue();
        assertThat(report.evaluatedGames()).isEqualTo(4);
        assertThat(report.pendingGames()).isZero();
        assertThat(report.zoneId()).isEqualTo("Europe/Madrid");
        assertThat(status(report, RestPeriodRule.CODE)).isEqualTo(RuleStatus.PASS);
        assertThat(status(report, MaxMatchesPerWeekRule.CODE)).isEqualTo(RuleStatus.PASS);
        assertThat(status(report, RepetitionLimitRule.CODE)).isEqualTo(RuleStatus.NOT_APPLICABLE);
        assertThat(status(report, PreferenceComplianceRule.CODE)).isEqualTo(RuleStatus.PASS);
        assertThat(status(report, FieldOverlapRule.CODE)).isEqualTo(RuleStatus.PASS);
        assertThat(report.teams()).hasSize(4).allSatisfy(team -> {
            assertThat(team.games()).isEqualTo(2);
            assertThat(team.score()).isNotNull();
        });
    }

    @Test
    void insufficientRestIsReported() {
        UUID round = data.round(season, 1, 1, "CONFIRMED");
        data.scheduledGame(round, teamA, teamB, field, TUE_19);
        data.scheduledGame(round, teamA, teamC, field, WED_19);

        AuditReport report = service.audit(season, false);

        assertThat(report.compliant()).isFalse();
        RuleResult rest = rule(report, RestPeriodRule.CODE);
        assertThat(rest.status()).isEqualTo(RuleStatus.FAIL);
        assertThat(rest.violations()).anyMatch(v -> teamA.equals(v.teamId()));
    }

    @Test
    void aTeamSpecificRuleOverridesThePlanDefault() {
        data.teamRule(season, teamA, 0, 2);
        UUID round = data.round(season, 1, 1, "CONFIRMED");
        data.scheduledGame(round, teamA, teamB, field, TUE_19);
        data.scheduledGame(round, teamA, teamC, field, WED_19);

        AuditReport report = service.audit(season, false);

        assertThat(rule(report, RestPeriodRule.CODE).violations()).noneMatch(v -> teamA.equals(v.teamId()));
    }

    @Test
    void twoGamesAtTheSameTimeOnOneFieldAreReported() {
        UUID round = data.round(season, 1, 1, "CONFIRMED");
        data.scheduledGame(round, teamA, teamB, field, TUE_19);
        data.scheduledGame(round, teamC, teamD, field, TUE_19);

        AuditReport report = service.audit(season, false);

        assertThat(status(report, FieldOverlapRule.CODE)).isEqualTo(RuleStatus.FAIL);
    }

    @Test
    void gamesOutsideTheTeamPreferencesAreReported() {
        UUID round = data.round(season, 1, 1, "CONFIRMED");
        data.scheduledGame(round, teamA, teamB, field, MON_19);

        AuditReport report = service.audit(season, false);

        RuleResult preferences = rule(report, PreferenceComplianceRule.CODE);
        assertThat(preferences.status()).isEqualTo(RuleStatus.FAIL);
        assertThat(preferences.violationCount()).isEqualTo(2);
    }

    @Test
    void draftRoundsAreOnlyIncludedOnRequestAndUnscheduledGamesAreCountedAsPending() {
        UUID confirmed = data.round(season, 1, 1, "CONFIRMED");
        data.scheduledGame(confirmed, teamA, teamB, field, TUE_19);
        UUID draft = data.round(season, 2, 2, "DRAFT");
        data.scheduledGame(draft, teamC, teamD, field, TUE_20_NEXT_WEEK);
        UUID unscheduled = data.round(season, 3, 3, "UNSCHEDULED");
        data.game(unscheduled, teamA, teamC);

        AuditReport confirmedOnly = service.audit(season, false);
        AuditReport withDraft = service.audit(season, true);

        assertThat(confirmedOnly.evaluatedGames()).isEqualTo(1);
        assertThat(confirmedOnly.includesDraft()).isFalse();
        assertThat(withDraft.evaluatedGames()).isEqualTo(2);
        assertThat(withDraft.includesDraft()).isTrue();
        assertThat(confirmedOnly.pendingGames()).isEqualTo(1);
    }

    @Test
    void theAuditRunsInAReadOnlyTransaction() {
        UUID round = data.round(season, 1, 1, "CONFIRMED");
        data.scheduledGame(round, teamA, teamB, field, TUE_19);

        TransactionTemplate readOnly = new TransactionTemplate(tx.getTransactionManager());
        readOnly.setReadOnly(true);

        AuditReport report = readOnly.execute(status -> service.audit(season, false));

        assertThat(report).isNotNull();
        assertThat(report.evaluatedGames()).isEqualTo(1);
    }

    @Test
    void aSeasonWithoutCalendarIsNotFound() {
        assertThatThrownBy(() -> service.audit(UUID.randomUUID(), false))
                .isInstanceOf(SeasonCalendarNotFoundException.class);
    }

    private static RuleResult rule(AuditReport report, String code) {
        return report.rules().stream().filter(r -> r.code().equals(code)).findFirst().orElseThrow();
    }

    private static RuleStatus status(AuditReport report, String code) {
        return rule(report, code).status();
    }
}
