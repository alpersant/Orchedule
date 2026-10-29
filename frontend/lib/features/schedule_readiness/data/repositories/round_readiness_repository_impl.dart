import '../../domain/entities/round_readiness.dart';
import '../../domain/repositories/round_readiness_repository.dart';
import '../datasources/round_readiness_remote_data_source.dart';
import '../dto/round_readiness_response_dto.dart';

class RoundReadinessRepositoryImpl implements RoundReadinessRepository {
  final RoundReadinessRemoteDataSource remoteDataSource;

  const RoundReadinessRepositoryImpl(this.remoteDataSource);

  @override
  Future<RoundReadiness> getForWeek({
    required String seasonId,
    required int weekNumber,
  }) async {
    final dto = await remoteDataSource.getForWeek(
      seasonId: seasonId,
      weekNumber: weekNumber,
    );
    return dto.toDomain();
  }
}
