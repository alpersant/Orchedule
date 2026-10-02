import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/storage/secure_storage.dart';
import '../data/auth_api.dart';

enum AuthStatus {
  unknown,
  authenticated,
  unauthenticated,
}

class AuthState {
  final AuthStatus status;
  final String? email;
  final String? fullName;
  final String? role;

  const AuthState({
    this.status = AuthStatus.unknown,
    this.email,
    this.fullName,
    this.role,
  });

  AuthState copyWith({
    AuthStatus? status,
    String? email,
    String? fullName,
    String? role,
  }) {
    return AuthState(
      status: status ?? this.status,
      email: email ?? this.email,
      fullName: fullName ?? this.fullName,
      role: role ?? this.role,
    );
  }
}

class AuthNotifier extends StateNotifier<AuthState> {
  final AuthApi _authApi;

  AuthNotifier(this._authApi) : super(const AuthState()) {
    _bootstrap();
  }

  Future<void> _bootstrap() async {
    final hasSession = await SecureStorage.instance.hasSession();

    state = state.copyWith(
      status: hasSession
          ? AuthStatus.authenticated
          : AuthStatus.unauthenticated,
    );
  }

  Future<void> login(String email, String password) async {
    final response = await _authApi.login(email, password);

    await SecureStorage.instance.saveTokens(
      accessToken: response.accessToken,
      refreshToken: response.refreshToken,
    );

    state = AuthState(
      status: AuthStatus.authenticated,
      email: response.email,
      fullName: response.fullName,
      role: response.role,
    );
  }

  Future<void> register({
    required String email,
    required String password,
    required String fullName,
  }) async {
    final response = await _authApi.register(
      email: email,
      password: password,
      fullName: fullName,
    );

    await SecureStorage.instance.saveTokens(
      accessToken: response.accessToken,
      refreshToken: response.refreshToken,
    );

    state = AuthState(
      status: AuthStatus.authenticated,
      email: response.email,
      fullName: response.fullName,
      role: response.role,
    );
  }

  Future<void> logout() async {
    final refreshToken =
    await SecureStorage.instance.getRefreshToken();

    if (refreshToken != null) {
      try {
        await _authApi.logout(refreshToken);
      } catch (_) {
        // Aunque falle el logout remoto, limpiamos la sesión local.
      }
    }

    await SecureStorage.instance.clear();

    state = const AuthState(
      status: AuthStatus.unauthenticated,
    );
  }

  Future<void> forceLogout() async {
    await SecureStorage.instance.clear();

    state = const AuthState(
      status: AuthStatus.unauthenticated,
    );
  }
}

final authApiProvider = Provider<AuthApi>((ref) {
  throw UnimplementedError(
    'authApiProvider debe sobreescribirse en main.dart',
  );
});

final authProvider =
StateNotifierProvider<AuthNotifier, AuthState>((ref) {
  return AuthNotifier(ref.watch(authApiProvider));
});