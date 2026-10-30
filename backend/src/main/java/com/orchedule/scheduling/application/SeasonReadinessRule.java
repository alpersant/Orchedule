package com.orchedule.scheduling.application;

import com.orchedule.scheduling.application.port.RoundReadinessContextPort;
import com.orchedule.scheduling.domain.ReadinessCode;
import com.orchedule.scheduling.domain.ReadinessSeverity;
import com.orchedule.scheduling.domain.RoundReadinessIssue;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Delegates season and competition state validation to the existing scheduling ACL.
 * The context port is expected to throw the domain InvalidScheduleException when the
 * season or its competition cannot be scheduled.
 */
@Component
public class SeasonReadinessRule implements RoundReadinessPolicy {

    private final RoundReadinessContextPort contextPort;

    public SeasonReadinessRule(RoundReadinessContextPort contextPort) {
        this.contextPort = contextPort;
    }

    @Override
    public List<RoundReadinessIssue> validate(RoundReadinessContext context, int weekNumber) {
        return List.of();
    }

    public void assertSeasonIsSchedulable(UUID seasonId) {
        contextPort.loadForWeek(seasonId, 1);
    }

    public static RoundReadinessIssue seasonNotActive(UUID seasonId) {
        return new RoundReadinessIssue(
                ReadinessCode.SEASON_NOT_ACTIVE,
                ReadinessSeverity.BLOCKER,
                "La temporada no está activa.",
                "SEASON",
                seasonId,
                "Activa la temporada antes de preparar la jornada."
        );
    }

    public static RoundReadinessIssue competitionNotActive(UUID competitionId) {
        return new RoundReadinessIssue(
                ReadinessCode.COMPETITION_NOT_ACTIVE,
                ReadinessSeverity.BLOCKER,
                "La competición asociada no está activa.",
                "COMPETITION",
                competitionId,
                "Activa la competición antes de preparar la jornada."
        );
    }
}
