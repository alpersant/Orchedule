import 'package:dio/dio.dart';

class AuthTokens {
  const AuthTokens({
    required this.accessToken,
    required this.refreshToken,
  });

  final String accessToken;
  final String refreshToken;

  factory AuthTokens.fromJson(Map<String, dynamic> json) {
    return AuthTokens(
      accessToken: json['accessToken'] as String,
      refreshToken: json['refreshToken'] as String,
    );
  }
}

class AuthApi {
  AuthApi(this._dio);

  final Dio _dio;

  static final Options _jsonOptions = Options(
    contentType: Headers.jsonContentType,
  );

  Future<AuthTokens> login(
      String email,
      String password,
      ) async {
    final response = await _dio.post<Map<String, dynamic>>(
      '/auth/login',
      data: {
        'email': email,
        'password': password,
      },
      options: _jsonOptions.copyWith(
        extra: {
          'skipAuth': true,
        },
      ),
    );

    return AuthTokens.fromJson(response.data!);
  }

  Future<AuthTokens> refresh(String refreshToken) async {
    final response = await _dio.post<Map<String, dynamic>>(
      '/auth/refresh',
      data: {
        'refreshToken': refreshToken,
      },
      options: _jsonOptions.copyWith(
        extra: {
          'skipAuth': true,
        },
      ),
    );

    return AuthTokens.fromJson(response.data!);
  }

  Future<void> logout(String refreshToken) async {
    await _dio.post<void>(
      '/auth/logout',
      data: {
        'refreshToken': refreshToken,
      },
      options: _jsonOptions,
    );
  }
}