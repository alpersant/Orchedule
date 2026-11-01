class ScheduleAssignmentDto {
  final String id;
  final String teamId;
  final String teamName;
  final String dayOfWeek;
  final String startTime;
  final String? fieldId;
  final String? fieldName;

  const ScheduleAssignmentDto({
    required this.id,
    required this.teamId,
    required this.teamName,
    required this.dayOfWeek,
    required this.startTime,
    this.fieldId,
    this.fieldName,
  });

  factory ScheduleAssignmentDto.fromJson(Map<String, dynamic> json) {
    return ScheduleAssignmentDto(
      id: json['id'].toString(),
      teamId: json['teamId'].toString(),
      teamName: json['teamName']?.toString() ?? 'Equipo',
      dayOfWeek: json['dayOfWeek']?.toString() ?? '',
      startTime: json['startTime']?.toString() ?? '',
      fieldId: json['fieldId']?.toString(),
      fieldName: json['fieldName']?.toString(),
    );
  }
}

class ScheduleRoundDto {
  final String id;
  final String seasonId;
  final int weekNumber;
  final String status;
  final List<ScheduleAssignmentDto> assignments;

  const ScheduleRoundDto({
    required this.id,
    required this.seasonId,
    required this.weekNumber,
    required this.status,
    required this.assignments,
  });

  factory ScheduleRoundDto.fromJson(Map<String, dynamic> json) {
    final assignments = (json['assignments'] as List<dynamic>? ?? [])
        .map(
          (item) => ScheduleAssignmentDto.fromJson(
            Map<String, dynamic>.from(item as Map),
          ),
        )
        .toList();

    return ScheduleRoundDto(
      id: json['id'].toString(),
      seasonId: json['seasonId'].toString(),
      weekNumber: json['weekNumber'] as int,
      status: json['status']?.toString() ?? 'DRAFT',
      assignments: assignments,
    );
  }
}
