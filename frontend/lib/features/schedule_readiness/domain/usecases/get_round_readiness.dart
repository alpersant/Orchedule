import '../entities/round_readiness.dart';
import '../repositories/round_readiness_repository.dart';

class GetRoundReadiness {
  final RoundReadinessRepository repository;

  const GetRoundReadiness(this.repository);

  Future<RoundReadiness> call({
    required String seasonId,
    required int weekNumber,
  }) {
    return repository.getForWeek(seasonId: seasonId, weekNumber: weekNumber);
  }
}
