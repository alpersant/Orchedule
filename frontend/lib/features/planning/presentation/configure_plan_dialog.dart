import 'package:flutter/material.dart';
import '../domain/manual_fixtures.dart';

class ConfigurePlanDialog extends StatefulWidget {
  final List<Map<String, dynamic>> teams;
  const ConfigurePlanDialog({super.key, required this.teams});
  @override
  State<ConfigurePlanDialog> createState() => _ConfigurePlanDialogState();
}
class _ConfigurePlanDialogState extends State<ConfigurePlanDialog> {
  final zone = TextEditingController();
  final duration = TextEditingController(text: '60');
  final csv = TextEditingController(text: 'round,homeTeamId,awayTeamId\n');
  final selected = <String>{};
  String mode = 'SINGLE';
  String? home, away, error;
  int round = 1;
  List<Map<String, dynamic>>? preview;
  @override
  void initState() {
    super.initState();
    selected.addAll(widget.teams.map((e) => e['id'] as String));
  }
  @override
  void dispose() { zone.dispose(); duration.dispose(); csv.dispose(); super.dispose(); }
  String name(String id) => widget.teams.firstWhere((t) => t['id'] == id)['name'] as String;
  void validate() {
    try {
      final parsed = ManualFixtures.parse(csv.text, selected);
      setState(() { preview = parsed; error = null; });
    } on FormatException catch (e) {
      setState(() { preview = null; error = e.message; });
    }
  }
  void save() {
    final minutes = int.tryParse(duration.text);
    if (selected.length < 2 || selected.length > 64 || zone.text.trim().isEmpty || minutes == null || minutes < 15 || minutes > 240) {
      setState(() => error = 'Selecciona de 2 a 64 equipos, una zona IANA y una duración de 15 a 240 minutos.');
      return;
    }
    if (mode == 'MANUAL') { validate(); if (preview == null) return; }
    Navigator.pop(context, <String, dynamic>{
      'mode': mode, 'teamIds': selected.toList(), 'zoneId': zone.text.trim(),
      'durationMinutes': minutes, if (mode == 'MANUAL') 'rounds': preview,
    });
  }
  @override
  Widget build(BuildContext context) => AlertDialog(
    title: const Text('Configurar calendario de enfrentamientos'),
    content: SizedBox(width: 720, child: SingleChildScrollView(child: Column(
      crossAxisAlignment: CrossAxisAlignment.stretch, children: [
        const Text('La configuración fija los participantes y rivales de esta temporada. Revisa antes de crear. No transforma ni borra las asignaciones antiguas.'),
        const SizedBox(height: 16),
        DropdownButtonFormField<String>(value: mode, decoration: const InputDecoration(labelText: 'Modalidad'),
          items: const [
            DropdownMenuItem(value: 'SINGLE', child: Text('Liga · una vuelta')),
            DropdownMenuItem(value: 'DOUBLE', child: Text('Liga · ida y vuelta')),
            DropdownMenuItem(value: 'MANUAL', child: Text('Cruces manuales o importados')),
          ], onChanged: (v) => setState(() { mode = v!; preview = null; })),
        const SizedBox(height: 16),
        TextField(controller: zone, decoration: const InputDecoration(labelText: 'Zona horaria IANA', hintText: 'Ejemplo: Europe/Madrid', helperText: 'Las horas de los campos se interpretan en esta zona.')),
        const SizedBox(height: 12),
        TextField(controller: duration, keyboardType: TextInputType.number, decoration: const InputDecoration(labelText: 'Duración del partido en minutos')),
        const SizedBox(height: 16),
        Text('${selected.length} participantes · ${selected.length.isOdd ? 'un descanso por jornada' : 'sin descansos'}'),
        ...widget.teams.map((t) => CheckboxListTile(dense: true, title: Text(t['name'] as String), value: selected.contains(t['id']),
          onChanged: (v) => setState(() {
            if (v == true) { selected.add(t['id'] as String); } else { selected.remove(t['id']); }
            home = null; away = null; preview = null;
          }))),
        if (mode == 'MANUAL') ...[
          const Divider(),
          const Text('Añade cruces por nombre o pega una plantilla CSV. La previsualización identifica los equipos por nombre y calcula los descansos.'),
          const SizedBox(height: 12),
          DropdownButtonFormField<int>(value: round, decoration: const InputDecoration(labelText: 'Jornada del cruce'),
            items: List.generate(126, (i) => DropdownMenuItem(value: i + 1, child: Text('${i + 1}'))),
            onChanged: (v) => setState(() => round = v!)),
          DropdownButtonFormField<String>(value: home, isExpanded: true, decoration: const InputDecoration(labelText: 'Local'),
            items: selected.map((id) => DropdownMenuItem(value: id, child: Text(name(id)))).toList(), onChanged: (v) => setState(() => home = v)),
          DropdownButtonFormField<String>(value: away, isExpanded: true, decoration: const InputDecoration(labelText: 'Visitante'),
            items: selected.map((id) => DropdownMenuItem(value: id, child: Text(name(id)))).toList(), onChanged: (v) => setState(() => away = v)),
          OutlinedButton.icon(onPressed: home == null || away == null || home == away ? null : () {
            csv.text = '${csv.text.trimRight()}\n$round,$home,$away\n';
            setState(() { preview = null; home = null; away = null; });
          }, icon: const Icon(Icons.add), label: const Text('Añadir cruce a la plantilla')),
          TextField(controller: csv, maxLines: 8, decoration: const InputDecoration(labelText: 'Plantilla CSV', border: OutlineInputBorder()), onChanged: (_) => setState(() => preview = null)),
          ExpansionTile(title: const Text('Identificadores para preparar la plantilla'), children: [
            ...widget.teams.where((t) => selected.contains(t['id'])).map((t) => ListTile(title: Text(t['name'] as String), subtitle: SelectableText(t['id'] as String)))
          ]),
          OutlinedButton(onPressed: validate, child: const Text('Validar y previsualizar')),
          if (preview != null) ...List.generate(preview!.length, (i) => Padding(padding: const EdgeInsets.symmetric(vertical: 8), child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
            Text('Jornada ${i + 1}', style: Theme.of(context).textTheme.titleSmall),
            ...(preview![i]['games'] as List).map((g) => Text('${name(g['homeTeamId'] as String)} — ${name(g['awayTeamId'] as String)}')),
            if (preview![i]['byeTeamId'] != null) Text('Descansa: ${name(preview![i]['byeTeamId'] as String)}'),
          ]))),
        ],
        if (error != null) Padding(padding: const EdgeInsets.only(top: 12), child: Text(error!, style: TextStyle(color: Theme.of(context).colorScheme.error))),
      ],
    ))),
    actions: [TextButton(onPressed: () => Navigator.pop(context), child: const Text('Cancelar')),
      FilledButton(onPressed: save, child: const Text('Crear calendario'))],
  );
}
