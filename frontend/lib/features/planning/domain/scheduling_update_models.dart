class SeasonRules {
  final String seasonId;
  final int defaultMinRestHours;
  final int defaultMaxMatchesPerWeek;

  const SeasonRules({
    required this.seasonId,
    required this.defaultMinRestHours,
    required this.defaultMaxMatchesPerWeek,
  });

  factory SeasonRules.fromJson(Map<String, dynamic> json) => SeasonRules(
        seasonId: json['seasonId'].toString(),
        defaultMinRestHours: json['defaultMinRestHours'] as int,
        defaultMaxMatchesPerWeek: json['defaultMaxMatchesPerWeek'] as int,
      );

  Map<String, dynamic> toJson() => {
        'defaultMinRestHours': defaultMinRestHours,
        'defaultMaxMatchesPerWeek': defaultMaxMatchesPerWeek,
      };
}

class TeamRule {
  final String seasonId;
  final String teamId;
  final int minRestHours;
  final int maxMatchesPerWeek;

  const TeamRule({
    required this.seasonId,
    required this.teamId,
    required this.minRestHours,
    required this.maxMatchesPerWeek,
  });

  factory TeamRule.fromJson(Map<String, dynamic> json) => TeamRule(
        seasonId: json['seasonId'].toString(),
        teamId: json['teamId'].toString(),
        minRestHours: json['minRestHours'] as int,
        maxMatchesPerWeek: json['maxMatchesPerWeek'] as int,
      );
}

class GameAlternative {
  final String id;
  final String gameId;
  final int alternativeWeek;
  final String proposedDay;
  final String proposedTime;
  final String status;
  final String? reason;
  final String createdBy;
  final DateTime createdAt;
  final String? decidedBy;
  final DateTime? decidedAt;

  const GameAlternative({
    required this.id,
    required this.gameId,
    required this.alternativeWeek,
    required this.proposedDay,
    required this.proposedTime,
    required this.status,
    required this.reason,
    required this.createdBy,
    required this.createdAt,
    required this.decidedBy,
    required this.decidedAt,
  });

  factory GameAlternative.fromJson(Map<String, dynamic> json) => GameAlternative(
        id: json['id'].toString(),
        gameId: json['gameId'].toString(),
        alternativeWeek: json['alternativeWeek'] as int,
        proposedDay: json['proposedDay'].toString(),
        proposedTime: json['proposedTime'].toString(),
        status: json['status'].toString(),
        reason: json['reason'] as String?,
        createdBy: json['createdBy'].toString(),
        createdAt: DateTime.parse(json['createdAt'].toString()),
        decidedBy: json['decidedBy'] as String?,
        decidedAt: json['decidedAt'] == null
            ? null
            : DateTime.parse(json['decidedAt'].toString()),
      );

  bool get isProposed => status == 'PROPOSED';
}

class SchedulingHistoryEntry {
  final String id;
  final String? gameId;
  final String action;
  final String? operation;
  final String? result;
  final String actor;
  final DateTime createdAt;
  final String? detail;

  const SchedulingHistoryEntry({
    required this.id,
    required this.gameId,
    required this.action,
    required this.operation,
    required this.result,
    required this.actor,
    required this.createdAt,
    required this.detail,
  });

  factory SchedulingHistoryEntry.fromJson(Map<String, dynamic> json) =>
      SchedulingHistoryEntry(
        id: json['id'].toString(),
        gameId: json['gameId'] as String?,
        action: json['action'].toString(),
        operation: json['operation'] as String?,
        result: json['result'] as String?,
        actor: json['actor'].toString(),
        createdAt: DateTime.parse(json['createdAt'].toString()),
        detail: json['detail'] as String?,
      );
}
