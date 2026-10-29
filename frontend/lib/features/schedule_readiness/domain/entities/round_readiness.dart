import 'round_readiness_issue.dart';

class RoundReadinessSummary {
  final int eligibleTeams;
  final int availableFields;
  final int compatibleSlots;
  final int estimatedCapacity;
  final String? existingRoundStatus;

  const RoundReadinessSummary({
    required this.eligibleTeams,
    required this.availableFields,
    required this.compatibleSlots,
    required this.estimatedCapacity,
    this.existingRoundStatus,
  });
}

class RoundReadiness {
  final String seasonId;
  final int weekNumber;
  final bool ready;
  final bool canAttemptGeneration;
  final RoundReadinessSummary summary;
  final List<RoundReadinessIssue> issues;

  const RoundReadiness({
    required this.seasonId,
    required this.weekNumber,
    required this.ready,
    required this.canAttemptGeneration,
    required this.summary,
    required this.issues,
  });

  bool get hasBlockers => issues.any((issue) => issue.isBlocker);
}
