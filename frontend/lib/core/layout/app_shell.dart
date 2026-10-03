import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../app/providers.dart';

class AppShell extends ConsumerWidget {
  const AppShell({
    required this.child,
    required this.currentPath,
    super.key,
  });

  final Widget child;
  final String currentPath;

  static const _items = <_NavigationItem>[
    _NavigationItem('Inicio', Icons.dashboard_outlined, '/'),
    _NavigationItem('Competiciones', Icons.emoji_events_outlined, '/competitions'),
    _NavigationItem('Equipos', Icons.groups_outlined, '/teams'),
    _NavigationItem('Campos', Icons.stadium_outlined, '/venues'),
    _NavigationItem('Temporadas', Icons.calendar_month_outlined, '/seasons'),
    _NavigationItem('Calendarios', Icons.event_note_outlined, '/schedules'),
    _NavigationItem('Auditoría', Icons.fact_check_outlined, '/audit'),
  ];

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final selectedIndex = _items.indexWhere((item) => item.path == currentPath);
    final isDesktop = MediaQuery.sizeOf(context).width >= 900;

    if (!isDesktop) {
      return Scaffold(
        appBar: AppBar(
          title: const Text('Orchedule'),
          actions: [_LogoutButton(ref: ref)],
        ),
        body: child,
        bottomNavigationBar: NavigationBar(
          selectedIndex: selectedIndex < 0 ? 0 : selectedIndex,
          onDestinationSelected: (index) => context.go(_items[index].path),
          destinations: _items
              .map((item) => NavigationDestination(
                    icon: Icon(item.icon),
                    label: item.label,
                  ))
              .toList(),
        ),
      );
    }

    return Scaffold(
      body: Row(
        children: [
          NavigationRail(
            extended: true,
            selectedIndex: selectedIndex < 0 ? 0 : selectedIndex,
            onDestinationSelected: (index) => context.go(_items[index].path),
            leading: const Padding(
              padding: EdgeInsets.symmetric(vertical: 24),
              child: Text(
                'Orchedule',
                style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
              ),
            ),
            trailing: Expanded(
              child: Align(
                alignment: Alignment.bottomCenter,
                child: Padding(
                  padding: const EdgeInsets.only(bottom: 16),
                  child: _LogoutButton(ref: ref),
                ),
              ),
            ),
            destinations: _items
                .map((item) => NavigationRailDestination(
                      icon: Icon(item.icon),
                      label: Text(item.label),
                    ))
                .toList(),
          ),
          const VerticalDivider(width: 1),
          Expanded(child: child),
        ],
      ),
    );
  }
}

class _LogoutButton extends StatelessWidget {
  const _LogoutButton({required this.ref});

  final WidgetRef ref;

  @override
  Widget build(BuildContext context) {
    return IconButton(
      tooltip: 'Cerrar sesión',
      icon: const Icon(Icons.logout),
      onPressed: () => ref.read(authProvider.notifier).logout(),
    );
  }
}

class _NavigationItem {
  const _NavigationItem(this.label, this.icon, this.path);

  final String label;
  final IconData icon;
  final String path;
}
