class FieldWeeklyAvailability {
  final String id;
  final String seasonId;
  final int weekNumber;
  final String fieldId;
  final bool enabled;

  const FieldWeeklyAvailability({
    required this.id,
    required this.seasonId,
    required this.weekNumber,
    required this.fieldId,
    required this.enabled,
  });
}