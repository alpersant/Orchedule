import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../application/schedule_round_use_cases.dart';
import '../../data/dto/round_readiness_dto.dart';
import '../../data/dto/schedule_round_dto.dart';

class ScheduleRoundPage extends ConsumerStatefulWidget {
  final String seasonId;

  const ScheduleRoundPage({super.key, required this.seasonId});

  @override
  ConsumerState<ScheduleRoundPage> createState() => _ScheduleRoundPageState();
}

class _ScheduleRoundPageState extends ConsumerState<ScheduleRoundPage> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      ref
          .read(scheduleRoundControllerProvider(widget.seasonId).notifier)
          .checkReadiness();
    });
  }

  @override
  Widget build(BuildContext context) {
    final state = ref.watch(scheduleRoundControllerProvider(widget.seasonId));
    final controller = ref.read(
      scheduleRoundControllerProvider(widget.seasonId).notifier,
    );

    final busy = state.loading || state.mutating;
    final readiness = state.readiness;
    final round = state.round;

    return Scaffold(
      appBar: AppBar(title: Text('Jornada ${state.weekNumber}')),
      body: Align(
        alignment: Alignment.topCenter,
        child: ConstrainedBox(
          constraints: const BoxConstraints(maxWidth: 960),
          child: ListView(
            padding: const EdgeInsets.all(20),
            children: [
              _WeekSelector(
                weekNumber: state.weekNumber,
                enabled: !busy,
                onChanged: (value) {
                  if (value == null) return;
                  controller.checkReadiness();
                },
              ),
              const SizedBox(height: 20),
              if (state.error != null) ...[
                Card(
                  color: Theme.of(context).colorScheme.errorContainer,
                  child: ListTile(
                    leading: const Icon(Icons.error_outline),
                    title: Text(state.error!),
                  ),
                ),
                const SizedBox(height: 16),
              ],
              if (state.loading)
                const Center(child: CircularProgressIndicator())
              else ...[
                if (readiness != null)
                  _ReadinessCard(
                    readiness: readiness,
                    busy: busy,
                    onGenerate: controller.generate,
                  ),
                const SizedBox(height: 20),
                if (round != null)
                  _RoundCard(
                    round: round,
                    busy: busy,
                    onConfirm: controller.confirm,
                    onCancel: controller.cancel,
                    onRegenerate: controller.regenerate,
                  ),
              ],
            ],
          ),
        ),
      ),
    );
  }
}

class _WeekSelector extends StatelessWidget {
  final int weekNumber;
  final bool enabled;
  final ValueChanged<int?> onChanged;

  const _WeekSelector({
    required this.weekNumber,
    required this.enabled,
    required this.onChanged,
  });

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        IconButton(
          onPressed:
              enabled && weekNumber > 1 ? () => onChanged(weekNumber - 1) : null,
          icon: const Icon(Icons.chevron_left),
        ),
        Expanded(
          child: InputDecorator(
            decoration: const InputDecoration(
              labelText: 'Semana',
              border: OutlineInputBorder(),
            ),
            child: DropdownButtonHideUnderline(
              child: DropdownButton<int>(
                value: weekNumber,
                isExpanded: true,
                items: List.generate(60, (index) {
                  final week = index + 1;
                  return DropdownMenuItem(
                    value: week,
                    child: Text('Semana $week'),
                  );
                }),
                onChanged: enabled ? onChanged : null,
              ),
            ),
          ),
        ),
        IconButton(
          onPressed: enabled && weekNumber < 60
              ? () => onChanged(weekNumber + 1)
              : null,
          icon: const Icon(Icons.chevron_right),
        ),
      ],
    );
  }
}

class _ReadinessCard extends StatelessWidget {
  final RoundReadinessDto readiness;
  final bool busy;
  final VoidCallback onGenerate;

  const _ReadinessCard({
    required this.readiness,
    required this.busy,
    required this.onGenerate,
  });

  @override
  Widget build(BuildContext context) {
    final canGenerate = readiness.canAttemptGeneration;

    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Icon(
                  canGenerate ? Icons.check_circle : Icons.block,
                  color: canGenerate
                      ? Theme.of(context).colorScheme.primary
                      : Theme.of(context).colorScheme.error,
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: Text(
                    canGenerate
                        ? 'La jornada puede generarse'
                        : 'La jornada no está lista',
                    style: Theme.of(context).textTheme.titleMedium,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 16),
            Wrap(
              spacing: 24,
              runSpacing: 12,
              children: [
                _Metric('Equipos válidos', readiness.eligibleTeams),
                _Metric('Campos disponibles', readiness.availableFields),
                _Metric('Franjas compatibles', readiness.compatibleSlots),
                _Metric('Capacidad estimada', readiness.estimatedCapacity),
              ],
            ),
            const SizedBox(height: 16),
            if (readiness.issues.isNotEmpty)
              ...readiness.issues.map(
                (issue) => ListTile(
                  dense: true,
                  leading: Icon(
                    issue.severity.toUpperCase() == 'BLOCKER'
                        ? Icons.block
                        : Icons.warning_amber_outlined,
                    color: issue.severity.toUpperCase() == 'BLOCKER'
                        ? Theme.of(context).colorScheme.error
                        : Theme.of(context).colorScheme.tertiary,
                  ),
                  title: Text(issue.message),
                  subtitle: issue.suggestedAction == null
                      ? null
                      : Text(issue.suggestedAction!),
                ),
              ),
            const SizedBox(height: 16),
            FilledButton.icon(
              onPressed: canGenerate && !busy ? onGenerate : null,
              icon: busy
                  ? const SizedBox(
                      width: 18,
                      height: 18,
                      child: CircularProgressIndicator(strokeWidth: 2),
                    )
                  : const Icon(Icons.auto_awesome),
              label: const Text('Generar jornada'),
            ),
          ],
        ),
      ),
    );
  }
}

class _Metric extends StatelessWidget {
  final String label;
  final int value;

  const _Metric(this.label, this.value);

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(label, style: Theme.of(context).textTheme.bodySmall),
        Text(value.toString(), style: Theme.of(context).textTheme.titleLarge),
      ],
    );
  }
}

class _RoundCard extends StatelessWidget {
  final ScheduleRoundDto round;
  final bool busy;
  final VoidCallback onConfirm;
  final VoidCallback onCancel;
  final VoidCallback onRegenerate;

  const _RoundCard({
    required this.round,
    required this.busy,
    required this.onConfirm,
    required this.onCancel,
    required this.onRegenerate,
  });

  @override
  Widget build(BuildContext context) {
    final isDraft = round.status.toUpperCase() == 'DRAFT';
    final isCancelled = round.status.toUpperCase() == 'CANCELLED';

    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              'Jornada ${round.weekNumber} · ${_statusLabel(round.status)}',
              style: Theme.of(context).textTheme.titleMedium,
            ),
            const SizedBox(height: 16),
            if (round.assignments.isEmpty)
              const Text('Esta jornada no tiene asignaciones.')
            else
              ...round.assignments.map(
                (assignment) => ListTile(
                  dense: true,
                  leading: const Icon(Icons.sports_soccer_outlined),
                  title: Text(assignment.teamName),
                  subtitle: Text(
                    '${assignment.dayOfWeek} · ${assignment.startTime}'
                    '${assignment.fieldName == null ? '' : ' · ${assignment.fieldName}'}',
                  ),
                ),
              ),
            const SizedBox(height: 16),
            Wrap(
              spacing: 12,
              children: [
                if (isDraft)
                  FilledButton.icon(
                    onPressed: busy ? null : onConfirm,
                    icon: const Icon(Icons.check),
                    label: const Text('Confirmar'),
                  ),
                if (isDraft || isCancelled)
                  OutlinedButton.icon(
                    onPressed: busy ? null : onRegenerate,
                    icon: const Icon(Icons.refresh),
                    label: const Text('Regenerar'),
                  ),
                if (!isCancelled)
                  OutlinedButton.icon(
                    onPressed: busy ? null : onCancel,
                    icon: const Icon(Icons.cancel_outlined),
                    label: const Text('Cancelar'),
                  ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  String _statusLabel(String status) {
    return switch (status.toUpperCase()) {
      'DRAFT' => 'Borrador',
      'CONFIRMED' => 'Confirmada',
      'CANCELLED' => 'Cancelada',
      _ => status,
    };
  }
}
