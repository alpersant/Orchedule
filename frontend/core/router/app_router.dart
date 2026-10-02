import 'package:flutter/cupertino.dart';
import 'package:go_router/go_router.dart';
import 'auth_change_notifier.dart';
import '../../features/auth/presentation/login_screen.dart';

GoRouter buildRouter(AuthChangeNotifier authNotifier) {
  return GoRouter(
    initialLocation: '/login',
    refreshListenable: authNotifier,
    redirect: (context, state) {
      final isAuthenticated = authNotifier.isAuthenticated;
      final isLoginRoute = state.matchedLocation == '/login';

      if (!isAuthenticated && !isLoginRoute) return '/login';
      if (isAuthenticated && isLoginRoute) return '/home';
      return null;
    },
    routes: [
      GoRoute(path: '/login', builder: (context, state) => const LoginScreen()),
      GoRoute(path: '/home', builder: (context, state) => const Placeholder()),
    ],
  );
}