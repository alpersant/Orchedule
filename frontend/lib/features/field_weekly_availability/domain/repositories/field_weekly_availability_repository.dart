import '../entities/field_weekly_availability.dart';

abstract class FieldWeeklyAvailabilityRepository {
  Future<List<FieldWeeklyAvailability>> getForWeek({
    required String seasonId,
    required int weekNumber,
  });

  Future<List<FieldWeeklyAvailability>> setForWeek({
    required String seasonId,
    required int weekNumber,
    required List<String> enabledFieldIds,
  });

  Future<List<FieldWeeklyAvailability>> applyToWeeks({
    required String seasonId,
    required List<int> weekNumbers,
    required List<String> enabledFieldIds,
  });

  Future<List<FieldWeeklyAvailability>> clearWeek({
    required String seasonId,
    required int weekNumber,
  });
}
