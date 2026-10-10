import 'package:flutter/foundation.dart';
import 'package:orchedule_app/features/auth/domain/auth_state.dart';

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