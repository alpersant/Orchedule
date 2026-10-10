import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../core/network/auth_interceptor.dart';
import '../core/network/dio_client.dart';
import '../core/storage/secure_storage.dart';
import '../features/auth/data/auth_api.dart';
import '../features/auth/domain/auth_state.dart';
import '../features/auth/presentation/auth_controller.dart';
import '../features/venues/data/venues_api.dart';

final secureStorageProvider = Provider<SecureStorage>((ref) {
  return SecureStorage.instance;
});

final dioProvider = Provider<Dio>((ref) {
  final dio = createDio();
  final storage = ref.read(secureStorageProvider);

  dio.interceptors.add(
    AuthInterceptor(
      storage: storage,
      onSessionExpired: () async {
        await storage.clear();
      },
    ),
  );

  return dio;
});

final authApiProvider = Provider<AuthApi>((ref) {
  return AuthApi(
    ref.watch(dioProvider),
  );
});

final authProvider =
StateNotifierProvider<AuthController, AuthState>((ref) {
  return AuthController(
    authApi: ref.watch(authApiProvider),
    storage: ref.watch(secureStorageProvider),
  );
});

final venuesApiProvider = Provider<VenuesApi>((ref) {
  return VenuesApi(
    ref.watch(dioProvider),
  );
});