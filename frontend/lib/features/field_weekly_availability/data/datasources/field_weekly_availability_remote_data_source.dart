import 'package:dio/dio.dart';
import '../dto/apply_weekly_availability_request_dto.dart';
import '../dto/field_weekly_availability_dto.dart';
import '../dto/set_weekly_availability_request_dto.dart';

abstract class FieldWeeklyAvailabilityRemoteDataSource {
  Future<List<FieldWeeklyAvailabilityDto>> getForWeek({
    required String seasonId,
    required int weekNumber,
  });

  Future<List<FieldWeeklyAvailabilityDto>> setForWeek(
    SetWeeklyAvailabilityRequestDto request,
  );

  Future<List<FieldWeeklyAvailabilityDto>> applyToWeeks(
    ApplyWeeklyAvailabilityRequestDto request,
  );

  Future<List<FieldWeeklyAvailabilityDto>> clearWeek({
    required String seasonId,
    required int weekNumber,
  });
}

class FieldWeeklyAvailabilityRemoteDataSourceImpl
    implements FieldWeeklyAvailabilityRemoteDataSource {
  final Dio _dio;

  FieldWeeklyAvailabilityRemoteDataSourceImpl(this._dio);

  @override
  Future<List<FieldWeeklyAvailabilityDto>> getForWeek({
    required String seasonId,
    required int weekNumber,
  }) async {
    final response = await _dio.get<List<dynamic>>(
      '/v1/fields/availability',
      queryParameters: {'seasonId': seasonId, 'weekNumber': weekNumber},
    );
    return _map(response.data);
  }

  @override
  Future<List<FieldWeeklyAvailabilityDto>> setForWeek(
    SetWeeklyAvailabilityRequestDto request,
  ) async {
    final response = await _dio.put<List<dynamic>>(
      '/v1/fields/availability',
      data: request.toJson(),
    );
    return _map(response.data);
  }

  @override
  Future<List<FieldWeeklyAvailabilityDto>> applyToWeeks(
    ApplyWeeklyAvailabilityRequestDto request,
  ) async {
    final response = await _dio.post<List<dynamic>>(
      '/v1/fields/availability/apply',
      data: request.toJson(),
    );
    return _map(response.data);
  }

  @override
  Future<List<FieldWeeklyAvailabilityDto>> clearWeek({
    required String seasonId,
    required int weekNumber,
  }) async {
    final response = await _dio.delete<List<dynamic>>(
      '/v1/fields/availability',
      queryParameters: {'seasonId': seasonId, 'weekNumber': weekNumber},
    );
    return _map(response.data);
  }

  List<FieldWeeklyAvailabilityDto> _map(List<dynamic>? data) {
    if (data == null) {
      throw const FormatException('Expected an availability list');
    }
    return List.unmodifiable(data.map((item) {
      if (item is! Map) {
        throw const FormatException('Expected an availability object');
      }
      return FieldWeeklyAvailabilityDto.fromJson(
        Map<String, dynamic>.from(item),
      );
    }));
  }
}
