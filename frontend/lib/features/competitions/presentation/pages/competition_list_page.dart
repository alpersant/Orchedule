import 'package:flutter/material.dart';

import '../../domain/entities/competition.dart';
import '../controllers/competition_list_controller.dart';
import '../widgets/competition_action_dialogs.dart';
import '../widgets/competition_create_dialog.dart';

class CompetitionListPage extends StatefulWidget {
  const CompetitionListPage({
    super.key,
    required this.controller,
  });

  final CompetitionListController controller;

  @override
  State<CompetitionListPage> createState() => _CompetitionListPageState();
}

class _CompetitionListPageState extends State<CompetitionListPage> {
  late final TextEditingController _searchController;

  @override
  void initState() {
    super.initState();

    _searchController = TextEditingController();

    widget.controller.addListener(_onControllerChanged);
    widget.controller.load();
  }

  @override
  void dispose() {
    widget.controller.removeListener(_onControllerChanged);
    _searchController.dispose();

    super.dispose();
  }

  void _onControllerChanged() {
    if (mounted) {
      setState(() {});
    }
  }

  Future<void> _createCompetition() async {
    if (widget.controller.isSaving) {
      return;
    }

    final result = await _showCreateDialog();

    if (!mounted || result == null) {
      return;
    }

    final created = await widget.controller.create(
      name: result.name,
      description: result.description,
    );

    if (!mounted) {
      return;
    }

    _showMessage(
      created
          ? 'Competición creada correctamente.'
          : 'No se pudo crear la competición.',
    );
  }

  Future<CompetitionFormResult?> _showCreateDialog() {
    return showDialog<CompetitionFormResult>(
      context: context,
      builder: (_) => const CompetitionCreateDialog(),
    );
  }

  Future<void> _search() async {
    await widget.controller.load(
      page: 0,
      search: _searchController.text.trim(),
    );
  }

  Future<void> _clearSearch() async {
    _searchController.clear();

    await widget.controller.load(
      page: 0,
      search: '',
    );
  }

  void _showMessage(String message) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(message),
      ),
    );
  }

  Future<void> _editCompetition(Competition competition) async {
    if (widget.controller.isSaving) {
      return;
    }

    final result = await showDialog<CompetitionDetailsResult>(
      context: context,
      builder: (_) => CompetitionEditDialog(
        competition: competition,
      ),
    );

    if (!mounted || result == null) {
      return;
    }

    final updated = await widget.controller.update(
      id: competition.id,
      name: result.name,
      description: result.description,
    );

    if (!mounted) {
      return;
    }

    _showMessage(
      updated
          ? 'Competición actualizada.'
          : 'No se pudo actualizar la competición.',
    );
  }

  Future<void> _duplicateCompetition(Competition competition) async {
    if (widget.controller.isSaving) {
      return;
    }

    final name = await showDialog<String>(
      context: context,
      builder: (_) => CompetitionDuplicateDialog(
        competition: competition,
      ),
    );

    if (!mounted || name == null) {
      return;
    }

    final duplicated = await widget.controller.duplicate(
      competition.id,
      name,
    );

    if (!mounted) {
      return;
    }

    _showMessage(
      duplicated
          ? 'Competición duplicada.'
          : 'No se pudo duplicar la competición.',
    );
  }

  Future<void> _deleteCompetition(Competition competition) async {
    if (widget.controller.isSaving) {
      return;
    }

    final confirmed = await showDialog<bool>(
      context: context,
      barrierDismissible: false,
      builder: (_) => CompetitionDeleteDialog(
        competition: competition,
      ),
    );

    if (!mounted || confirmed != true) {
      return;
    }

    final deleted = await widget.controller.deleteOne(competition.id);

    if (!mounted) {
      return;
    }

    _showMessage(
      deleted
          ? 'Competición eliminada.'
          : 'No se pudo eliminar la competición.',
    );
  }

  Future<void> _changeCompetitionStatus(
      Competition competition, {
        required bool activate,
      }) async {
    if (widget.controller.isSaving) {
      return;
    }

    final confirmed = await showDialog<bool>(
      context: context,
      builder: (dialogContext) => AlertDialog(
        title: Text(
          activate ? 'Activar competición' : 'Cerrar competición',
        ),
        content: Text(
          activate
              ? '¿Quieres activar "${competition.name}"?'
              : '¿Quieres cerrar "${competition.name}"?',
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(dialogContext).pop(false),
            child: const Text('Cancelar'),
          ),
          FilledButton(
            onPressed: () => Navigator.of(dialogContext).pop(true),
            child: Text(activate ? 'Activar' : 'Cerrar'),
          ),
        ],
      ),
    );

    if (!mounted || confirmed != true) {
      return;
    }

    final updated = activate
        ? await widget.controller.activate(competition.id)
        : await widget.controller.close(competition.id);

    if (!mounted) {
      return;
    }

    _showMessage(
      updated
          ? 'Estado actualizado.'
          : 'No se pudo actualizar el estado.',
    );
  }

  @override
  Widget build(BuildContext context) {
    final controller = widget.controller;

    return Scaffold(
      appBar: AppBar(
        title: const Text('Competiciones'),
      ),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: controller.isSaving ? null : _createCompetition,
        icon: const Icon(Icons.add),
        label: const Text('Nueva competición'),
      ),
      body: Column(
        children: [
          Padding(
            padding: const EdgeInsets.fromLTRB(20, 20, 20, 8),
            child: TextField(
              controller: _searchController,
              textInputAction: TextInputAction.search,
              onSubmitted: (_) {
                _search();
              },
              decoration: InputDecoration(
                prefixIcon: const Icon(Icons.search),
                hintText: 'Buscar competición',
                border: const OutlineInputBorder(),
                suffixIcon: IconButton(
                  tooltip: 'Limpiar búsqueda',
                  onPressed: _searchController.text.isEmpty
                      ? null
                      : _clearSearch,
                  icon: const Icon(Icons.clear),
                ),
              ),
            ),
          ),
          if (controller.isLoading)
            const Expanded(
              child: Center(
                child: CircularProgressIndicator(),
              ),
            )
          else if (controller.errorMessage != null)
            Expanded(
              child: Center(
                child: Padding(
                  padding: const EdgeInsets.all(24),
                  child: Column(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Text(
                        controller.errorMessage!,
                        textAlign: TextAlign.center,
                      ),
                      const SizedBox(height: 12),
                      OutlinedButton.icon(
                        onPressed: controller.load,
                        icon: const Icon(Icons.refresh),
                        label: const Text('Reintentar'),
                      ),
                    ],
                  ),
                ),
              ),
            )
          else if (controller.items.isEmpty)
              const Expanded(
                child: Center(
                  child: Padding(
                    padding: EdgeInsets.all(24),
                    child: Text(
                      'No hay competiciones todavía.\n'
                          'Crea la primera para empezar.',
                      textAlign: TextAlign.center,
                    ),
                  ),
                ),
              )
            else
              Expanded(
                child: RefreshIndicator(
                  onRefresh: controller.load,
                  child: ListView.separated(
                    padding: const EdgeInsets.all(20),
                    itemCount: controller.items.length,
                    separatorBuilder: (_, __) {
                      return const SizedBox(height: 10);
                    },
                    itemBuilder: (context, index) {
                      final competition = controller.items[index];

                      return _CompetitionListItem(
                        competition: competition,
                        isSelected: controller.selectedIds.contains(competition.id),
                        enabled: !controller.isSaving && !controller.isLoading,
                        onToggleSelection: () {
                          controller.toggleSelection(competition.id);
                        },
                        onEdit: () => _editCompetition(competition),
                        onDuplicate: () => _duplicateCompetition(competition),
                        onDelete: () => _deleteCompetition(competition),
                        onActivate: () => _changeCompetitionStatus(
                          competition,
                          activate: true,
                        ),
                        onClose: () => _changeCompetitionStatus(
                          competition,
                          activate: false,
                        ),
                      );
                    },
                  ),
                ),
              ),
          if (!controller.isLoading && controller.totalPages > 1)
            SafeArea(
              top: false,
              child: Padding(
                padding: const EdgeInsets.all(12),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    TextButton(
                      onPressed: controller.hasPreviousPage
                          ? () {
                        controller.load(
                          page: controller.page - 1,
                        );
                      }
                          : null,
                      child: const Text('Anterior'),
                    ),
                    Padding(
                      padding: const EdgeInsets.symmetric(horizontal: 16),
                      child: Text(
                        'Página ${controller.page + 1} '
                            'de ${controller.totalPages}',
                      ),
                    ),
                    TextButton(
                      onPressed: controller.hasNextPage
                          ? () {
                        controller.load(
                          page: controller.page + 1,
                        );
                      }
                          : null,
                      child: const Text('Siguiente'),
                    ),
                  ],
                ),
              ),
            ),
        ],
      ),
    );
  }
}

class _CompetitionListItem extends StatelessWidget {
  const _CompetitionListItem({
    required this.competition,
    required this.isSelected,
    required this.enabled,
    required this.onToggleSelection,
    required this.onEdit,
    required this.onDuplicate,
    required this.onDelete,
    required this.onActivate,
    required this.onClose,
  });

  final Competition competition;
  final bool isSelected;
  final bool enabled;

  final VoidCallback onToggleSelection;
  final VoidCallback onEdit;
  final VoidCallback onDuplicate;
  final VoidCallback onDelete;
  final VoidCallback onActivate;
  final VoidCallback onClose;

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;

    return Card(
      child: ListTile(
        leading: Checkbox(
          value: isSelected,
          onChanged: enabled ? (_) => onToggleSelection() : null,
        ),
        title: Text(competition.name),
        subtitle: Padding(
          padding: const EdgeInsets.only(top: 4),
          child: Text(
            '${_statusLabel(competition.status)} · '
                '${competition.defaultFieldCount} campo(s)',
          ),
        ),
        onTap: enabled ? onEdit : null,
        trailing: PopupMenuButton<String>(
          enabled: enabled,
          tooltip: 'Acciones de la competición',
          icon: const Icon(Icons.more_vert),
          onSelected: (action) {
            switch (action) {
              case 'edit':
                onEdit();
                break;
              case 'duplicate':
                onDuplicate();
                break;
              case 'activate':
                onActivate();
                break;
              case 'close':
                onClose();
                break;
              case 'delete':
                onDelete();
                break;
            }
          },
          itemBuilder: (_) => [
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
            const PopupMenuItem<String>(
              value: 'duplicate',
              child: Row(
                children: [
                  Icon(Icons.copy_outlined),
                  SizedBox(width: 8),
                  Text('Duplicar'),
                ],
              ),
            ),
            if (competition.status == CompetitionStatus.draft)
              const PopupMenuItem<String>(
                value: 'activate',
                child: Row(
                  children: [
                    Icon(Icons.play_circle_outline),
                    SizedBox(width: 8),
                    Text('Activar'),
                  ],
                ),
              ),
            if (competition.status == CompetitionStatus.active)
              const PopupMenuItem<String>(
                value: 'close',
                child: Row(
                  children: [
                    Icon(Icons.lock_outline),
                    SizedBox(width: 8),
                    Text('Cerrar'),
                  ],
                ),
              ),
            const PopupMenuDivider(),
            PopupMenuItem<String>(
              value: 'delete',
              child: Row(
                children: [
                  Icon(
                    Icons.delete_outline,
                    color: colors.error,
                  ),
                  const SizedBox(width: 8),
                  Text(
                    'Eliminar',
                    style: TextStyle(color: colors.error),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  String _statusLabel(CompetitionStatus status) {
    return switch (status) {
      CompetitionStatus.draft => 'Borrador',
      CompetitionStatus.active => 'Activa',
      CompetitionStatus.closed => 'Cerrada',
      CompetitionStatus.archived => 'Archivada',
    };
  }
}

