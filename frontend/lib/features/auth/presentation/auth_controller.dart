import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/storage/secure_storage.dart';
import '../data/auth_api.dart';
import '../domain/auth_state.dart';

class AuthController extends StateNotifier<AuthState> {
  AuthController({
    required AuthApi authApi,
    required SecureStorage storage,
  })  : _authApi = authApi,
        _storage = storage,
        super(
        const AuthState(
          status: AuthStatus.unknown,
        ),
      ) {
    _checkInitialAuth();
  }

  final AuthApi _authApi;
  final SecureStorage _storage;

  Future<void> _checkInitialAuth() async {
    final accessToken = await _storage.getAccessToken();

    state = AuthState(
      status: accessToken != null && accessToken.isNotEmpty
          ? AuthStatus.authenticated
          : AuthStatus.unauthenticated,
    );
  }

  Future<void> login(
      String email,
      String password,
      ) async {
    final tokens = await _authApi.login(
      email,
      password,
    );

    await _storage.saveTokens(
      accessToken: tokens.accessToken,
      refreshToken: tokens.refreshToken,
    );

    state = const AuthState(
      status: AuthStatus.authenticated,
    );
  }

  Future<void> logout() async {
    final refreshToken = await _storage.getRefreshToken();

    if (refreshToken != null && refreshToken.isNotEmpty) {
      try {
        await _authApi.logout(refreshToken);
      } catch (_) {
        // El logout local debe completarse incluso si falla el backend.
      }
    }

    await forceLogout();
  }

  Future<void> forceLogout() async {
    await _storage.clear();

    state = const AuthState(
      status: AuthStatus.unauthenticated,
    );
  }

  String _errorMessage(DioException error) {
    final status = error.response?.statusCode;

    if (status == 401 || status == 403) {
      return 'Email o contraseña incorrectos.';
    }

    if (error.type == DioExceptionType.connectionTimeout ||
        error.type == DioExceptionType.receiveTimeout) {
      return 'La conexión ha tardado demasiado.';
    }

    return 'No se ha podido conectar con el servidor.';
  }
}