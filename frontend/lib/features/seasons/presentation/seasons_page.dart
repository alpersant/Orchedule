import 'package:flutter/material.dart';

import '../../../core/widgets/placeholder_page.dart';

class SeasonsPage extends StatelessWidget {
  const SeasonsPage({super.key});

  @override
  Widget build(BuildContext context) {
    return const PlaceholderPage(
      title: 'Temporadas',
      description: 'Aquí configurarás jornadas y fechas de temporada.',
    );
  }
}