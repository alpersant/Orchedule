import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../features/auth/domain/auth_state.dart';
import '../../features/auth/presentation/login_screen.dart';
import 'app_shell.dart';

enum AppRoute {
  home('/home'),
  teams('/teams'),
  competitions('/competitions'),
  fields('/fields'),
  seasons('/seasons');

  final String path;
  const AppRoute(this.path);
}

final goRouterProvider = Provider<GoRouter>((ref) {
  final authNotifier = ref.watch(authProvider);

  return GoRouter(
    initialLocation: '/login',
    refreshListenable: authNotifier,
    redirect: (context, state) {
      final isLoggingIn = state.matchedLocation == '/login';

      if (authNotifier.status == AuthStatus.unknown) {
        return null;
      }

      final isAuthenticated = authNotifier.status == AuthStatus.authenticated;

      if (!isAuthenticated && !isLoggingIn) {
        return '/login';
      }
      if (isAuthenticated && isLoggingIn) {
        return AppRoute.home.path;
      }
      return null;
    },
    routes: [
      GoRoute(
        path: '/login',
        builder: (context, state) => const LoginScreen(),
      ),
      ShellRoute(
        builder: (context, state, child) => AppShell(child: child),
        routes: [
          GoRoute(
            path: AppRoute.home.path,
            builder: (context, state) => const _PlaceholderPage(title: 'Inicio'),
          ),
          GoRoute(
            path: AppRoute.teams.path,
            builder: (context, state) => const _PlaceholderPage(title: 'Equipos'),
          ),
          GoRoute(
            path: AppRoute.competitions.path,
            builder: (context, state) =>
                const _PlaceholderPage(title: 'Competiciones'),
          ),
          GoRoute(
            path: AppRoute.fields.path,
            builder: (context, state) => const _PlaceholderPage(title: 'Campos'),
          ),
          GoRoute(
            path: AppRoute.seasons.path,
            builder: (context, state) =>
                const _PlaceholderPage(title: 'Temporadas'),
          ),
        ],
      ),
    ],
  );
});

class _PlaceholderPage extends StatelessWidget {
  final String title;
  const _PlaceholderPage({required this.title});

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Text(
        '$title — pantalla pendiente de implementar',
        style: const TextStyle(fontSize: 18),
      ),
    );
  }
}
