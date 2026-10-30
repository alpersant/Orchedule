package com.orchedule.scheduling.domain;

import java.util.List;
import java.util.Objects;

public record RoundReadiness(
        boolean ready,
        boolean canAttemptGeneration,
        List<RoundReadinessIssue> issues
) {
    public RoundReadiness {
        issues = issues == null ? List.of() : List.copyOf(issues);
        boolean hasBlockers = issues.stream().anyMatch(RoundReadinessIssue::isBlocker);
        ready = !hasBlockers;
        canAttemptGeneration = ready;
    }

    public static RoundReadiness of(List<RoundReadinessIssue> issues) {
        return new RoundReadiness(false, false, Objects.requireNonNullElse(issues, List.of()));
    }
}
