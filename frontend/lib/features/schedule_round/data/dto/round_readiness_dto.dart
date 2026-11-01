class RoundReadinessIssueDto {
  final String code;
  final String severity;
  final String message;
  final String? suggestedAction;

  const RoundReadinessIssueDto({
    required this.code,
    required this.severity,
    required this.message,
    this.suggestedAction,
  });

  factory RoundReadinessIssueDto.fromJson(Map<String, dynamic> json) {
    return RoundReadinessIssueDto(
      code: json['code'].toString(),
      severity: json['severity'].toString(),
      message: json['message'].toString(),
      suggestedAction: json['suggestedAction']?.toString(),
    );
  }
}

class RoundReadinessDto {
  final String seasonId;
  final int weekNumber;
  final bool ready;
  final bool canAttemptGeneration;
  final int eligibleTeams;
  final int availableFields;
  final int compatibleSlots;
  final int estimatedCapacity;
  final String? existingRoundStatus;
  final List<RoundReadinessIssueDto> issues;

  const RoundReadinessDto({
    required this.seasonId,
    required this.weekNumber,
    required this.ready,
    required this.canAttemptGeneration,
    required this.eligibleTeams,
    required this.availableFields,
    required this.compatibleSlots,
    required this.estimatedCapacity,
    required this.existingRoundStatus,
    required this.issues,
  });

  factory RoundReadinessDto.fromJson(Map<String, dynamic> json) {
    final summary = Map<String, dynamic>.from(json['summary'] as Map);
    final issues = (json['issues'] as List<dynamic>? ?? [])
        .map(
          (item) => RoundReadinessIssueDto.fromJson(
            Map<String, dynamic>.from(item as Map),
          ),
        )
        .toList();

    return RoundReadinessDto(
      seasonId: json['seasonId'].toString(),
      weekNumber: json['weekNumber'] as int,
      ready: json['ready'] as bool? ?? false,
      canAttemptGeneration: json['canAttemptGeneration'] as bool? ?? false,
      eligibleTeams: summary['eligibleTeams'] as int? ?? 0,
      availableFields: summary['availableFields'] as int? ?? 0,
      compatibleSlots: summary['compatibleSlots'] as int? ?? 0,
      estimatedCapacity: summary['estimatedCapacity'] as int? ?? 0,
      existingRoundStatus: summary['existingRoundStatus']?.toString(),
      issues: issues,
    );
  }
}
