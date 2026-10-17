import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../app/providers.dart';
import '../controllers/field_weekly_availability_controller.dart';

class _FieldOption {
  final String id;
  final String name;

  const _FieldOption({
    required this.id,
    required this.name,
  });

  factory _FieldOption.fromJson(Map<String, dynamic> json) {
    return _FieldOption(
      id: json['id'] as String,
      name: json['name'] as String,
    );
  }
}

class _AvailabilityContext {
  final String seasonName;
  final List<_FieldOption> fields;

  const _AvailabilityContext({
    required this.seasonName,
    required this.fields,
  });
}

/// Loads reference data independently from weekly availability.
/// Loads reference data using the shared API base URL.
class _AvailabilityContextRepository {
  final Dio _dio;

  const _AvailabilityContextRepository(this._dio);

  Future<_AvailabilityContext> load(String seasonId) async {
    final responses = await Future.wait([
      _dio.get<Map<String, dynamic>>(
        '/v1/seasons/$seasonId',
      ),
      _dio.get<dynamic>('/fields'),
    ]);

    final season = responses[0].data;
    final fields = responses[1].data;

    if (season is! Map<String, dynamic> || fields is! List) {
      throw const FormatException(
        'Unexpected availability context response',
      );
    }

    final options = fields
        .map(
          (item) => _FieldOption.fromJson(
        Map<String, dynamic>.from(item as Map),
      ),
    )
        .toList()
      ..sort(
            (a, b) => a.name.toLowerCase().compareTo(
          b.name.toLowerCase(),
        ),
      );

    return _AvailabilityContext(
      seasonName: season['name'] as String,
      fields: List<_FieldOption>.unmodifiable(options),
    );
  }
}

final _availabilityContextRepositoryProvider =
Provider<_AvailabilityContextRepository>((ref) {
  return _AvailabilityContextRepository(ref.watch(dioProvider));
});

final _availabilityContextProvider =
FutureProvider.autoDispose.family<_AvailabilityContext, String>(
      (ref, seasonId) {
    return ref
        .watch(_availabilityContextRepositoryProvider)
        .load(seasonId);
  },
);

class FieldWeeklyAvailabilityPage extends ConsumerStatefulWidget {
  final String seasonId;

  const FieldWeeklyAvailabilityPage({
    super.key,
    required this.seasonId,
  });

  @override
  ConsumerState<FieldWeeklyAvailabilityPage> createState() =>
      _FieldWeeklyAvailabilityPageState();
}

class _FieldWeeklyAvailabilityPageState
    extends ConsumerState<FieldWeeklyAvailabilityPage> {
  int _weekNumber = 1;
  bool _dirty = false;
  bool _interactionPending = false;

  @override
  void initState() {
    super.initState();

    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (mounted) {
        _loadWeek();
      }
    });
  }

  void _loadWeek() {
    ref
        .read(
      fieldWeeklyAvailabilityControllerProvider(widget.seasonId).notifier,
    )
        .loadWeek(_weekNumber);
  }

  Future<bool> _confirmDiscard() async {
    if (!_dirty) {
      return true;
    }

    final discard = await showDialog<bool>(
      context: context,
      builder: (dialogContext) => AlertDialog(
        title: const Text('Hay cambios sin guardar'),
        content: const Text(
          'Si continúas, se perderán los cambios de esta semana.',
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(dialogContext).pop(false),
            child: const Text('Seguir editando'),
          ),
          FilledButton(
            onPressed: () => Navigator.of(dialogContext).pop(true),
            child: const Text('Descartar cambios'),
          ),
        ],
      ),
    );

    return discard == true;
  }

  Future<void> _changeWeek(int week) async {
    if (_interactionPending) { return; }
    setState(() { _interactionPending = true; });
    try {
      await _changeWeekInternal(week);
    } finally {
      if (mounted) { setState(() { _interactionPending = false; }); }
    }
  }

  Future<void> _changeWeekInternal(int week) async {
    if (week < 1 || week > 60 || week == _weekNumber) {
      return;
    }

    if (!await _confirmDiscard() || !mounted) {
      return;
    }

    setState(() {
      _weekNumber = week;
      _dirty = false;
    });

    _loadWeek();
  }

  void _toggleField(String fieldId) {
    final controller = ref.read(
      fieldWeeklyAvailabilityControllerProvider(widget.seasonId).notifier,
    );

    controller.toggleField(fieldId);

    setState(() {
      _dirty = true;
    });
  }

  Future<void> _save() async {
    if (_interactionPending) { return; }
    setState(() { _interactionPending = true; });
    try {
      await _saveInternal();
    } finally {
      if (mounted) { setState(() { _interactionPending = false; }); }
    }
  }

  Future<void> _saveInternal() async {
    final state = ref.read(
      fieldWeeklyAvailabilityControllerProvider(widget.seasonId),
    );

    if (state.selectedFieldIds.isEmpty) {
      final confirmed = await showDialog<bool>(
        context: context,
        builder: (dialogContext) => AlertDialog(
          title: const Text('Guardar semana sin campos'),
          content: Text(
            'La semana $_weekNumber quedará sin campos disponibles. '
                'No se podrán generar asignaciones para esa semana.',
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.of(dialogContext).pop(false),
              child: const Text('Cancelar'),
            ),
            FilledButton(
              onPressed: () => Navigator.of(dialogContext).pop(true),
              child: const Text('Guardar sin campos'),
            ),
          ],
        ),
      );

      if (confirmed != true || !mounted) {
        return;
      }
    }

    final saved = await ref
        .read(
      fieldWeeklyAvailabilityControllerProvider(widget.seasonId).notifier,
    )
        .saveWeek(_weekNumber);

    if (!mounted) {
      return;
    }

    if (saved) {
      setState(() {
        _dirty = false;
      });

      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(
            'Disponibilidad de la semana $_weekNumber guardada.',
          ),
        ),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    final reference = ref.watch(
      _availabilityContextProvider(widget.seasonId),
    );

    final weekly = ref.watch(
      fieldWeeklyAvailabilityControllerProvider(widget.seasonId),
    );

    final busy = weekly.loading || _interactionPending;

    return PopScope(
      canPop: !_dirty && !busy,
      onPopInvokedWithResult: (didPop, result) async {
        if (didPop || busy) {
          return;
        }

        final discard = await _confirmDiscard();

        if (!discard || !mounted) {
          return;
        }

        setState(() {
          _dirty = false;
        });

        WidgetsBinding.instance.addPostFrameCallback((_) {
          if (mounted) {
            Navigator.of(context).pop();
          }
        });
      },
      child: Scaffold(
        appBar: AppBar(
          title: const Text('Campos disponibles'),
        ),
        body: reference.when(
          loading: () => const Center(
            child: CircularProgressIndicator(),
          ),
          error: (error, stackTrace) => Center(
            child: Padding(
              padding: const EdgeInsets.all(24),
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  const Icon(Icons.cloud_off_outlined, size: 48),
                  const SizedBox(height: 16),
                  const Text(
                    'No se pudieron cargar la temporada y los campos.',
                    textAlign: TextAlign.center,
                  ),
                  const SizedBox(height: 16),
                  FilledButton.icon(
                    onPressed: () {
                      ref.invalidate(
                        _availabilityContextProvider(widget.seasonId),
                      );
                    },
                    icon: const Icon(Icons.refresh),
                    label: const Text('Reintentar'),
                  ),
                ],
              ),
            ),
          ),
          data: (referenceData) {
            final fields = referenceData.fields;
            final activeIds = fields.map((field) => field.id).toSet();

            final selectedCount = weekly.selectedFieldIds
                .where(activeIds.contains)
                .length;

            return Align(
              alignment: Alignment.topCenter,
              child: ConstrainedBox(
                constraints: const BoxConstraints(maxWidth: 960),
                child: Column(
                  children: [
                    Padding(
                      padding: const EdgeInsets.all(20),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            referenceData.seasonName,
                            style: Theme.of(context).textTheme.headlineSmall,
                          ),
                          const SizedBox(height: 8),
                          const Text(
                            'Elige los campos que se podrán utilizar. '
                                'Los cambios solo afectan a la semana seleccionada.',
                          ),
                          const SizedBox(height: 20),
                          Row(
                            children: [
                              IconButton(
                                tooltip: 'Semana anterior',
                                onPressed:
                                busy || _weekNumber <= 1
                                    ? null
                                    : () => _changeWeek(_weekNumber - 1),
                                icon: const Icon(Icons.chevron_left),
                              ),
                              Expanded(
                                child: InputDecorator(
                                  decoration: const InputDecoration(
                                    labelText: 'Semana de la temporada',
                                    border: OutlineInputBorder(),
                                  ),
                                  child: DropdownButtonHideUnderline(
                                    child: DropdownButton<int>(
                                  value: _weekNumber,
                                  isExpanded: true,
                                  items: List.generate(
                                    60,
                                        (index) => DropdownMenuItem(
                                      value: index + 1,
                                      child: Text('Semana ${index + 1}'),
                                    ),
                                  ),
                                  onChanged: busy
                                      ? null
                                      : (week) {
                                    if (week != null) {
                                      _changeWeek(week);
                                    }
                                  },
                                ),
                                    ),
                                  ),
                              ),
                              IconButton(
                                tooltip: 'Semana siguiente',
                                onPressed:
                                busy || _weekNumber >= 60
                                    ? null
                                    : () => _changeWeek(_weekNumber + 1),
                                icon: const Icon(Icons.chevron_right),
                              ),
                            ],
                          ),
                          const SizedBox(height: 16),
                          Text(
                            '$selectedCount de ${fields.length} campos '
                                'seleccionados'
                                '${_dirty ? ' · Cambios sin guardar' : ''}',
                            style: Theme.of(context).textTheme.titleSmall,
                          ),
                        ],
                      ),
                    ),
                    if (busy)
                      const LinearProgressIndicator(),
                    if (weekly.errorMessage != null)
                      Padding(
                        padding: const EdgeInsets.symmetric(horizontal: 20),
                        child: MaterialBanner(
                          content: Text(weekly.errorMessage!),
                          actions: [
                            TextButton(
                              onPressed: busy ? null : _loadWeek,
                              child: const Text('Reintentar'),
                            ),
                          ],
                        ),
                      ),
                    Expanded(
                      child: fields.isEmpty
                          ? const Center(
                        child: Padding(
                          padding: EdgeInsets.all(24),
                          child: Column(
                            mainAxisSize: MainAxisSize.min,
                            children: [
                              Icon(
                                Icons.sports_soccer_outlined,
                                size: 48,
                              ),
                              SizedBox(height: 16),
                              Text(
                                'Todavía no hay campos activos',
                                textAlign: TextAlign.center,
                              ),
                              SizedBox(height: 8),
                              Text(
                                'Crea o activa un campo antes de '
                                    'configurar la disponibilidad.',
                                textAlign: TextAlign.center,
                              ),
                            ],
                          ),
                        ),
                      )
                          : ListView.separated(
                        padding: const EdgeInsets.fromLTRB(
                          20,
                          8,
                          20,
                          20,
                        ),
                        itemCount: fields.length,
                        separatorBuilder: (_, __) =>
                        const SizedBox(height: 8),
                        itemBuilder: (context, index) {
                          final field = fields[index];
                          final selected = weekly.selectedFieldIds
                              .contains(field.id);

                          return Card(
                            child: SwitchListTile(
                              secondary: Icon(
                                selected
                                    ? Icons.check_circle_outline
                                    : Icons.sports_soccer_outlined,
                              ),
                              title: Text(field.name),
                              subtitle: Text(
                                selected
                                    ? 'Se podrá utilizar esta semana'
                                    : 'No se utilizará esta semana',
                              ),
                              value: selected,
                              onChanged: busy ||
                                  weekly.errorMessage != null
                                  ? null
                                  : (_) => _toggleField(field.id),
                            ),
                          );
                        },
                      ),
                    ),
                    SafeArea(
                      top: false,
                      child: Padding(
                        padding: const EdgeInsets.all(20),
                        child: SizedBox(
                          width: double.infinity,
                          child: FilledButton.icon(
                            onPressed: busy ||
                                weekly.errorMessage != null ||
                                fields.isEmpty ||
                                !_dirty
                                ? null
                                : _save,
                            icon: const Icon(Icons.save_outlined),
                            label: Text(
                              'Guardar semana $_weekNumber',
                            ),
                          ),
                        ),
                      ),
                    ),
                  ],
                ),
              ),
            );
          },
        ),
      ),
    );
  }
}
