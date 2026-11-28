package com.orchedule.scheduling.planning.audit;

import com.orchedule.scheduling.planning.audit.AuditModel.AuditInput;
import com.orchedule.scheduling.planning.audit.AuditModel.AuditReport;
import com.orchedule.scheduling.planning.audit.AuditModel.RuleResult;
import com.orchedule.scheduling.planning.audit.AuditModel.RuleStatus;
import com.orchedule.scheduling.planning.audit.AuditModel.TeamFairness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Fachada de la auditoría: lee la temporada una sola vez, aplica todas las reglas registradas
 * y calcula la equidad. Es de solo lectura y no modifica ningún dato.
 */
@Service
public class SeasonAuditService {

    private final AuditDataPort data;
    private final List<AuditRule> rules;
    private final FairnessCalculator fairness;
    private final Clock clock;

    @Autowired
    public SeasonAuditService(AuditDataPort data, List<AuditRule> rules, FairnessCalculator fairness) {
        this(data, rules, fairness, Clock.systemUTC());
    }

    SeasonAuditService(AuditDataPort data, List<AuditRule> rules, FairnessCalculator fairness, Clock clock) {
        this.data = Objects.requireNonNull(data, "data");
        this.rules = List.copyOf(rules);
        this.fairness = Objects.requireNonNull(fairness, "fairness");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    @Transactional(readOnly = true)
    public AuditReport audit(UUID seasonId, boolean includeDraft) {
        Objects.requireNonNull(seasonId, "seasonId");

        AuditInput input = data.load(seasonId, includeDraft);
        List<RuleResult> results = rules.stream().map(rule -> rule.evaluate(input)).toList();
        List<TeamFairness> teams = fairness.teams(input);
        boolean compliant = results.stream().noneMatch(result -> result.status() == RuleStatus.FAIL);

        return new AuditReport(seasonId, clock.instant(), input.zone().getId(), input.includesDraft(),
                input.games().size(), input.pendingGames(), compliant, results,
                fairness.league(teams), teams);
    }
}
