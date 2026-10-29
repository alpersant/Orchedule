import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../domain/entities/round_readiness.dart';
import '../controllers/round_readiness_controller.dart';
import '../widgets/readiness_issue_card.dart';
import '../widgets/readiness_summary_card.dart';

class RoundReadinessPage extends ConsumerStatefulWidget {
  final String seasonId;

  const RoundReadinessPage({
    super.key,
    required this.seasonId,
  });

  @override
  ConsumerState<RoundReadinessPage> createState() =>
      _RoundReadinessPageState();
}

class _RoundReadinessPageState extends ConsumerState<RoundReadinessPage> {
  int weekNumber = 1;

  @override
  void initState() {
    super.initState();

    WidgetsBinding.instance.addPostFrameCallback((_) {
      _load();
    });
  }

  void _load() {
    ref
        .read(
      roundReadinessControllerProvider(widget.seasonId).notifier,
    )
        .loadWeek(weekNumber);
  }

  void _changeWeek(int value) {
    setState(() {
      weekNumber = value;
    });

    _load();
  }

  @override
  Widget build(BuildContext context) {
    final state = ref.watch(
      roundReadinessControllerProvider(widget.seasonId),
    );

    return Scaffold(
      appBar: AppBar(
        title: const Text('Preparación de la jornada'),
      ),
      body: Align(
        alignment: Alignment.topCenter,
        child: ConstrainedBox(
          constraints: const BoxConstraints(maxWidth: 960),
          child: ListView(
            padding: const EdgeInsets.all(20),
            children: [
              Row(
                children: [
                  IconButton(
                    tooltip: 'Semana anterior',
                    onPressed: state.loading || weekNumber <= 1
                        ? null
                        : () => _changeWeek(weekNumber - 1),
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
                          value: weekNumber,
                          isExpanded: true,
                          items: List.generate(60, (index) {
                            final value = index + 1;

                            return DropdownMenuItem(
                              value: value,
                              child: Text('Semana $value'),
                            );
                          }),
                          onChanged: state.loading
                              ? null
                              : (value) {
                            if (value != null) {
                              _changeWeek(value);
                            }
                          },
                        ),
                      ),
                    ),
                  ),
                  IconButton(
                    tooltip: 'Semana siguiente',
                    onPressed: state.loading || weekNumber >= 60
                        ? null
                        : () => _changeWeek(weekNumber + 1),
                    icon: const Icon(Icons.chevron_right),
                  ),
                ],
              ),
              const SizedBox(height: 20),
              if (state.loading)
                const Center(
                  child: CircularProgressIndicator(),
                )
              else if (state.errorMessage != null) ...[
                Text(
                  state.errorMessage!,
                  textAlign: TextAlign.center,
                ),
                const SizedBox(height: 12),
                Center(
                  child: FilledButton.icon(
                    onPressed: _load,
                    icon: const Icon(Icons.refresh),
                    label: const Text('Reintentar'),
                  ),
                ),
              ] else if (state.readiness != null) ...[
                ReadinessSummaryCard(
                  summary: state.readiness!.summary,
                ),
                const SizedBox(height: 20),
                _statusBanner(
                  context,
                  state.readiness!,
                ),
                const SizedBox(height: 20),
                if (state.readiness!.issues.isEmpty)
                  const Card(
                    child: ListTile(
                      leading: Icon(Icons.check_circle_outline),
                      title: Text('No se han detectado incidencias.'),
                    ),
                  )
                else
                  ...state.readiness!.issues.map(
                        (issue) => ReadinessIssueCard(issue: issue),
                  ),
              ],
            ],
          ),
        ),
      ),
    );
  }

  Widget _statusBanner(
      BuildContext context,
      RoundReadiness readiness,
      ) {
    final colors = Theme.of(context).colorScheme;
    final canGenerate = readiness.canAttemptGeneration;
    final hasWarnings = readiness.issues.isNotEmpty;

    return Card(
      color: canGenerate ? colors.primaryContainer : colors.errorContainer,
      child: ListTile(
        leading: Icon(
          canGenerate ? Icons.check_circle : Icons.error_outline,
          color: canGenerate ? colors.primary : colors.error,
        ),
        title: Text(
          canGenerate
              ? 'La jornada puede intentarse'
              : 'La jornada no está lista',
        ),
        subtitle: Text(
          canGenerate
              ? hasWarnings
              ? 'Hay avisos que conviene revisar antes de generar.'
              : 'Los datos permiten intentar la planificación.'
              : 'Resuelve los bloqueos antes de intentar generar la jornada.',
        ),
      ),
    );
  }
}