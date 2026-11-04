import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../domain/planning_view.dart';
import '../data/scheduling_update_api.dart';
import '../domain/scheduling_update_models.dart';

class GameActionsSheet extends ConsumerStatefulWidget {
  final String seasonId;
  final PlanningGame game;

  const GameActionsSheet({
    super.key,
    required this.seasonId,
    required this.game,
  });

  @override
  ConsumerState<GameActionsSheet> createState() => _GameActionsSheetState();
}

class _GameActionsSheetState extends ConsumerState<GameActionsSheet> {
  bool busy = false;
  String? error;
  List<GameAlternative> alternatives = const [];

  SchedulingUpdateApi get api => ref.read(schedulingUpdateApiProvider);

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) => load());
  }

  Future<void> load() async {
    try {
      final result = await api.alternatives(widget.game.id);
      if (mounted) setState(() => alternatives = result);
    } catch (e) {
      if (mounted) setState(() => error = SchedulingUpdateApi.message(e));
    }
  }

  Future<void> run(Future<void> Function() action, String message) async {
    if (busy) return;
    setState(() {
      busy = true;
      error = null;
    });
    try {
      await action();
      if (mounted) {
        Navigator.pop(context, message);
      }
    } catch (e) {
      if (mounted) setState(() {
        busy = false;
        error = SchedulingUpdateApi.message(e);
      });
    }
  }

  Future<void> proposeAlternative() async {
    final values = await showDialog<({int week, String day, String time, String? reason})>(
      context: context,
      builder: (context) => _AlternativeDialog(),
    );
    if (values == null) return;

    await run(
      () => api.createAlternative(
        gameId: widget.game.id,
        alternativeWeek: values.week,
        proposedDay: values.day,
        proposedTime: values.time,
        reason: values.reason,
      ),
      'Alternativa propuesta.',
    );
  }

  @override
  Widget build(BuildContext context) {
    return SafeArea(
      child: Padding(
        padding: const EdgeInsets.all(20),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Text(
              '${widget.game.home} vs ${widget.game.away}',
              style: Theme.of(context).textTheme.titleLarge,
            ),
            const SizedBox(height: 6),
            Text(
              widget.game.field ?? 'Campo pendiente',
              style: Theme.of(context).textTheme.bodyMedium,
            ),
            const SizedBox(height: 20),
            if (error != null) ...[
              Text(
                error!,
                style: TextStyle(color: Theme.of(context).colorScheme.error),
              ),
              const SizedBox(height: 12),
            ],
            Wrap(
              spacing: 10,
              runSpacing: 10,
              children: [
                FilledButton.icon(
                  onPressed: busy
                      ? null
                      : () => run(
                          () => api.confirmGame(widget.game.id),
                          'Partido confirmado.',
                        ),
                  icon: const Icon(Icons.check),
                  label: const Text('Confirmar partido'),
                ),
                OutlinedButton.icon(
                  onPressed: busy
                      ? null
                      : () => run(
                          () => api.unconfirmGame(widget.game.id),
                          'Partido devuelto a provisional.',
                        ),
                  icon: const Icon(Icons.undo),
                  label: const Text('Desconfirmar'),
                ),
                OutlinedButton.icon(
                  onPressed: busy ? null : proposeAlternative,
                  icon: const Icon(Icons.event_repeat),
                  label: const Text('Proponer otra semana'),
                ),
              ],
            ),
            const SizedBox(height: 20),
            Text(
              'Alternativas (${alternatives.length})',
              style: Theme.of(context).textTheme.titleMedium,
            ),
            const SizedBox(height: 8),
            if (alternatives.isEmpty)
              const Text('Todavía no hay alternativas para este partido.')
            else
              ...alternatives.map(
                (item) => Card(
                  child: ListTile(
                    title: Text(
                      'Semana ${item.alternativeWeek} · '
                      '${item.proposedDay} ${item.proposedTime}',
                    ),
                    subtitle: Text(item.status),
                    trailing: item.isProposed
                        ? Wrap(
                            spacing: 6,
                            children: [
                              IconButton(
                                tooltip: 'Aceptar',
                                onPressed: busy
                                    ? null
                                    : () => run(
                                        () => api.decideAlternative(item.id, true),
                                        'Alternativa aceptada.',
                                      ),
                                icon: const Icon(Icons.check),
                              ),
                              IconButton(
                                tooltip: 'Rechazar',
                                onPressed: busy
                                    ? null
                                    : () => run(
                                        () => api.decideAlternative(item.id, false),
                                        'Alternativa rechazada.',
                                      ),
                                icon: const Icon(Icons.close),
                              ),
                            ],
                          )
                        : null,
                  ),
                ),
              ),
          ],
        ),
      ),
    );
  }
}

class _AlternativeDialog extends StatefulWidget {
  @override
  State<_AlternativeDialog> createState() => _AlternativeDialogState();
}

class _AlternativeDialogState extends State<_AlternativeDialog> {
  final weekController = TextEditingController(text: '1');
  String day = 'MONDAY';
  String time = '18:00';
  final reasonController = TextEditingController();

  @override
  void dispose() {
    weekController.dispose();
    reasonController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      title: const Text('Proponer alternativa'),
      content: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          TextField(
            controller: weekController,
            keyboardType: TextInputType.number,
            decoration: const InputDecoration(labelText: 'Semana alternativa'),
          ),
          DropdownButtonFormField<String>(
            initialValue: day,
            items: const [
              DropdownMenuItem(value: 'MONDAY', child: Text('Lunes')),
              DropdownMenuItem(value: 'TUESDAY', child: Text('Martes')),
              DropdownMenuItem(value: 'WEDNESDAY', child: Text('Miércoles')),
              DropdownMenuItem(value: 'THURSDAY', child: Text('Jueves')),
              DropdownMenuItem(value: 'FRIDAY', child: Text('Viernes')),
              DropdownMenuItem(value: 'SATURDAY', child: Text('Sábado')),
              DropdownMenuItem(value: 'SUNDAY', child: Text('Domingo')),
            ],
            onChanged: (value) => setState(() => day = value ?? day),
            decoration: const InputDecoration(labelText: 'Día'),
          ),
          DropdownButtonFormField<String>(
            initialValue: time,
            items: const [
              DropdownMenuItem(value: '18:00', child: Text('18:00')),
              DropdownMenuItem(value: '19:00', child: Text('19:00')),
              DropdownMenuItem(value: '20:00', child: Text('20:00')),
              DropdownMenuItem(value: '21:00', child: Text('21:00')),
              DropdownMenuItem(value: '22:00', child: Text('22:00')),
            ],
            onChanged: (value) => setState(() => time = value ?? time),
            decoration: const InputDecoration(labelText: 'Hora'),
          ),
          TextField(
            controller: reasonController,
            decoration: const InputDecoration(labelText: 'Motivo (opcional)'),
          ),
        ],
      ),
      actions: [
        TextButton(
          onPressed: () => Navigator.pop(context),
          child: const Text('Cancelar'),
        ),
        FilledButton(
          onPressed: () => Navigator.pop(
            context,
            (
              week: int.tryParse(weekController.text.trim()) ?? 0,
              day: day,
              time: time,
              reason: reasonController.text.trim().isEmpty
                  ? null
                  : reasonController.text.trim(),
            ),
          ),
          child: const Text('Proponer'),
        ),
      ],
    );
  }
}
