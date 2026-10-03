import 'package:flutter/material.dart';

import '../../../core/widgets/placeholder_page.dart';

class TeamsPage extends StatelessWidget {
  const TeamsPage({super.key});

  @override
  Widget build(BuildContext context) {
    return const PlaceholderPage(
      title: 'Equipos',
      description: 'Aquí gestionarás equipos y preferencias horarias.',
    );
  }
}