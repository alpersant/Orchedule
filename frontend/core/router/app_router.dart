import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:orchedule_app/features/auth/presentation/login_screen.dart';
import 'package:orchedule_app/features/venues/presentation/venue_form_page.dart';

import '../../lib/features/venues/presentation/venues_page.dart';
import 'app_shell.dart';
import 'auth_change_notifier.dart';

enum AppRoute {
  home('/home'),
  teams('/teams'),
  competitions('/competitions'),
  fields('/venues'),
  seasons('/seasons');

  const AppRoute(this.path);

  final String path;
}

GoRouter buildRouter(AuthChangeNotifier authNotifier) {
  return GoRouter(
    initialLocation: '/login',
    refreshListenable: authNotifier,
    redirect: (context, state) {
      final isAuthenticated = authNotifier.isAuthenticated;
      final isLoginRoute = state.matchedLocation == '/login';

      if (!isAuthenticated && !isLoginRoute) {
        return '/login';
      }

      if (isAuthenticated && isLoginRoute) {
        return AppRoute.home.path;
      }

      return null;
    },
    routes: [
      GoRoute(
        path: '/login',
        builder: (context, state) {
          return const LoginScreen();
        },
      ),

      ShellRoute(
        builder: (context, state, child) {
          return AppShell(child: child);
        },
        routes: [
          GoRoute(
            path: AppRoute.home.path,
            builder: (context, state) {
              return const _HomePage();
            },
          ),
/*
          GoRoute(
            path: AppRoute.teams.path,
            builder: (context, state) {
              return const TeamsPage();
            },
          ),

          GoRoute(
            path: AppRoute.competitions.path,
            builder: (context, state) {
              return const CompetitionsPage();
            },
          ),*/

          GoRoute(
            path: AppRoute.fields.path,
            builder: (context, state) {
              return const VenuesPage();
            },
            routes: [
              GoRoute(
                path: 'new',
                builder: (context, state) {
                  return const VenueFormPage();
                },
              ),
            ],
          ),
/*
          GoRoute(
            path: AppRoute.seasons.path,
            builder: (context, state) {
              return const SeasonsPage();
            },
          ),*/
        ],
      ),
    ],
  );
}

class _HomePage extends StatelessWidget {
  const _HomePage();

  @override
  Widget build(BuildContext context) {
    return const Scaffold(
      body: Center(
        child: Text('Inicio'),
      ),
    );
  }
}