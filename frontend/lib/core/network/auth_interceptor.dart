import 'dart:async';

import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../features/auth/data/auth_api.dart';
import '../../features/auth/domain/auth_state.dart';
import '../storage/secure_storage.dart';

class AuthInterceptor extends Interceptor {
  final Dio _rawDio;
  final Ref _ref;

  bool _isRefreshing = false;
  final List<Completer<Response>> _pendingCompleters = [];
  final List<RequestOptions> _pendingRequests = [];

  AuthInterceptor(this._rawDio, this._ref);

  @override
  Future<void> onRequest(
    RequestOptions options,
    RequestInterceptorHandler handler,
  ) async {
    final token = await SecureStorage.instance.getAccessToken();
    if (token != null && token.isNotEmpty) {
      options.headers['Authorization'] = 'Bearer $token';
    }
    handler.next(options);
  }

  @override
  Future<void> onError(
    DioException err,
    ErrorInterceptorHandler handler,
  ) async {
    final isUnauthorized = err.response?.statusCode == 401;
    final isAuthEndpoint = err.requestOptions.path.contains('/api/auth/');

    if (!isUnauthorized || isAuthEndpoint) {
      handler.next(err);
      return;
    }

    final completer = Completer<Response>();
    _pendingCompleters.add(completer);
    _pendingRequests.add(err.requestOptions);

    if (!_isRefreshing) {
      _isRefreshing = true;
      unawaited(_refreshAndFlushQueue());
    }

    try {
      final response = await completer.future;
      handler.resolve(response);
    } catch (e) {
      handler.next(err);
    }
  }

  Future<void> _refreshAndFlushQueue() async {
    final completers = List<Completer<Response>>.from(_pendingCompleters);
    final requests = List<RequestOptions>.from(_pendingRequests);
    _pendingCompleters.clear();
    _pendingRequests.clear();

    try {
      final refreshToken = await SecureStorage.instance.getRefreshToken();
      if (refreshToken == null) {
        throw DioException(
          requestOptions: RequestOptions(path: '/api/auth/refresh'),
          error: 'No refresh token available',
        );
      }

      final authApi = AuthApi(_rawDio);
      final response = await authApi.refresh(refreshToken);

      await SecureStorage.instance.saveTokens(
        accessToken: response.accessToken,
        refreshToken: response.refreshToken,
      );

      for (var i = 0; i < requests.length; i++) {
        requests[i].headers['Authorization'] = 'Bearer ${response.accessToken}';
        try {
          final retryResponse = await _rawDio.fetch(requests[i]);
          completers[i].complete(retryResponse);
        } catch (e) {
          completers[i].completeError(e);
        }
      }
    } catch (e) {
      for (final completer in completers) {
        completer.completeError(e);
      }
      await _ref.read(authProvider).forceLogout();
    } finally {
      _isRefreshing = false;
    }
  }
}
