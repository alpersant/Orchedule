import 'package:dio/dio.dart';


class AuthResponse {
  final String accessToken;
  final String refreshToken;
  final String email;
  final String fullName;
  final String role;

  AuthResponse({
    required this.accessToken,
    required this.refreshToken,
    required this.email,
    required this.fullName,
    required this.role,
  });

  factory AuthResponse.fromJson(Map<String, dynamic> json) {
    return AuthResponse(
      accessToken: json['accessToken'] as String,
      refreshToken: json['refreshToken'] as String,
      email: json['email'] as String,
      fullName: json['fullName'] as String,
      role: json['role'] as String,
    );
  }
}


class AuthApi {
  final Dio _rawDio;

  AuthApi(this._rawDio);

  Future<AuthResponse> login(String email, String password) async {
    final response = await _rawDio.post(
      '/api/auth/login',
      data: {'email': email, 'password': password},
    );
    return AuthResponse.fromJson(response.data as Map<String, dynamic>);
  }

  Future<AuthResponse> register({
    required String email,
    required String password,
    required String fullName,
  }) async {
    final response = await _rawDio.post(
      '/api/auth/register',
      data: {'email': email, 'password': password, 'fullName': fullName},
    );
    return AuthResponse.fromJson(response.data as Map<String, dynamic>);
  }

  Future<AuthResponse> refresh(String refreshToken) async {
    final response = await _rawDio.post(
      '/api/auth/refresh',
      data: {'refreshToken': refreshToken},
    );
    return AuthResponse.fromJson(response.data as Map<String, dynamic>);
  }

  Future<void> logout(String refreshToken) async {
    await _rawDio.post(
      '/api/auth/logout',
      data: {'refreshToken': refreshToken},
    );
  }
}
