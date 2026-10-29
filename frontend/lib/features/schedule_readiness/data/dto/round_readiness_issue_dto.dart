import '../../domain/entities/round_readiness_issue.dart';

class RoundReadinessIssueDto {
  final String code;
  final String severity;
  final String message;
  final String? entityType;
  final String? entityId;
  final String? suggestedAction;

  const RoundReadinessIssueDto({
    required this.code,
    required this.severity,
    required this.message,
    this.entityType,
    this.entityId,
    this.suggestedAction,
  });

  factory RoundReadinessIssueDto.fromJson(Map<String, dynamic> json) {
    return RoundReadinessIssueDto(
      code: json['code'] as String,
      severity: json['severity'] as String,
      message: json['message'] as String,
      entityType: json['entityType'] as String?,
      entityId: json['entityId'] as String?,
      suggestedAction: json['suggestedAction'] as String?,
    );
  }

  RoundReadinessIssue toDomain() => RoundReadinessIssue(
        code: code,
        severity: ReadinessSeverity.values.firstWhere(
          (value) => value.name.toUpperCase() == severity.toUpperCase(),
          orElse: () => ReadinessSeverity.info,
        ),
        message: message,
        entityType: entityType,
        entityId: entityId,
        suggestedAction: suggestedAction,
      );
}
