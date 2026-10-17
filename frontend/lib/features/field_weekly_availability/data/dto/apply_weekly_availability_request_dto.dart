class ApplyWeeklyAvailabilityRequestDto {
  final String seasonId;
  final List<int> weekNumbers;
  final List<String> enabledFieldIds;

  const ApplyWeeklyAvailabilityRequestDto({
    required this.seasonId,
    required this.weekNumbers,
    required this.enabledFieldIds,
  });

  Map<String, dynamic> toJson() {
    return {
      'seasonId': seasonId,
      'weekNumbers': weekNumbers,
      'enabledFieldIds': enabledFieldIds,
    };
  }
}