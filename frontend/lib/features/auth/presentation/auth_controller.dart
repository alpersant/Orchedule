import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/storage/secure_storage.dart';
import '../data/auth_api.dart';

enum AuthStatus { unknown, authenticated, unauthenticated }

class AuthState {
  final AuthStatus status;
  final String? error;

  const AuthState({
    required this.status,
    this.error,
  });

  AuthState copyWith({
    AuthStatus? status,
    String? error,
    bool clearError = false,
  }) {
    return AuthState(
      status: status ?? this.status,
      error: clearError ? null : (error ?? this.error),
    );
  }
}

class AuthController extends StateNotifier<AuthState> {
  AuthController({
    required AuthApi authApi,
    required SecureStorage storage,
  })  : _authApi = authApi,
        _storage = storage,
        super(const AuthState(status: AuthStatus.unknown)) {
    _checkInitialAuth();
  }

  final AuthApi _authApi;
  final SecureStorage _storage;

  Future<void> _checkInitialAuth() async {
    final hasSession = await _storage.hasSession();
    state = AuthState(
      status: hasSession
          ? AuthStatus.authenticated
          : AuthStatus.unauthenticated,
    );
  }

  Future<void> login(String email, String password) async {
    try {
      final tokens = await _authApi.login(email, password);
      await _storage.saveTokens(
        accessToken: tokens.accessToken,
        refreshToken: tokens.refreshToken,
      );
      state = const AuthState(status: AuthStatus.authenticated);
    } on DioException catch (error) {
      state = AuthState(
        status: AuthStatus.unauthenticated,
        error: _messageFor(error),
      );
    } catch (_) {
      state = const AuthState(
        status: AuthStatus.unauthenticated,
        error: 'No ha sido posible iniciar sesión.',
      );
    }
  }

  Future<void> logout() async {
    final refreshToken = await _storage.getRefreshToken();

    if (refreshToken != null && refreshToken.isNotEmpty) {
      try {
        await _authApi.logout(refreshToken);
      } catch (_) {
      }
    }

    await forceLogout();
  }

  Future<void> forceLogout() async {
    await _storage.clear();
    state = const AuthState(status: AuthStatus.unauthenticated);
  }

  String _messageFor(DioException error) {
    final statusCode = error.response?.statusCode;
    if (statusCode == 401 || statusCode == 403) {
      return 'Email o contraseña incorrectos.';
    }
    if (error.type == DioExceptionType.connectionTimeout ||
        error.type == DioExceptionType.receiveTimeout) {
      return 'La conexión ha tardado demasiado. Inténtalo de nuevo.';
    }
    return 'No ha sido posible conectar con el servidor.';
  }
}
