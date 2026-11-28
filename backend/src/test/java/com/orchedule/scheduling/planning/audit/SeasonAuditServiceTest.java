package com.orchedule.scheduling.planning.audit;

import com.orchedule.scheduling.planning.audit.AuditModel.AuditInput;
import com.orchedule.scheduling.planning.audit.AuditModel.AuditReport;
import com.orchedule.scheduling.planning.audit.AuditModel.RuleStatus;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.orchedule.scheduling.planning.audit.AuditTestSupport.TEAM_A;
import static com.orchedule.scheduling.planning.audit.AuditTestSupport.TEAM_B;
import static com.orchedule.scheduling.planning.audit.AuditTestSupport.TUE_19;
import static com.orchedule.scheduling.planning.audit.AuditTestSupport.WED_19;
import static com.orchedule.scheduling.planning.audit.AuditTestSupport.game;
import static com.orchedule.scheduling.planning.audit.AuditTestSupport.input;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SeasonAuditServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2035-02-01T10:00:00Z"), ZoneOffset.UTC);
    private static final List<AuditRule> RULES = List.of(
            new RestPeriodRule(), new MaxMatchesPerWeekRule(), new RepetitionLimitRule(),
            new PreferenceComplianceRule(), new FieldOverlapRule());

    @Test
    void aCalendarWithoutFailingRulesIsCompliant() {
        AuditInput input = input(List.of(
                game(1, TEAM_A, TEAM_B, TUE_19),
                game(2, TEAM_A, TEAM_B, "2035-01-10T19:00:00Z")));

        AuditReport report = service(port -> input).audit(input.seasonId(), false);

        assertThat(report.compliant()).isTrue();
        assertThat(report.generatedAt()).isEqualTo(Instant.parse("2035-02-01T10:00:00Z"));
        assertThat(report.evaluatedGames()).isEqualTo(2);
        assertThat(report.rules()).extracting(r -> r.code()).containsExactly(
                RestPeriodRule.CODE, MaxMatchesPerWeekRule.CODE, RepetitionLimitRule.CODE,
                PreferenceComplianceRule.CODE, FieldOverlapRule.CODE);
        assertThat(report.teams()).hasSize(2);
    }

    @Test
    void aFailingRuleMakesTheReportNonCompliant() {
        AuditInput input = input(List.of(
                game(1, TEAM_A, TEAM_B, TUE_19),
                game(2, TEAM_A, TEAM_B, WED_19)));

        AuditReport report = service(port -> input).audit(input.seasonId(), false);

        assertThat(report.compliant()).isFalse();
        assertThat(report.rules()).anyMatch(r -> r.status() == RuleStatus.FAIL);
    }

    @Test
    void theDraftFlagIsForwardedToTheDataPort() {
        AtomicBoolean received = new AtomicBoolean(false);
        AuditInput input = input(List.of(game(1, TEAM_A, TEAM_B, TUE_19)));

        service(draft -> {
            received.set(draft);
            return input;
        }).audit(input.seasonId(), true);

        assertThat(received).isTrue();
    }

    @Test
    void aMissingCalendarIsPropagated() {
        UUID season = UUID.randomUUID();
        SeasonAuditService service = new SeasonAuditService(
                (id, draft) -> {
                    throw new SeasonCalendarNotFoundException(id);
                }, RULES, new FairnessCalculator(), CLOCK);

        assertThatThrownBy(() -> service.audit(season, false))
                .isInstanceOf(SeasonCalendarNotFoundException.class);
    }

    @Test
    void aNullSeasonIsRejected() {
        SeasonAuditService service = service(draft -> input(List.of()));

        assertThatThrownBy(() -> service.audit(null, false)).isInstanceOf(NullPointerException.class);
    }

    private SeasonAuditService service(java.util.function.Function<Boolean, AuditInput> loader) {
        return new SeasonAuditService((id, draft) -> loader.apply(draft), RULES, new FairnessCalculator(), CLOCK);
    }
}
