import 'package:flutter/foundation.dart';
import '../../features/auth/presentation/auth_controller.dart';

class AuthChangeNotifier extends ChangeNotifier {
  AuthStatus _status;

  AuthChangeNotifier(this._status);

  bool get isAuthenticated => _status == AuthStatus.authenticated;

  void update(AuthStatus newStatus) {
    if (_status != newStatus) {
      _status = newStatus;
      notifyListeners();
    }
  }
}