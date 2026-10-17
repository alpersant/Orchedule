import '../entities/field_weekly_availability.dart';
import '../repositories/field_weekly_availability_repository.dart';

class GetFieldWeeklyAvailability {
  final FieldWeeklyAvailabilityRepository _repository;

  GetFieldWeeklyAvailability(this._repository);

  Future<List<FieldWeeklyAvailability>> call({
    required String seasonId,
    required int weekNumber,
  }) {
    return _repository.getForWeek(seasonId: seasonId, weekNumber: weekNumber);
  }
}

class SetFieldWeeklyAvailability {
  final FieldWeeklyAvailabilityRepository _repository;

  SetFieldWeeklyAvailability(this._repository);

  Future<List<FieldWeeklyAvailability>> call({
    required String seasonId,
    required int weekNumber,
    required List<String> enabledFieldIds,
  }) {
    return _repository.setForWeek(
      seasonId: seasonId,
      weekNumber: weekNumber,
      enabledFieldIds: enabledFieldIds,
    );
  }
}

class ApplyFieldWeeklyAvailability {
  final FieldWeeklyAvailabilityRepository _repository;

  ApplyFieldWeeklyAvailability(this._repository);

  Future<List<FieldWeeklyAvailability>> call({
    required String seasonId,
    required List<int> weekNumbers,
    required List<String> enabledFieldIds,
  }) {
    return _repository.applyToWeeks(
      seasonId: seasonId,
      weekNumbers: weekNumbers,
      enabledFieldIds: enabledFieldIds,
    );
  }
}

class ClearFieldWeeklyAvailability {
  final FieldWeeklyAvailabilityRepository _repository;

  ClearFieldWeeklyAvailability(this._repository);

  Future<List<FieldWeeklyAvailability>> call({
    required String seasonId,
    required int weekNumber,
  }) {
    return _repository.clearWeek(seasonId: seasonId, weekNumber: weekNumber);
  }
}
