package com.orchedule.scheduling.application;

import com.orchedule.scheduling.application.port.FieldScheduleSlot;
import com.orchedule.scheduling.domain.ReadinessCode;
import com.orchedule.scheduling.domain.ReadinessSeverity;
import com.orchedule.scheduling.domain.RoundReadinessIssue;
import com.orchedule.scheduling.domain.TeamScheduleProfile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class CapacityReadinessRule implements RoundReadinessPolicy {

    @Override
    public List<RoundReadinessIssue> validate(RoundReadinessContext context, int weekNumber) {
        List<RoundReadinessIssue> issues = new ArrayList<>();
        int eligibleTeams = countEligibleTeams(context.teamProfiles());
        int compatibleSlots = countCompatibleSlots(context);

        if (eligibleTeams > 0 && compatibleSlots < eligibleTeams) {
            issues.add(new RoundReadinessIssue(
                    ReadinessCode.INSUFFICIENT_CAPACITY,
                    ReadinessSeverity.BLOCKER,
                    "No hay suficientes franjas compatibles para asignar todos los equipos.",
                    "SEASON",
                    null,
                    "Habilita más campos o amplía los días y horas disponibles."
            ));
        }
        return issues;
    }

    private int countEligibleTeams(List<TeamScheduleProfile> profiles) {
        return (int) profiles.stream()
                .filter(p -> !p.daysWithPriority(null).isEmpty() && !p.hoursWithPriority(null).isEmpty())
                .count();
    }

    private int countCompatibleSlots(RoundReadinessContext context) {
        Set<String> unique = new HashSet<>();
        for (FieldScheduleSlot slot : context.fieldSlots()) {
            unique.add(slot.day() + "|" + slot.hour() + "|" + slot.fieldId());
        }
        return unique.size();
    }
}
