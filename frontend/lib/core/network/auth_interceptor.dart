import 'package:dio/dio.dart';

import '../../features/auth/data/auth_api.dart';
import '../storage/secure_storage.dart';

typedef SessionExpiredCallback = Future<void> Function();

class AuthInterceptor extends QueuedInterceptor {
  AuthInterceptor({
    required Dio dio,
    required AuthApi authApi,
    required SecureStorage storage,
    required SessionExpiredCallback onSessionExpired,
  })  : _dio = dio,
        _authApi = authApi,
        _storage = storage,
        _onSessionExpired = onSessionExpired;

  final Dio _dio;
  final AuthApi _authApi;
  final SecureStorage _storage;
  final SessionExpiredCallback _onSessionExpired;

  @override
  Future<void> onRequest(
    RequestOptions options,
    RequestInterceptorHandler handler,
  ) async {
    if (options.extra['skipAuth'] == true) {
      handler.next(options);
      return;
    }

    final accessToken = await _storage.getAccessToken();
    if (accessToken != null && accessToken.isNotEmpty) {
      options.headers['Authorization'] = 'Bearer $accessToken';
    }

    handler.next(options);
  }

  @override
  Future<void> onError(
    DioException err,
    ErrorInterceptorHandler handler,
  ) async {
    final request = err.requestOptions;
    final isUnauthorized = err.response?.statusCode == 401;
    final cannotRefresh = request.extra['skipRefresh'] == true;
    final alreadyRetried = request.extra['retried'] == true;

    if (!isUnauthorized || cannotRefresh || alreadyRetried) {
      handler.next(err);
      return;
    }

    final refreshToken = await _storage.getRefreshToken();
    if (refreshToken == null || refreshToken.isEmpty) {
      await _onSessionExpired();
      handler.next(err);
      return;
    }

    try {
      final tokens = await _authApi.refresh(refreshToken);
      await _storage.saveTokens(
        accessToken: tokens.accessToken,
        refreshToken: tokens.refreshToken,
      );

      final retryOptions = request.copyWith(
        headers: {
          ...request.headers,
          'Authorization': 'Bearer ${tokens.accessToken}',
        },
        extra: {...request.extra, 'retried': true},
      );

      final response = await _dio.fetch<dynamic>(retryOptions);
      handler.resolve(response);
    } on DioException {
      await _onSessionExpired();
      handler.next(err);
    }
  }
}
