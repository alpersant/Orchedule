import 'package:flutter/material.dart';

import '../../../core/widgets/placeholder_page.dart';

class AuditPage extends StatelessWidget {
  const AuditPage({super.key});

  @override
  Widget build(BuildContext context) {
    return const PlaceholderPage(
      title: 'Auditoría',
      description: 'Aquí revisarás equidad, restricciones y excepciones.',
    );
  }
}