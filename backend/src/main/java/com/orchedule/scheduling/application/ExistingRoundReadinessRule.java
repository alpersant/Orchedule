package com.orchedule.scheduling.application;

import com.orchedule.scheduling.domain.ReadinessCode;
import com.orchedule.scheduling.domain.ReadinessSeverity;
import com.orchedule.scheduling.domain.RoundReadinessIssue;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ExistingRoundReadinessRule implements RoundReadinessPolicy {

    @Override
    public List<RoundReadinessIssue> validate(RoundReadinessContext context, int weekNumber) {
        List<RoundReadinessIssue> issues = new ArrayList<>();
        String status = context.existingRoundStatus();
        if (status == null) {
            return issues;
        }
        if ("CANCELLED".equalsIgnoreCase(status)) {
            issues.add(new RoundReadinessIssue(
                    ReadinessCode.ROUND_CANCELLED,
                    ReadinessSeverity.WARNING,
                    "La jornada de esta semana fue cancelada.",
                    "SCHEDULE_ROUND",
                    null,
                    "Revisa el historial antes de volver a planificar."
            ));
        } else {
            issues.add(new RoundReadinessIssue(
                    ReadinessCode.ROUND_ALREADY_EXISTS,
                    ReadinessSeverity.BLOCKER,
                    "Ya existe una jornada para esta semana.",
                    "SCHEDULE_ROUND",
                    null,
                    "Consulta o gestiona la jornada existente antes de crear otra."
            ));
        }
        return issues;
    }
}
