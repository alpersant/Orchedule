package com.orchedule.scheduling.planning.audit;

import com.orchedule.scheduling.planning.audit.AuditModel.AuditInput;
import com.orchedule.scheduling.planning.audit.AuditModel.RuleResult;
import com.orchedule.scheduling.planning.audit.AuditModel.ScheduledGame;
import com.orchedule.scheduling.planning.audit.AuditModel.Violation;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Un equipo no juega tres jornadas consecutivas en el mismo día de la semana ni a la misma hora. */
@Component
@Order(30)
public class RepetitionLimitRule implements AuditRule {

    public static final String CODE = "REPETITION_LIMIT";

    @Override
    public String code() {
        return CODE;
    }

    @Override
    public RuleResult evaluate(AuditInput input) {
        List<Violation> violations = new ArrayList<>();
        int checked = 0;

        for (Map.Entry<UUID, List<ScheduledGame>> entry : input.timelinesByTeam().entrySet()) {
            UUID team = entry.getKey();
            List<ScheduledGame> byRound = new ArrayList<>(entry.getValue());
            byRound.sort(Comparator.comparingInt(ScheduledGame::roundNumber).thenComparing(ScheduledGame::at));

            for (int i = 0; i + 2 < byRound.size(); i++) {
                checked++;
                ZonedDateTime a = byRound.get(i).at().atZone(input.zone());
                ZonedDateTime b = byRound.get(i + 1).at().atZone(input.zone());
                ZonedDateTime c = byRound.get(i + 2).at().atZone(input.zone());
                ScheduledGame last = byRound.get(i + 2);

                if (a.getDayOfWeek() == b.getDayOfWeek() && b.getDayOfWeek() == c.getDayOfWeek()) {
                    violations.add(new Violation(team, last.id(), last.roundNumber(), AuditFormat.text(
                            "%s: tres jornadas seguidas (hasta la %d) en %s.",
                            input.nameOf(team), last.roundNumber(), AuditFormat.day(a.getDayOfWeek()))));
                }
                if (a.toLocalTime().equals(b.toLocalTime()) && b.toLocalTime().equals(c.toLocalTime())) {
                    violations.add(new Violation(team, last.id(), last.roundNumber(), AuditFormat.text(
                            "%s: tres jornadas seguidas (hasta la %d) a las %s.",
                            input.nameOf(team), last.roundNumber(), a.toLocalTime())));
                }
            }
        }
        return RuleResult.of(CODE, checked, violations, List.of());
    }
}
