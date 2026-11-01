import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../seasons/presentation/seasons_controller.dart';

class SchedulesPage extends ConsumerWidget {
  const SchedulesPage({super.key});
  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final state = ref.watch(seasonsProvider), controller = ref.read(seasonsProvider.notifier);
    return Scaffold(
      appBar: AppBar(title: const Text('Calendarios de partidos'), actions: [
        IconButton(tooltip: 'Actualizar', onPressed: state.loading ? null : () => controller.load(), icon: const Icon(Icons.refresh)),
      ]),
      body: Align(alignment: Alignment.topCenter, child: ConstrainedBox(constraints: const BoxConstraints(maxWidth: 1100), child: Column(children: [
        const Padding(padding: EdgeInsets.all(24), child: Text('Selecciona la temporada para consultar o planificar sus enfrentamientos.')),
        if (state.loading) const LinearProgressIndicator(),
        if (state.error != null) Padding(padding: const EdgeInsets.all(16), child: Text(state.error!)),
        Expanded(child: state.items.isEmpty && !state.loading
          ? const Center(child: Text('No hay temporadas para mostrar.'))
          : ListView.builder(itemCount: state.items.length, itemBuilder: (context, i) {
              final season = state.items[i];
              return Card(margin: const EdgeInsets.symmetric(horizontal: 24, vertical: 6), child: ListTile(
                leading: const Icon(Icons.calendar_month), title: Text(season.name),
                subtitle: Text('${season.startDate.toIso8601String().split('T').first} — ${season.endDate.toIso8601String().split('T').first}'),
                trailing: const Icon(Icons.chevron_right),
                onTap: () => context.pushNamed('season-schedule', pathParameters: {'seasonId': season.id}),
              ));
            })),
        Row(mainAxisAlignment: MainAxisAlignment.center, children: [
          TextButton(onPressed: state.loading || state.page == 0 ? null : () => controller.load(page: state.page - 1), child: const Text('Anterior')),
          Text('Página ${state.page + 1}'),
          TextButton(onPressed: state.loading || state.page + 1 >= state.totalPages ? null : () => controller.load(page: state.page + 1), child: const Text('Siguiente')),
        ]),
      ]))),
    );
  }
}
