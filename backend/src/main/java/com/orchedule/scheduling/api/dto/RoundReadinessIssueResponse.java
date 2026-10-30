package com.orchedule.scheduling.api.dto;

import com.orchedule.scheduling.domain.RoundReadinessIssue;

import java.util.UUID;

public record RoundReadinessIssueResponse(
        String code,
        String severity,
        String message,
        String entityType,
        UUID entityId,
        String suggestedAction
) {
    public static RoundReadinessIssueResponse from(RoundReadinessIssue issue) {
        return new RoundReadinessIssueResponse(
                issue.code().name(),
                issue.severity().name(),
                issue.message(),
                issue.entityType(),
                issue.entityId(),
                issue.suggestedAction()
        );
    }
}
