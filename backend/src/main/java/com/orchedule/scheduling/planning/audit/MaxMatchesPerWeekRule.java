package com.orchedule.scheduling.planning.audit;

import com.orchedule.scheduling.planning.audit.AuditModel.AuditInput;
import com.orchedule.scheduling.planning.audit.AuditModel.RuleResult;
import com.orchedule.scheduling.planning.audit.AuditModel.ScheduledGame;
import com.orchedule.scheduling.planning.audit.AuditModel.Violation;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

/** Máximo de partidos por equipo en una semana natural (lunes a domingo, en la zona de la temporada). */
@Component
@Order(20)
public class MaxMatchesPerWeekRule implements AuditRule {

    public static final String CODE = "MAX_MATCHES_PER_WEEK";

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
            int max = input.ruleFor(team).maxMatchesPerWeek();

            Map<LocalDate, Integer> perWeek = new TreeMap<>();
            for (ScheduledGame game : entry.getValue()) {
                LocalDate monday = game.at().atZone(input.zone()).toLocalDate()
                        .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                perWeek.merge(monday, 1, Integer::sum);
            }

            for (Map.Entry<LocalDate, Integer> week : perWeek.entrySet()) {
                checked++;
                if (week.getValue() > max) {
                    violations.add(new Violation(team, null, null, AuditFormat.text(
                            "%s: %d partidos en la semana del %s (máximo %d).",
                            input.nameOf(team), week.getValue(), week.getKey(), max)));
                }
            }
        }
        return RuleResult.of(CODE, checked, violations, List.of());
    }
}
