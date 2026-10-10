enum AuthStatus {
  unknown,
  authenticated,
  unauthenticated,
}

class AuthState {
  const AuthState({
    required this.status,
    this.error,
  });

  final AuthStatus status;
  final String? error;

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