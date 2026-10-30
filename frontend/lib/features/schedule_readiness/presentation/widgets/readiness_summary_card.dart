import 'package:flutter/material.dart';

import '../../domain/entities/round_readiness.dart';

class ReadinessSummaryCard extends StatelessWidget {
  final RoundReadinessSummary summary;

  const ReadinessSummaryCard({
    super.key,
    required this.summary,
  });

  @override
  Widget build(BuildContext context) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Wrap(
          spacing: 24,
          runSpacing: 12,
          children: [
            _item(
              context,
              'Equipos válidos',
              summary.eligibleTeams.toString(),
            ),
            _item(
              context,
              'Campos disponibles',
              summary.availableFields.toString(),
            ),
            _item(
              context,
              'Franjas compatibles',
              summary.compatibleSlots.toString(),
            ),
            _item(
              context,
              'Capacidad estimada',
              summary.estimatedCapacity.toString(),
            ),
          ],
        ),
      ),
    );
  }

  Widget _item(
      BuildContext context,
      String label,
      String value,
      ) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      mainAxisSize: MainAxisSize.min,
      children: [
        Text(
          label,
          style: Theme.of(context).textTheme.bodySmall,
        ),
        const SizedBox(height: 4),
        Text(
          value,
          style: Theme.of(context).textTheme.titleLarge,
        ),
      ],
    );
  }
}