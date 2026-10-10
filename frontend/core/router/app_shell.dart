import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:orchedule_app/features/auth/domain/auth_state.dart';

import '../../lib/app/providers.dart';
import 'app_router.dart';

class AppShell extends ConsumerWidget {
  const AppShell({
    required this.child,
    super.key,
  });

  final Widget child;

  static const _destinations = [
    (
    route: AppRoute.home,
    icon: Icons.home_outlined,
    label: 'Inicio',
    ),
    (
    route: AppRoute.teams,
    icon: Icons.groups_outlined,
    label: 'Equipos',
    ),
    (
    route: AppRoute.competitions,
    icon: Icons.emoji_events_outlined,
    label: 'Competiciones',
    ),
    (
    route: AppRoute.fields,
    icon: Icons.stadium_outlined,
    label: 'Campos',
    ),
    (
    route: AppRoute.seasons,
    icon: Icons.calendar_month_outlined,
    label: 'Temporadas',
    ),
  ];

  int _currentIndex(BuildContext context) {
    final location = GoRouterState.of(context).matchedLocation;

    final index = _destinations.indexWhere(
          (destination) {
        return location == destination.route.path ||
            location.startsWith('${destination.route.path}/');
      },
    );

    return index == -1 ? 0 : index;
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final authState = ref.watch(authProvider);
    final isWide = MediaQuery.sizeOf(context).width >= 800;
    final currentIndex = _currentIndex(context);

    void onDestinationSelected(int index) {
      context.go(_destinations[index].route.path);
    }

    Future<void> onLogout() async {
      await ref.read(authProvider.notifier).logout();
    }

    if (isWide) {
      return Scaffold(
        body: Row(
          children: [
            NavigationRail(
              selectedIndex: currentIndex,
              onDestinationSelected: onDestinationSelected,
              labelType: NavigationRailLabelType.all,
              leading: Padding(
                padding: const EdgeInsets.symmetric(vertical: 16),
                child: Column(
                  children: [
                    const CircleAvatar(
                      child: Icon(Icons.sports_soccer),
                    ),
                    const SizedBox(height: 8),
                    if (authState.status ==
                        AuthStatus.authenticated)
                      const Text('Sesión activa'),
                  ],
                ),
              ),
              trailing: Expanded(
                child: Align(
                  alignment: Alignment.bottomCenter,
                  child: Padding(
                    padding: const EdgeInsets.only(bottom: 16),
                    child: IconButton(
                      icon: const Icon(Icons.logout),
                      tooltip: 'Cerrar sesión',
                      onPressed: onLogout,
                    ),
                  ),
                ),
              ),
              destinations: _destinations
                  .map(
                    (destination) {
                  return NavigationRailDestination(
                    icon: Icon(destination.icon),
                    label: Text(destination.label),
                  );
                },
              )
                  .toList(),
            ),
            const VerticalDivider(width: 1),
            Expanded(child: child),
          ],
        ),
      );
    }

    return Scaffold(
      appBar: AppBar(
        title: Text(
          _destinations[currentIndex].label,
        ),
        actions: [
          IconButton(
            icon: const Icon(Icons.logout),
            tooltip: 'Cerrar sesión',
            onPressed: onLogout,
          ),
        ],
      ),
      body: child,
      bottomNavigationBar: NavigationBar(
        selectedIndex: currentIndex,
        onDestinationSelected: onDestinationSelected,
        destinations: _destinations
            .map(
              (destination) {
            return NavigationDestination(
              icon: Icon(destination.icon),
              label: destination.label,
            );
          },
        )
            .toList(),
      ),
    );
  }
}