class SetWeeklyAvailabilityRequestDto {
  final String seasonId;
  final int weekNumber;
  final List<String> enabledFieldIds;

  const SetWeeklyAvailabilityRequestDto({
    required this.seasonId,
    required this.weekNumber,
    required this.enabledFieldIds,
  });

  Map<String, dynamic> toJson() {
    return {
      'seasonId': seasonId,
      'weekNumber': weekNumber,
      'enabledFieldIds': enabledFieldIds,
    };
  }
}