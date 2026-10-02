import 'package:dio/dio.dart';
import '../storage/secure_storage.dart';

class AuthInterceptor extends Interceptor {
  final SecureStorage storage;
  final Dio refreshDio;

  AuthInterceptor(this.storage, this.refreshDio);

  @override
  void onRequest(RequestOptions options, RequestInterceptorHandler handler) async {
    final token = await storage.getAccessToken();
    if (token != null) {
      options.headers['Authorization'] = 'Bearer $token';
    }
    handler.next(options);
  }

  @override
  void onError(DioException err, ErrorInterceptorHandler handler) async {
    if (err.response?.statusCode == 401) {
      final refreshToken = await storage.getRefreshToken();
      if (refreshToken != null) {
        try {
          final response = await refreshDio.post('/api/auth/refresh',
              data: {'refreshToken': refreshToken});
          final newAccessToken = response.data['accessToken'] as String;
          await storage.saveAccessToken(newAccessToken);

          final retryRequest = err.requestOptions;
          retryRequest.headers['Authorization'] = 'Bearer $newAccessToken';
          final retryResponse = await refreshDio.fetch(retryRequest);
          return handler.resolve(retryResponse);
        } catch (_) {
          await storage.clear();
        }
      }
    }
    handler.next(err);
  }
}