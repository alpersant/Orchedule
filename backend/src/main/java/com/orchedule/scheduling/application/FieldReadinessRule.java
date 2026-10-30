package com.orchedule.scheduling.application;

import com.orchedule.scheduling.application.port.FieldScheduleSlot;
import com.orchedule.scheduling.domain.ReadinessCode;
import com.orchedule.scheduling.domain.ReadinessSeverity;
import com.orchedule.scheduling.domain.RoundReadinessIssue;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class FieldReadinessRule implements RoundReadinessPolicy {

    @Override
    public List<RoundReadinessIssue> validate(RoundReadinessContext context, int weekNumber) {
        List<RoundReadinessIssue> issues = new ArrayList<>();
        if (context.fieldSlots().isEmpty()) {
            issues.add(new RoundReadinessIssue(
                    ReadinessCode.NO_AVAILABLE_FIELDS,
                    ReadinessSeverity.BLOCKER,
                    "No hay campos habilitados para la semana seleccionada.",
                    "WEEK",
                    null,
                    "Habilita al menos un campo en la disponibilidad semanal."
            ));
        }

        Set<String> seen = new HashSet<>();
        for (FieldScheduleSlot slot : context.fieldSlots()) {
            String key = slot.day() + "|" + slot.hour() + "|" + slot.fieldId();
            if (!seen.add(key)) {
                issues.add(new RoundReadinessIssue(
                        ReadinessCode.NO_AVAILABLE_FIELDS,
                        ReadinessSeverity.WARNING,
                        "Existen franjas duplicadas para el mismo campo, día y hora.",
                        "FIELD",
                        slot.fieldId(),
                        "Revisa la configuración de disponibilidad semanal."
                ));
            }
        }
        return issues;
    }
}
