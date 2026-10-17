import '../../data/dto/apply_weekly_availability_request_dto.dart';
import '../../data/dto/set_weekly_availability_request_dto.dart';
import '../../domain/entities/field_weekly_availability.dart';
import '../../domain/repositories/field_weekly_availability_repository.dart';
import '../datasources/field_weekly_availability_remote_data_source.dart';
import '../mappers/field_weekly_availability_mapper.dart';

class FieldWeeklyAvailabilityRepositoryImpl
    implements FieldWeeklyAvailabilityRepository {
  final FieldWeeklyAvailabilityRemoteDataSource _remoteDataSource;

  FieldWeeklyAvailabilityRepositoryImpl(this._remoteDataSource);

  @override
  Future<List<FieldWeeklyAvailability>> getForWeek({
    required String seasonId,
    required int weekNumber,
  }) async {
    final dtos = await _remoteDataSource.getForWeek(
      seasonId: seasonId,
      weekNumber: weekNumber,
    );
    return dtos.map(FieldWeeklyAvailabilityMapper.toDomain).toList();
  }

  @override
  Future<List<FieldWeeklyAvailability>> setForWeek({
    required String seasonId,
    required int weekNumber,
    required List<String> enabledFieldIds,
  }) async {
    final dtos = await _remoteDataSource.setForWeek(
      SetWeeklyAvailabilityRequestDto(
        seasonId: seasonId,
        weekNumber: weekNumber,
        enabledFieldIds: enabledFieldIds,
      ),
    );
    return dtos.map(FieldWeeklyAvailabilityMapper.toDomain).toList();
  }

  @override
  Future<List<FieldWeeklyAvailability>> applyToWeeks({
    required String seasonId,
    required List<int> weekNumbers,
    required List<String> enabledFieldIds,
  }) async {
    final dtos = await _remoteDataSource.applyToWeeks(
      ApplyWeeklyAvailabilityRequestDto(
        seasonId: seasonId,
        weekNumbers: weekNumbers,
        enabledFieldIds: enabledFieldIds,
      ),
    );
    return dtos.map(FieldWeeklyAvailabilityMapper.toDomain).toList();
  }

  @override
  Future<List<FieldWeeklyAvailability>> clearWeek({
    required String seasonId,
    required int weekNumber,
  }) async {
    final dtos = await _remoteDataSource.clearWeek(
      seasonId: seasonId,
      weekNumber: weekNumber,
    );
    return dtos.map(FieldWeeklyAvailabilityMapper.toDomain).toList();
  }
}
