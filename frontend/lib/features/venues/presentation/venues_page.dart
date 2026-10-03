import 'package:flutter/material.dart';

import '../../../core/widgets/placeholder_page.dart';

class VenuesPage extends StatelessWidget {
  const VenuesPage({super.key});

  @override
  Widget build(BuildContext context) {
    return const PlaceholderPage(
      title: 'Campos',
      description: 'Aquí gestionarás los campos y sus disponibilidades.',
    );
  }
}