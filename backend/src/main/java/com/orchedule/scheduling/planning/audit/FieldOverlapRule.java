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
import java.util.TreeMap;
import java.util.UUID;

/** Un campo no alberga dos partidos a la vez. */
@Component
@Order(50)
public class FieldOverlapRule implements AuditRule {

    public static final String CODE = "FIELD_OVERLAP";

    @Override
    public String code() {
        return CODE;
    }

    @Override
    public RuleResult evaluate(AuditInput input) {
        Duration match = Duration.ofMinutes(input.durationMinutes());
        Map<UUID, List<ScheduledGame>> byField = new TreeMap<>();
        for (ScheduledGame game : input.games()) {
            if (game.field() != null) {
                byField.computeIfAbsent(game.field(), k -> new ArrayList<>()).add(game);
            }
        }

        List<Violation> violations = new ArrayList<>();
        int checked = 0;

        for (List<ScheduledGame> games : byField.values()) {
            games.sort(AuditModel.CHRONOLOGICAL);
            for (int i = 1; i < games.size(); i++) {
                ScheduledGame previous = games.get(i - 1);
                ScheduledGame next = games.get(i);
                checked++;

                if (previous.at().plus(match).isAfter(next.at())) {
                    violations.add(new Violation(null, next.id(), next.roundNumber(), AuditFormat.text(
                            "Dos partidos se solapan en el mismo campo (jornadas %d y %d).",
                            previous.roundNumber(), next.roundNumber())));
                }
            }
        }
        return RuleResult.of(CODE, checked, violations, List.of());
    }
}
