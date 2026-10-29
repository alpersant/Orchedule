import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../app/providers.dart' show dioProvider;
import '../data/datasources/round_readiness_remote_data_source.dart';
import '../data/repositories/round_readiness_repository_impl.dart';
import '../domain/repositories/round_readiness_repository.dart';
import '../domain/usecases/get_round_readiness.dart';

final roundReadinessRemoteDataSourceProvider =
    Provider<RoundReadinessRemoteDataSource>((ref) {
  return RoundReadinessRemoteDataSourceImpl(ref.watch(dioProvider));
});

final roundReadinessRepositoryProvider =
    Provider<RoundReadinessRepository>((ref) {
  return RoundReadinessRepositoryImpl(
    ref.watch(roundReadinessRemoteDataSourceProvider),
  );
});

final getRoundReadinessProvider = Provider<GetRoundReadiness>((ref) {
  return GetRoundReadiness(ref.watch(roundReadinessRepositoryProvider));
});
