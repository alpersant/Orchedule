import 'package:flutter/material.dart';

import '../../domain/entities/competition.dart';
import '../controllers/competition_list_controller.dart';
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
                        isSelected: controller.selectedIds.contains(
                          competition.id,
                        ),
                        onToggleSelection: () {
                          controller.toggleSelection(competition.id);
                        },
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
    required this.onToggleSelection,
  });

  final Competition competition;
  final bool isSelected;
  final VoidCallback onToggleSelection;

  @override
  Widget build(BuildContext context) {
    final colorScheme = Theme.of(context).colorScheme;

    return Card(
      child: ListTile(
        leading: Checkbox(
          value: isSelected,
          onChanged: (_) {
            onToggleSelection();
          },
        ),
        title: Text(competition.name),
        subtitle: Padding(
          padding: const EdgeInsets.only(top: 4),
          child: Text(
            '${_statusLabel(competition.status)} · '
                '${competition.defaultDays.join(', ')} · '
                '${competition.defaultHours.join(', ')} · '
                '${competition.defaultFieldCount} campo(s)',
          ),
        ),
        trailing: Icon(
          Icons.chevron_right,
          color: colorScheme.outline,
        ),
        onTap: onToggleSelection,
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

