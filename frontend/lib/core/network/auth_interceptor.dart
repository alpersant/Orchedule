import 'package:dio/dio.dart';
import 'package:flutter/foundation.dart';

import '../storage/secure_storage.dart';

typedef SessionExpiredCallback = Future<void> Function();

class AuthInterceptor extends Interceptor {
  AuthInterceptor({
    required SecureStorage storage,
    required SessionExpiredCallback onSessionExpired,
  })  : _storage = storage,
        _onSessionExpired = onSessionExpired;

  final SecureStorage _storage;
  final SessionExpiredCallback _onSessionExpired;

  @override
  Future<void> onRequest(
    RequestOptions options,
    RequestInterceptorHandler handler,
  ) async {
    final skipAuth = options.extra['skipAuth'] == true;

    if (!skipAuth) {
      final token = await _storage.getAccessToken();
      if (token != null && token.isNotEmpty) {
        options.headers['Authorization'] = 'Bearer $token';
      }
    }

    debugPrint(
      '[HTTP] ${options.method} ${options.uri} | '
      'Bearer=${options.headers['Authorization'] != null} | '
      'skipAuth=$skipAuth',
    );

    handler.next(options);
  }

  @override
  Future<void> onError(
    DioException err,
    ErrorInterceptorHandler handler,
  ) async {
    debugPrint('[HTTP ERROR] type=${err.type} error=${err.error}');
    debugPrint('[HTTP ERROR] uri=${err.requestOptions.uri}');
    debugPrint('[HTTP ERROR] status=${err.response?.statusCode}');

    if (err.response?.statusCode == 401 &&
        err.requestOptions.extra['skipAuth'] != true) {
      await _onSessionExpired();
    }

    handler.next(err);
  }
}
