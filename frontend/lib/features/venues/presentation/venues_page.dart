import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../domain/venue.dart';
import 'venue_form_page.dart';
import 'venues_controller.dart';
import '../../fields/presentation/venue_fields_page.dart';

class VenuesPage extends ConsumerWidget {
  const VenuesPage({super.key});

  Future<void> _openFields(BuildContext context, Venue venue) async {
    final id = venue.id;
    if (id == null || id.isEmpty) { return; }
    await Navigator.of(context).push<void>(MaterialPageRoute(
      builder: (_) => VenueFieldsPage(venueId: id, venueName: venue.name,
        venueActive: venue.active),
    ));
  }

  Future<void> _openForm(
      BuildContext context, {
        Venue? venue,
      }) async {
    await Navigator.of(context).push<bool>(
      MaterialPageRoute(
        builder: (_) => VenueFormPage(
          venue: venue,
        ),
      ),
    );
  }

  Future<void> _confirmDelete(
      BuildContext context,
      WidgetRef ref,
      Venue venue,
      ) async {
    final venueId = venue.id;

    if (venueId == null || venueId.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('La instalación no tiene identificador.'),
        ),
      );

      return;
    }

    final confirmed = await showDialog<bool>(
      context: context,
      builder: (dialogContext) {
        return AlertDialog(
          title: const Text('Eliminar instalación'),
          content: Text(
            '¿Quieres eliminar definitivamente la instalación '
                '"${venue.name}"?\n\n'
                'Esta acción no se puede deshacer.',
          ),
          actions: [
            TextButton(
              onPressed: () {
                Navigator.of(dialogContext).pop(false);
              },
              child: const Text('Cancelar'),
            ),
            FilledButton(
              style: FilledButton.styleFrom(
                backgroundColor: Theme.of(
                  dialogContext,
                ).colorScheme.error,
                foregroundColor: Theme.of(
                  dialogContext,
                ).colorScheme.onError,
              ),
              onPressed: () {
                Navigator.of(dialogContext).pop(true);
              },
              child: const Text('Eliminar'),
            ),
          ],
        );
      },
    );

    if (confirmed != true || !context.mounted) {
      return;
    }

    final deleted = await ref
        .read(venuesProvider.notifier)
        .deleteById(venueId);

    if (!context.mounted) {
      return;
    }

    if (deleted) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(
            '"${venue.name}" se ha eliminado correctamente.',
          ),
        ),
      );

      return;
    }

    final error = ref.read(venuesProvider).error;

    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(
          error ?? 'No se ha podido eliminar la instalación.',
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final state = ref.watch(venuesProvider);

    return Scaffold(
      appBar: AppBar(
        title: const Text('Instalaciones'),
        actions: [
          IconButton(
            tooltip: 'Actualizar',
            onPressed: state.loading
                ? null
                : () {
              ref.read(venuesProvider.notifier).load();
            },
            icon: const Icon(Icons.refresh),
          ),
        ],
      ),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: () => _openForm(context),
        icon: const Icon(Icons.add),
        label: const Text('Nueva instalación'),
      ),
      body: _body(
        context,
        ref,
        state,
      ),
    );
  }

  Widget _body(
      BuildContext context,
      WidgetRef ref,
      VenuesState state,
      ) {
    if (state.loading && state.items.isEmpty) {
      return const Center(
        child: CircularProgressIndicator(),
      );
    }

    if (state.error != null && state.items.isEmpty) {
      return Center(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Text(state.error!),
            const SizedBox(height: 12),
            FilledButton(
              onPressed: () {
                ref.read(venuesProvider.notifier).load();
              },
              child: const Text('Reintentar'),
            ),
          ],
        ),
      );
    }

    if (state.items.isEmpty) {
      return const Center(
        child: Text(
          'Todavía no hay instalaciones creadas.',
        ),
      );
    }

    return ListView.separated(
      padding: const EdgeInsets.all(16),
      itemCount: state.items.length,
      separatorBuilder: (_, __) {
        return const SizedBox(height: 8);
      },
      itemBuilder: (context, index) {
        final venue = state.items[index];

        final details = [
          venue.city,
          if (venue.address?.isNotEmpty == true)
            venue.address!,
          if (venue.capacity != null)
            'Capacidad: ${venue.capacity}',
          venue.active ? 'Activo' : 'Inactivo',
        ].join(' · ');

        return Card(
          child: ListTile(
            leading: CircleAvatar(
              child: Icon(
                venue.active
                    ? Icons.stadium
                    : Icons.pause,
              ),
            ),
            title: Text(venue.name),
            subtitle: Text('$details · Pulsa para gestionar campos'),
            onTap: state.loading ? null : () => _openFields(context, venue),
            trailing: PopupMenuButton<String>(
              tooltip: 'Acciones de la instalación',
              onSelected: (value) async {
                switch (value) {
                  case 'fields':
                    await _openFields(context, venue);

                  case 'edit':
                    await _openForm(
                      context,
                      venue: venue,
                    );

                  case 'toggle':
                    await ref
                        .read(venuesProvider.notifier)
                        .setActive(
                      venue,
                      !venue.active,
                    );

                  case 'delete':
                    await _confirmDelete(
                      context,
                      ref,
                      venue,
                    );
                }
              },
              itemBuilder: (_) {
                return [
                  const PopupMenuItem(value: 'fields', child: Text('Gestionar campos')),
                  const PopupMenuItem(
                    value: 'edit',
                    child: Row(
                      children: [
                        Icon(Icons.edit_outlined),
                        SizedBox(width: 8),
                        Text('Editar'),
                      ],
                    ),
                  ),
                  PopupMenuItem(
                    value: 'toggle',
                    child: Row(
                      children: [
                        Icon(
                          venue.active
                              ? Icons.pause_circle_outline
                              : Icons.play_circle_outline,
                        ),
                        const SizedBox(width: 8),
                        Text(
                          venue.active
                              ? 'Desactivar'
                              : 'Activar',
                        ),
                      ],
                    ),
                  ),
                  const PopupMenuDivider(),
                  const PopupMenuItem(
                    value: 'delete',
                    child: Row(
                      children: [
                        Icon(
                          Icons.delete_outline,
                          color: Colors.red,
                        ),
                        SizedBox(width: 8),
                        Text(
                          'Eliminar',
                          style: TextStyle(
                            color: Colors.red,
                          ),
                        ),
                      ],
                    ),
                  ),
                ];
              },
            ),
          ),
        );
      },
    );
  }
}