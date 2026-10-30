import 'package:flutter/material.dart';

import '../../domain/entities/round_readiness_issue.dart';

class ReadinessIssueCard extends StatelessWidget {
  final RoundReadinessIssue issue;

  const ReadinessIssueCard({
    super.key,
    required this.issue,
  });

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final isError = issue.isBlocker;

    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      child: Semantics(
        label: '${isError ? 'Bloqueo' : 'Aviso'}: ${issue.message}',
        child: ListTile(
          leading: Icon(
            isError ? Icons.block : Icons.warning_amber_outlined,
            color: isError ? colors.error : colors.tertiary,
          ),
          title: Text(issue.message),
          subtitle: issue.suggestedAction == null
              ? null
              : Padding(
            padding: const EdgeInsets.only(top: 4),
            child: Text(issue.suggestedAction!),
          ),
        ),
      ),
    );
  }
}