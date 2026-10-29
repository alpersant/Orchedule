import '../entities/round_readiness.dart';

abstract interface class RoundReadinessRepository {
  Future<RoundReadiness> getForWeek({
    required String seasonId,
    required int weekNumber,
  });
}
