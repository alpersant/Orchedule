import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../app/providers.dart';
import '../../features/audit/presentation/audit_page.dart';
import '../../features/auth/presentation/auth_controller.dart';
import '../../features/auth/presentation/login_page.dart';
import '../../features/competitions/presentation/competitions_page.dart';
import '../../features/dashboard/presentation/dashboard_page.dart';
import '../../features/schedules/presentation/schedules_page.dart';
import '../../features/seasons/presentation/seasons_page.dart';
import '../../features/teams/presentation/teams_page.dart';
import '../../features/venues/presentation/venues_page.dart';
import '../layout/app_shell.dart';

final appRouterProvider = Provider<GoRouter>((ref) {
  final auth = ref.watch(authProvider);

  return GoRouter(
    initialLocation: '/',
    redirect: (context, state) {
      final isLogin = state.matchedLocation == '/login';

      if (auth.status == AuthStatus.unknown) {
        return null;
      }

      if (auth.status == AuthStatus.unauthenticated) {
        return isLogin ? null : '/login';
      }

      if (isLogin) {
        return '/';
      }

      return null;
    },
    routes: [
      GoRoute(
        path: '/login',
        builder: (_, __) => const LoginPage(),
      ),
      ShellRoute(
        builder: (context, state, child) {
          return AppShell(
            currentPath: state.matchedLocation,
            child: child,
          );
        },
        routes: [
          GoRoute(path: '/', builder: (_, __) => const DashboardPage()),
          GoRoute(path: '/competitions', builder: (_, __) => const CompetitionsPage()),
          GoRoute(path: '/teams', builder: (_, __) => const TeamsPage()),
          GoRoute(path: '/venues', builder: (_, __) => const VenuesPage()),
          GoRoute(path: '/seasons', builder: (_, __) => const SeasonsPage()),
          GoRoute(path: '/schedules', builder: (_, __) => const SchedulesPage()),
          GoRoute(path: '/audit', builder: (_, __) => const AuditPage()),
        ],
      ),
    ],
  );
});
