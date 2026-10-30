import '../../domain/entities/round_readiness.dart';
import '../../domain/entities/round_readiness_issue.dart';
import 'round_readiness_issue_dto.dart';

class RoundReadinessResponseDto {
  final String seasonId;
  final int weekNumber;
  final bool ready;
  final bool canAttemptGeneration;
  final RoundReadinessSummary summary;
  final List<RoundReadinessIssue> issues;

  const RoundReadinessResponseDto({
    required this.seasonId,
    required this.weekNumber,
    required this.ready,
    required this.canAttemptGeneration,
    required this.summary,
    required this.issues,
  });

  factory RoundReadinessResponseDto.fromJson(Map<String, dynamic> json) {
    final summaryJson = json['summary'] as Map<String, dynamic>;
    final issueJson = json['issues'] as List<dynamic>;

    return RoundReadinessResponseDto(
      seasonId: json['seasonId'] as String,
      weekNumber: json['weekNumber'] as int,
      ready: json['ready'] as bool,
      canAttemptGeneration: json['canAttemptGeneration'] as bool,
      summary: RoundReadinessSummary(
        eligibleTeams: summaryJson['eligibleTeams'] as int? ?? 0,
        availableFields: summaryJson['availableFields'] as int? ?? 0,
        compatibleSlots: summaryJson['compatibleSlots'] as int? ?? 0,
        estimatedCapacity: summaryJson['estimatedCapacity'] as int? ?? 0,
        existingRoundStatus: summaryJson['existingRoundStatus'] as String?,
      ),
      issues: issueJson
          .map((item) => RoundReadinessIssueDto.fromJson(
                Map<String, dynamic>.from(item as Map),
              ).toDomain())
          .toList(),
    );
  }

  RoundReadiness toDomain() => RoundReadiness(
        seasonId: seasonId,
        weekNumber: weekNumber,
        ready: ready,
        canAttemptGeneration: canAttemptGeneration,
        summary: summary,
        issues: List.unmodifiable(issues),
      );
}
