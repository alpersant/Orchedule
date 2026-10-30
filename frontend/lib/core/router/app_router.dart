import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../app/providers.dart';
import '../../features/audit/presentation/audit_page.dart';
import '../../features/auth/domain/auth_state.dart';
import '../../features/auth/presentation/login_page.dart';
import '../../features/competitions/presentation/pages/competitions_page.dart';
import '../../features/dashboard/presentation/dashboard_page.dart';
import '../../features/field_weekly_availability/presentation/pages/field_weekly_availability_page.dart';
import '../../features/schedule_readiness/presentation/pages/round_readiness_page.dart';
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
        builder: (context, state) => const LoginPage(),
      ),

      ShellRoute(
        builder: (context, state, child) {
          return AppShell(
            currentPath: state.matchedLocation,
            child: child,
          );
        },
        routes: [
          GoRoute(
            path: '/',
            builder: (context, state) => const DashboardPage(),
          ),

          GoRoute(
            path: '/competitions',
            builder: (context, state) => const CompetitionsPage(),
          ),

          GoRoute(
            path: '/teams',
            builder: (context, state) => const TeamsPage(),
          ),

          GoRoute(
            path: '/venues',
            builder: (context, state) => const VenuesPage(),
          ),

          GoRoute(
            path: '/seasons',
            builder: (context, state) => const SeasonsPage(),
          ),

          GoRoute(
            path: '/schedules',
            builder: (context, state) => const SchedulesPage(),
          ),

          GoRoute(
            path: '/audit',
            builder: (context, state) => const AuditPage(),
          ),
        ],
      ),

      GoRoute(
        path: '/seasons/:seasonId/availability',
        builder: (context, state) {
          final seasonId = state.pathParameters['seasonId']!;

          return FieldWeeklyAvailabilityPage(seasonId: seasonId);
        },
      ),

      GoRoute(
        path: '/seasons/:seasonId/readiness',
        builder: (context, state) {
          final seasonId = state.pathParameters['seasonId']!;

          return RoundReadinessPage(seasonId: seasonId);
        },
      ),
    ],
  );
});