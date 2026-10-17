import '../../domain/entities/field_weekly_availability.dart';
import '../dto/field_weekly_availability_dto.dart';

class FieldWeeklyAvailabilityMapper {
  const FieldWeeklyAvailabilityMapper._();

  static FieldWeeklyAvailability toDomain(FieldWeeklyAvailabilityDto dto) {
    return FieldWeeklyAvailability(
      id: dto.id,
      seasonId: dto.seasonId,
      weekNumber: dto.weekNumber,
      fieldId: dto.fieldId,
      enabled: dto.enabled,
    );
  }
}
