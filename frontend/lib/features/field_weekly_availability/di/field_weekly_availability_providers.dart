import 'package:dio/dio.dart';
import '../../../app/providers.dart' show dioProvider;
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../data/datasources/field_weekly_availability_remote_data_source.dart';
import '../data/repositories/field_weekly_availability_repository_impl.dart';
import '../domain/repositories/field_weekly_availability_repository.dart';
import '../domain/usecases/field_weekly_availability_usecases.dart';

/// Reuses the authenticated client and its existing API base URL.
final availabilityDioProvider = Provider<Dio>((ref) {
  return ref.watch(dioProvider);
});

final fieldWeeklyAvailabilityRemoteDataSourceProvider =
    Provider<FieldWeeklyAvailabilityRemoteDataSource>((ref) {
  return FieldWeeklyAvailabilityRemoteDataSourceImpl(
    ref.watch(availabilityDioProvider),
  );
});

final fieldWeeklyAvailabilityRepositoryProvider =
    Provider<FieldWeeklyAvailabilityRepository>((ref) {
  return FieldWeeklyAvailabilityRepositoryImpl(
    ref.watch(fieldWeeklyAvailabilityRemoteDataSourceProvider),
  );
});

final getFieldWeeklyAvailabilityProvider = Provider<GetFieldWeeklyAvailability>(
  (ref) => GetFieldWeeklyAvailability(
    ref.watch(fieldWeeklyAvailabilityRepositoryProvider),
  ),
);

final setFieldWeeklyAvailabilityProvider =
    Provider<SetFieldWeeklyAvailability>((ref) {
  return SetFieldWeeklyAvailability(
    ref.watch(fieldWeeklyAvailabilityRepositoryProvider),
  );
});

final applyFieldWeeklyAvailabilityProvider =
    Provider<ApplyFieldWeeklyAvailability>((ref) {
  return ApplyFieldWeeklyAvailability(
    ref.watch(fieldWeeklyAvailabilityRepositoryProvider),
  );
});

final clearFieldWeeklyAvailabilityProvider =
    Provider<ClearFieldWeeklyAvailability>((ref) {
  return ClearFieldWeeklyAvailability(
    ref.watch(fieldWeeklyAvailabilityRepositoryProvider),
  );
});
