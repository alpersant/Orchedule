package com.orchedule.scheduling.application;

import com.orchedule.scheduling.application.port.RoundReadinessContextPort;
import com.orchedule.scheduling.application.exception.InvalidScheduleException;
import com.orchedule.scheduling.domain.ReadinessCode;
import com.orchedule.scheduling.domain.ReadinessSeverity;
import com.orchedule.scheduling.domain.RoundReadiness;
import com.orchedule.scheduling.domain.RoundReadinessIssue;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class ValidateRoundReadinessService {

    private static final int MIN_WEEK = 1;
    private static final int MAX_WEEK = 60;

    private final RoundReadinessContextPort contextPort;
    private final List<RoundReadinessPolicy> policies;

    public ValidateRoundReadinessService(
            RoundReadinessContextPort contextPort,
            List<RoundReadinessPolicy> policies
    ) {
        this.contextPort = contextPort;
        this.policies = List.copyOf(policies);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public RoundReadinessResult validate(UUID seasonId, int weekNumber) {
        if (seasonId == null) {
            throw new InvalidScheduleException("Season id is required");
        }
        if (weekNumber < MIN_WEEK || weekNumber > MAX_WEEK) {
            throw new InvalidScheduleException("Week number must be between " + MIN_WEEK + " and " + MAX_WEEK);
        }

        RoundReadinessContext context;
        try {
            context = contextPort.loadForWeek(seasonId, weekNumber);
        } catch (InvalidScheduleException ex) {
            return new RoundReadinessResult(
                    seasonId,
                    weekNumber,
                    RoundReadiness.of(List.of(
                            new RoundReadinessIssue(
                                    ReadinessCode.NO_AVAILABLE_FIELDS,
                                    ReadinessSeverity.BLOCKER,
                                    ex.getMessage(),
                                    "WEEK",
                                    seasonId,
                                    "Habilita al menos un campo para esta semana."
                            )
                    )),
                    new RoundReadinessSummary(0, 0, 0, 0, null)
            );
        }

        List<RoundReadinessIssue> issues = new ArrayList<>();
        for (RoundReadinessPolicy policy : policies) {
            issues.addAll(policy.validate(context, weekNumber));
        }
        issues.sort(Comparator.comparing(issue -> issue.severity().name()));

        RoundReadiness readiness = new RoundReadiness(false, false, issues);
        RoundReadinessSummary summary = new RoundReadinessSummary(
                context.teamProfiles().size(),
                context.fieldSlots().stream().map(s -> s.fieldId()).distinct().toList().size(),
                context.fieldSlots().size(),
                context.fieldSlots().size(),
                context.existingRoundStatus()
        );

        return new RoundReadinessResult(seasonId, weekNumber, readiness, summary);
    }
}
