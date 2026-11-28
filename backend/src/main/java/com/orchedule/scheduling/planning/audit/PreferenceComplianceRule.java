package com.orchedule.scheduling.planning.audit;

import com.orchedule.scheduling.planning.audit.AuditModel.AuditInput;
import com.orchedule.scheduling.planning.audit.AuditModel.RuleResult;
import com.orchedule.scheduling.planning.audit.AuditModel.ScheduledGame;
import com.orchedule.scheduling.planning.audit.AuditModel.Violation;
import com.orchedule.scheduling.planning.partial.PlanningData.Preferences;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Cada partido cae en un día y una hora permitidos por las preferencias de ambos equipos. */
@Component
@Order(40)
public class PreferenceComplianceRule implements AuditRule {

    public static final String CODE = "PREFERENCE_COMPLIANCE";

    @Override
    public String code() {
        return CODE;
    }

    @Override
    public RuleResult evaluate(AuditInput input) {
        List<Violation> violations = new ArrayList<>();
        Set<UUID> notEvaluated = new LinkedHashSet<>();
        int checked = 0;

        for (ScheduledGame game : input.games()) {
            ZonedDateTime local = game.at().atZone(input.zone());

            for (UUID team : List.of(game.home(), game.away())) {
                Preferences prefs = input.preferences().get(team);
                if (prefs == null || !prefs.valid()) {
                    notEvaluated.add(team);
                    continue;
                }

                checked++;
                if (!prefs.days().contains(local.getDayOfWeek()) || !prefs.hours().contains(local.toLocalTime())) {
                    violations.add(new Violation(team, game.id(), game.roundNumber(), AuditFormat.text(
                            "%s juega %s a las %s (jornada %d), fuera de sus opciones permitidas.",
                            input.nameOf(team), AuditFormat.day(local.getDayOfWeek()),
                            local.toLocalTime(), game.roundNumber())));
                }
            }
        }

        List<String> notes = notEvaluated.isEmpty()
                ? List.of()
                : List.of(AuditFormat.text("%d equipo(s) sin preferencias válidas: no se han podido evaluar.",
                notEvaluated.size()));
        return RuleResult.of(CODE, checked, violations, notes);
    }
}
