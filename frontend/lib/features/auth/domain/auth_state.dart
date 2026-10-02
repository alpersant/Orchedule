import 'package:flutter/foundation.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/storage/secure_storage.dart';
import '../data/auth_api.dart';

enum AuthStatus { unknown, authenticated, unauthenticated }

/// Notifier central de sesión. Es un ChangeNotifier puro (no StateNotifier)
/// porque go_router necesita exactamente esa interfaz para su
/// `refreshListenable`, y mezclar ambos causa un conflicto real de
/// firmas en `addListener`.
class AuthNotifier extends ChangeNotifier {
  final AuthApi _authApi;

  AuthStatus status = AuthStatus.unknown;
  String? email;
  String? fullName;
  String? role;

  AuthNotifier(this._authApi) {
    _bootstrap();
  }

  Future<void> _bootstrap() async {
    final hasSession = await SecureStorage.instance.hasSession();
    _setStatus(
      hasSession ? AuthStatus.authenticated : AuthStatus.unauthenticated,
    );
  }

  Future<void> login(String emailInput, String password) async {
    final response = await _authApi.login(emailInput, password);
    await SecureStorage.instance.saveTokens(
      accessToken: response.accessToken,
      refreshToken: response.refreshToken,
    );
    email = response.email;
    fullName = response.fullName;
    role = response.role;
    _setStatus(AuthStatus.authenticated);
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
    this.email = response.email;
    this.fullName = response.fullName;
    role = response.role;
    _setStatus(AuthStatus.authenticated);
  }

  Future<void> logout() async {
    final refreshToken = await SecureStorage.instance.getRefreshToken();
    if (refreshToken != null) {
      try {
        await _authApi.logout(refreshToken);
      } catch (_) {}
    }
    await SecureStorage.instance.clear();
    _setStatus(AuthStatus.unauthenticated);
  }

  Future<void> forceLogout() async {
    await SecureStorage.instance.clear();
    _setStatus(AuthStatus.unauthenticated);
  }

  void _setStatus(AuthStatus newStatus) {
    status = newStatus;
    notifyListeners();
  }
}

final authApiProvider = Provider<AuthApi>((ref) {
  throw UnimplementedError('authApiProvider debe sobreescribirse en main.dart');
});

/// ChangeNotifierProvider expone directamente el AuthNotifier (no un
/// AuthState separado): ref.watch(authProvider) devuelve el notifier,
/// y acceder a .status, .email, etc. ya dispara rebuild automático.
final authProvider = ChangeNotifierProvider<AuthNotifier>((ref) {
  return AuthNotifier(ref.watch(authApiProvider));
});
