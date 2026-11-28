package com.orchedule.scheduling.planning.audit;

import com.orchedule.scheduling.planning.audit.AuditModel.AuditInput;
import com.orchedule.scheduling.planning.audit.AuditModel.RuleResult;
import com.orchedule.scheduling.planning.audit.AuditModel.ScheduledGame;
import com.orchedule.scheduling.planning.audit.AuditModel.Violation;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Descanso mínimo entre el final de un partido y el inicio del siguiente del mismo equipo. */
@Component
@Order(10)
public class RestPeriodRule implements AuditRule {

    public static final String CODE = "REST_PERIOD";

    @Override
    public String code() {
        return CODE;
    }

    @Override
    public RuleResult evaluate(AuditInput input) {
        Duration match = Duration.ofMinutes(input.durationMinutes());
        List<Violation> violations = new ArrayList<>();
        int checked = 0;

        for (Map.Entry<UUID, List<ScheduledGame>> entry : input.timelinesByTeam().entrySet()) {
            UUID team = entry.getKey();
            int minRest = input.ruleFor(team).minRestHours();
            List<ScheduledGame> games = entry.getValue();

            for (int i = 1; i < games.size(); i++) {
                ScheduledGame previous = games.get(i - 1);
                ScheduledGame next = games.get(i);
                checked++;

                Duration rest = Duration.between(previous.at().plus(match), next.at());
                if (rest.compareTo(Duration.ofHours(minRest)) < 0) {
                    violations.add(new Violation(team, next.id(), next.roundNumber(), AuditFormat.text(
                            "%s: %d h de descanso antes de la jornada %d (mínimo %d h).",
                            input.nameOf(team), Math.max(0, rest.toHours()), next.roundNumber(), minRest)));
                }
            }
        }
        return RuleResult.of(CODE, checked, violations, List.of());
    }
}
