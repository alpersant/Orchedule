package com.orchedule.scheduling.domain;

import java.util.Optional;
import java.util.UUID;

public record RoundReadinessIssue(
        ReadinessCode code,
        ReadinessSeverity severity,
        String message,
        String entityType,
        UUID entityId,
        String suggestedAction
) {
    public RoundReadinessIssue {
        if (code == null) throw new IllegalArgumentException("Readiness code is required");
        if (severity == null) throw new IllegalArgumentException("Readiness severity is required");
        if (message == null || message.isBlank()) throw new IllegalArgumentException("Readiness message is required");
        message = message.trim();
        entityType = entityType == null || entityType.isBlank() ? null : entityType.trim();
        suggestedAction = suggestedAction == null || suggestedAction.isBlank() ? null : suggestedAction.trim();
    }

    public boolean isBlocker() {
        return severity == ReadinessSeverity.BLOCKER;
    }

    public Optional<UUID> entity() {
        return Optional.ofNullable(entityId);
    }
}
