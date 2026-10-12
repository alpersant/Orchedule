import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../domain/team.dart';
import 'team_form_page.dart';
import 'teams_controller.dart';

class TeamsPage extends ConsumerStatefulWidget {
  const TeamsPage({super.key});

  @override
  ConsumerState<TeamsPage> createState() => _TeamsPageState();
}

class _TeamsPageState extends ConsumerState<TeamsPage> {
  final Set<String> _selectedIds = <String>{};

  Future<void> _openForm({Team? team}) async {
    await Navigator.of(context).push<bool>(
      MaterialPageRoute<bool>(
        builder: (_) => TeamFormPage(team: team),
      ),
    );
  }

  Future<bool> _confirm({
    required String title,
    required String content,
    required String action,
  }) async {
    final result = await showDialog<bool>(
      context: context,
      builder: (dialogContext) {
        return AlertDialog(
          title: Text(title),
          content: Text(content),
          actions: [
            TextButton(
              onPressed: () {
                Navigator.of(dialogContext).pop(false);
              },
              child: const Text('Cancelar'),
            ),
            FilledButton(
              style: FilledButton.styleFrom(
                backgroundColor:
                Theme.of(dialogContext).colorScheme.error,
                foregroundColor:
                Theme.of(dialogContext).colorScheme.onError,
              ),
              onPressed: () {
                Navigator.of(dialogContext).pop(true);
              },
              child: Text(action),
            ),
          ],
        );
      },
    );

    return result ?? false;
  }

  Future<void> _deleteOne(Team team) async {
    final confirmed = await _confirm(
      title: 'Eliminar equipo',
      content: '¿Quieres eliminar definitivamente "${team.name}"?\n\n'
          'Esta acción no se puede deshacer.',
      action: 'Eliminar',
    );

    if (!confirmed || !mounted) {
      return;
    }

    final deleted = await ref
        .read(teamsProvider.notifier)
        .deleteById(team.id);

    if (!mounted) {
      return;
    }

    if (!deleted) {
      _showError();
    }
  }

  Future<void> _deleteSelected() async {
    if (_selectedIds.isEmpty) {
      return;
    }

    final selectedIds = _selectedIds.toList();
    final count = selectedIds.length;

    final confirmed = await _confirm(
      title: 'Eliminar equipos',
      content: '¿Quieres eliminar los $count equipos seleccionados?\n\n'
          'Esta acción no se puede deshacer.',
      action: 'Eliminar $count',
    );

    if (!confirmed || !mounted) {
      return;
    }

    final deleted = await ref
        .read(teamsProvider.notifier)
        .deleteBulk(selectedIds);

    if (!mounted) {
      return;
    }

    if (deleted) {
      setState(() {
        _selectedIds.clear();
      });
    } else {
      _showError();
    }
  }

  void _showError() {
    final error = ref.read(teamsProvider).error;

    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(
          error ?? 'No se ha podido completar la operación.',
        ),
      ),
    );
  }

  void _toggleSelection(String teamId) {
    setState(() {
      if (_selectedIds.contains(teamId)) {
        _selectedIds.remove(teamId);
      } else {
        _selectedIds.add(teamId);
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    final state = ref.watch(teamsProvider);
    final selectionMode = _selectedIds.isNotEmpty;

    return Scaffold(
      appBar: AppBar(
        title: selectionMode
            ? Text('${_selectedIds.length} seleccionados')
            : const Text('Equipos'),
        actions: [
          if (selectionMode)
            IconButton(
              tooltip: 'Eliminar seleccionados',
              onPressed: _deleteSelected,
              icon: const Icon(Icons.delete_outline),
            ),
          IconButton(
            tooltip: 'Actualizar',
            onPressed: state.loading
                ? null
                : () {
              ref.read(teamsProvider.notifier).load();
            },
            icon: const Icon(Icons.refresh),
          ),
        ],
      ),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: () => _openForm(),
        icon: const Icon(Icons.add),
        label: const Text('Nuevo equipo'),
      ),
      body: _buildBody(state),
    );
  }

  Widget _buildBody(TeamsState state) {
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
                ref.read(teamsProvider.notifier).load();
              },
              child: const Text('Reintentar'),
            ),
          ],
        ),
      );
    }

    if (state.items.isEmpty) {
      return const Center(
        child: Text('Todavía no hay equipos creados.'),
      );
    }

    return ListView.separated(
      padding: const EdgeInsets.all(16),
      itemCount: state.items.length,
      separatorBuilder: (_, __) => const SizedBox(height: 8),
      itemBuilder: (context, index) {
        final team = state.items[index];
        final selected = _selectedIds.contains(team.id);
        final selectionMode = _selectedIds.isNotEmpty;

        return Card(
          child: ListTile(
            onLongPress: () {
              _toggleSelection(team.id);
            },
            onTap: selectionMode
                ? () {
              _toggleSelection(team.id);
            }
                : null,
            leading: selectionMode
                ? Checkbox(
              value: selected,
              onChanged: (_) {
                _toggleSelection(team.id);
              },
            )
                : CircleAvatar(
              child: Icon(
                team.active
                    ? Icons.groups_outlined
                    : Icons.pause_circle_outline,
              ),
            ),
            title: Text(team.name),
            subtitle: Text(
              team.active ? 'Activo' : 'Inactivo',
            ),
            trailing: PopupMenuButton<String>(
              tooltip: 'Acciones del equipo',
              onSelected: (value) async {
                switch (value) {
                  case 'edit':
                    await _openForm(team: team);
                    break;

                  case 'toggle':
                    final updated = await ref
                        .read(teamsProvider.notifier)
                        .setActive(team, !team.active);

                    if (!updated && mounted) {
                      _showError();
                    }
                    break;

                  case 'delete':
                    await _deleteOne(team);
                    break;
                }
              },
              itemBuilder: (_) {
                return [
                  const PopupMenuItem<String>(
                    value: 'edit',
                    child: Row(
                      children: [
                        Icon(Icons.edit_outlined),
                        SizedBox(width: 8),
                        Text('Editar'),
                      ],
                    ),
                  ),
                  PopupMenuItem<String>(
                    value: 'toggle',
                    child: Row(
                      children: [
                        Icon(
                          team.active
                              ? Icons.pause_circle_outline
                              : Icons.play_circle_outline,
                        ),
                        const SizedBox(width: 8),
                        Text(
                          team.active ? 'Desactivar' : 'Activar',
                        ),
                      ],
                    ),
                  ),
                  const PopupMenuDivider(),
                  const PopupMenuItem<String>(
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
                          style: TextStyle(color: Colors.red),
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