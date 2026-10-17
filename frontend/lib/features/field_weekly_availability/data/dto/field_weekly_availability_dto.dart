class FieldWeeklyAvailabilityDto {
  final String id;
  final String seasonId;
  final int weekNumber;
  final String fieldId;
  final bool enabled;

  const FieldWeeklyAvailabilityDto({
    required this.id,
    required this.seasonId,
    required this.weekNumber,
    required this.fieldId,
    required this.enabled,
  });

  factory FieldWeeklyAvailabilityDto.fromJson(Map<String, dynamic> json) {
    return FieldWeeklyAvailabilityDto(
      id: json['id'] as String,
      seasonId: json['seasonId'] as String,
      weekNumber: json['weekNumber'] as int,
      fieldId: json['fieldId'] as String,
      enabled: json['enabled'] as bool,
    );
  }
}