package com.orchedule.scheduling.application;

import com.orchedule.scheduling.domain.ReadinessCode;
import com.orchedule.scheduling.domain.ReadinessSeverity;
import com.orchedule.scheduling.domain.RoundReadinessIssue;
import com.orchedule.scheduling.domain.TeamScheduleProfile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TeamReadinessRule implements RoundReadinessPolicy {

    @Override
    public List<RoundReadinessIssue> validate(RoundReadinessContext context, int weekNumber) {
        List<RoundReadinessIssue> issues = new ArrayList<>();
        if (context.teamProfiles().isEmpty()) {
            issues.add(new RoundReadinessIssue(
                    ReadinessCode.NO_TEAMS,
                    ReadinessSeverity.BLOCKER,
                    "No hay equipos con preferencias válidas para planificar.",
                    "SEASON",
                    null,
                    "Registra equipos activos y define sus preferencias de día y hora."
            ));
            return issues;
        }

        for (TeamScheduleProfile profile : context.teamProfiles()) {
            if (profile.daysWithPriority(null).isEmpty() || profile.hoursWithPriority(null).isEmpty()) {
                issues.add(new RoundReadinessIssue(
                        ReadinessCode.INVALID_TEAM_PREFERENCES,
                        ReadinessSeverity.BLOCKER,
                        "El equipo no tiene preferencias completas de día u hora.",
                        "TEAM",
                        profile.getTeamId(),
                        "Revisa las preferencias de planificación del equipo."
                ));
            }
        }
        return issues;
    }
}
