import 'package:dio/dio.dart';

import 'dto/round_readiness_dto.dart';
import 'dto/schedule_round_dto.dart';

class ScheduleRoundApi {
  final Dio dio;

  const ScheduleRoundApi(this.dio);

  Future<RoundReadinessDto> getReadiness({
    required String seasonId,
    required int weekNumber,
  }) async {
    final response = await dio.get<Map<String, dynamic>>(
      '/v1/schedules/seasons/$seasonId/rounds/$weekNumber/readiness',
    );

    return RoundReadinessDto.fromJson(response.data!);
  }

  Future<ScheduleRoundDto> generate({
    required String seasonId,
    required int weekNumber,
  }) async {
    final response = await dio.post<Map<String, dynamic>>(
      '/schedules/seasons/$seasonId/rounds/$weekNumber',
    );

    return ScheduleRoundDto.fromJson(response.data!);
  }

  Future<ScheduleRoundDto> confirm(String roundId) async {
    final response = await dio.post<Map<String, dynamic>>(
      '/schedules/rounds/$roundId/confirm',
    );

    return ScheduleRoundDto.fromJson(response.data!);
  }

  Future<ScheduleRoundDto> cancel(String roundId) async {
    final response = await dio.post<Map<String, dynamic>>(
      '/schedules/rounds/$roundId/cancel',
    );

    return ScheduleRoundDto.fromJson(response.data!);
  }

  Future<ScheduleRoundDto> regenerate(String roundId) async {
    final response = await dio.post<Map<String, dynamic>>(
      '/schedules/rounds/$roundId/regenerate',
    );

    return ScheduleRoundDto.fromJson(response.data!);
  }
}
