import 'package:flutter/material.dart';

class PaginationBar extends StatelessWidget {
  const PaginationBar({super.key, required this.page, required this.totalPages,
    required this.totalElements, required this.loading, required this.onPage,
    required this.onSearch});
  final int page, totalPages, totalElements;
  final bool loading;
  final ValueChanged<int> onPage;
  final ValueChanged<String> onSearch;
  @override
  Widget build(BuildContext context) => SafeArea(top: false, child: Padding(
    padding: const EdgeInsets.fromLTRB(16, 8, 16, 8),
    child: Column(mainAxisSize: MainAxisSize.min, children: [
      TextField(enabled: !loading, maxLength: 150, textInputAction: TextInputAction.search,
        decoration: const InputDecoration(labelText: 'Buscar por nombre',
          hintText: 'Escribe y pulsa Intro para buscar', counterText: ''),
        onSubmitted: onSearch),
      Row(mainAxisAlignment: MainAxisAlignment.center, children: [
        IconButton(tooltip: 'Página anterior', onPressed: loading || page == 0 ? null : () => onPage(page - 1),
          icon: const Icon(Icons.chevron_left)),
        Flexible(child: Text(totalPages == 0 ? 'Sin resultados' :
          'Página ${page + 1} de $totalPages · $totalElements resultados', textAlign: TextAlign.center)),
        IconButton(tooltip: 'Página siguiente', onPressed: loading || page + 1 >= totalPages ? null : () => onPage(page + 1),
          icon: const Icon(Icons.chevron_right)),
      ]),
    ]),
  ));
}
