import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../data/auth_api.dart';
import '../../../core/storage/secure_storage.dart';

enum AuthStatus { unknown, authenticated, unauthenticated }

class AuthState {
  final AuthStatus status;
  final String? error;
  AuthState({required this.status, this.error});
}

class AuthController extends StateNotifier<AuthState> {
  final AuthApi authApi;
  final SecureStorage storage;

  AuthController(this.authApi, this.storage)
      : super(AuthState(status: AuthStatus.unknown)) {
    _checkInitialAuth();
  }

  Future<void> _checkInitialAuth() async {
    final token = await storage.getAccessToken();

    state = AuthState(
      status: token != null
          ? AuthStatus.authenticated
          : AuthStatus.unauthenticated,
    );
  }

  Future<void> login(String email, String password) async {
    try {
      final tokens = await authApi.login(email, password);

      await storage.saveAccessToken(tokens.accessToken);
      await storage.saveRefreshToken(tokens.refreshToken);

      state = AuthState(
        status: AuthStatus.authenticated,
      );
    } catch (e) {
      state = AuthState(
        status: AuthStatus.unauthenticated,
        error: 'Credenciales inválidas',
      );
    }
  }

  Future<void> logout() async {
    final refreshToken = await storage.getRefreshToken();

    if (refreshToken != null) {
      try {
        await authApi.logout(refreshToken);
      } catch (_) {
      }
    }

    await storage.clear();

    state = AuthState(
      status: AuthStatus.unauthenticated,
    );
  }
}