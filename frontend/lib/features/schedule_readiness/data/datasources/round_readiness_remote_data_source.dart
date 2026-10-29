import 'package:dio/dio.dart';
import '../dto/round_readiness_response_dto.dart';

abstract interface class RoundReadinessRemoteDataSource {
  Future<RoundReadinessResponseDto> getForWeek({
    required String seasonId,
    required int weekNumber,
  });
}

class RoundReadinessRemoteDataSourceImpl implements RoundReadinessRemoteDataSource {
  final Dio dio;

  const RoundReadinessRemoteDataSourceImpl(this.dio);

  @override
  Future<RoundReadinessResponseDto> getForWeek({
    required String seasonId,
    required int weekNumber,
  }) async {
    final response = await dio.get<Map<String, dynamic>>(
      '/v1/schedules/seasons/$seasonId/rounds/$weekNumber/readiness',
    );

    final data = response.data;

    if (data == null) {
      throw const FormatException('Expected a readiness object');
    }

    return RoundReadinessResponseDto.fromJson(data);
  }
}
