enum ReadinessSeverity { blocker, warning, info }

class RoundReadinessIssue {
  final String code;
  final ReadinessSeverity severity;
  final String message;
  final String? entityType;
  final String? entityId;
  final String? suggestedAction;

  const RoundReadinessIssue({
    required this.code,
    required this.severity,
    required this.message,
    this.entityType,
    this.entityId,
    this.suggestedAction,
  });

  bool get isBlocker => severity == ReadinessSeverity.blocker;
}
