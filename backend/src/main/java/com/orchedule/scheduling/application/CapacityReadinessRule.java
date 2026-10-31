package com.orchedule.scheduling.application;

import com.orchedule.scheduling.application.port.FieldScheduleSlot;
import com.orchedule.scheduling.domain.ReadinessCode;
import com.orchedule.scheduling.domain.ReadinessSeverity;
import com.orchedule.scheduling.domain.RoundReadinessIssue;
import com.orchedule.scheduling.domain.TeamScheduleProfile;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Validates whether the available scheduling capacity is sufficient for every
 * eligible team.
 *
 * <p>Capacity is measured using unique field slots. A slot is unique when its
 * field, day and hour combination differs from every other slot.</p>
 */
@Component
public class CapacityReadinessRule implements RoundReadinessPolicy {

    @Override
    public List<RoundReadinessIssue> validate(
            RoundReadinessContext context,
            int weekNumber
    ) {
        int eligibleTeams = countEligibleTeams(context.teamProfiles());
        int availableSlots = countUniqueSlots(context.fieldSlots());

        if (eligibleTeams > 0 && availableSlots < eligibleTeams) {
            return List.of(new RoundReadinessIssue(
                    ReadinessCode.INSUFFICIENT_CAPACITY,
                    ReadinessSeverity.BLOCKER,
                    "No hay suficientes franjas compatibles para asignar todos los equipos.",
                    "SEASON",
                    null,
                    "Habilita más campos o amplía los días y horas disponibles."
            ));
        }

        return List.of();
    }

    private int countEligibleTeams(List<TeamScheduleProfile> profiles) {
        return (int) profiles.stream()
                .filter(profile -> !profile.getDayOptions().isEmpty())
                .filter(profile -> !profile.getHourOptions().isEmpty())
                .count();
    }

    private int countUniqueSlots(List<FieldScheduleSlot> slots) {
        Set<String> uniqueSlots = new HashSet<>();

        for (FieldScheduleSlot slot : slots) {
            uniqueSlots.add(slotKey(slot));
        }

        return uniqueSlots.size();
    }

    private String slotKey(FieldScheduleSlot slot) {
        return slot.fieldId()
                + "|" + slot.day().name()
                + "|" + slot.hour().toString();
    }
}