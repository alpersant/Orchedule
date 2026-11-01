import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../../planning/data/planning_api.dart';
import '../../../planning/presentation/configure_plan_dialog.dart';

class ScheduleRoundPage extends ConsumerStatefulWidget {
  final String seasonId;
  const ScheduleRoundPage({super.key, required this.seasonId});
  @override
  ConsumerState<ScheduleRoundPage> createState() => _ScheduleRoundPageState();
}
class _ScheduleRoundPageState extends ConsumerState<ScheduleRoundPage> {
  Map<String, dynamic>? data;
  String? error;
  bool loading = true, busy = false;
  int number = 1, request = 0;
  final search = TextEditingController();
  @override
  void initState() { super.initState(); Future.microtask(load); }
  @override
  void dispose() { request++; search.dispose(); super.dispose(); }
  @override
  void didUpdateWidget(covariant ScheduleRoundPage oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (oldWidget.seasonId != widget.seasonId) { number = 1; data = null; load(); }
  }
  Future<void> load() async {
    if (!mounted) return;
    final current = ++request;
    setState(() { loading = true; error = null; });
    try {
      final result = await ref.read(planningApiProvider).read(widget.seasonId);
      if (!mounted || current != request) return;
      setState(() { data = result; loading = false; });
    } catch (e) {
      if (!mounted || current != request) return;
      setState(() { loading = false; error = PlanningApi.message(e); });
    }
  }
  Future<bool> ask(String title, String message) async =>
    await showDialog<bool>(context: context, builder: (ctx) => AlertDialog(
      title: Text(title), content: Text(message), actions: [
        TextButton(onPressed: () => Navigator.pop(ctx, false), child: const Text('Volver')),
        FilledButton(onPressed: () => Navigator.pop(ctx, true), child: const Text('Continuar')),
      ])) == true;
  Future<void> mutate(Future<void> Function() action) async {
    if (busy || !mounted) return;
    setState(() { busy = true; error = null; });
    try {
      await action();
      if (!mounted) return;
      await load();
      if (mounted && error == null) ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Operación completada.')));
    } catch (e) {
      if (mounted) setState(() => error = PlanningApi.message(e));
    } finally { if (mounted) setState(() => busy = false); }
  }
  Future<void> configure() async {
    final body = await showDialog<Map<String, dynamic>>(context: context, barrierDismissible: false,
      builder: (_) => ConfigurePlanDialog(teams: rows(data!['teams'])));
    if (body != null && mounted) await mutate(() => ref.read(planningApiProvider).configure(widget.seasonId, body));
  }
  Future<void> act(Map<String, dynamic> round, String action) async {
    final messages = {
      'schedule': 'Se asignarán horarios a los enfrentamientos, sin cambiar los rivales.',
      'regenerate': 'Se sustituirán los horarios. Se conservarán los enfrentamientos y el descanso.',
      'confirm': 'Se publicarán los partidos en el calendario. Se comprobará de nuevo la disponibilidad.',
      'cancel': 'Los partidos publicados quedarán cancelados. No se eliminarán los enfrentamientos.',
    };
    if (!await ask('Jornada $number', messages[action]!) || !mounted) return;
    await mutate(() => ref.read(planningApiProvider).action(widget.seasonId, number, action, round['version'] as int));
  }
  String label(String value) => const {
    'UNSCHEDULED': 'Pendiente de horario', 'DRAFT': 'Borrador',
    'CONFIRMED': 'Confirmada', 'CANCELLED': 'Cancelada',
  }[value] ?? value;
  String dayLabel(String day) => const {
    'MONDAY': 'Lunes', 'TUESDAY': 'Martes', 'WEDNESDAY': 'Miércoles',
    'THURSDAY': 'Jueves', 'FRIDAY': 'Viernes', 'SATURDAY': 'Sábado', 'SUNDAY': 'Domingo',
  }[day] ?? day;
  String weekRange(Map<String, dynamic> round) {
    final start = DateTime.parse(data!['startDate'] as String), end = DateTime.parse(data!['endDate'] as String);
    final monday = DateTime(start.year, start.month, start.day - (start.weekday - 1) + ((round['week_number'] as int) - 1) * 7);
    final sunday = DateTime(monday.year, monday.month, monday.day + 6);
    String fmt(DateTime d) => '${d.day.toString().padLeft(2, '0')}/${d.month.toString().padLeft(2, '0')}/${d.year}';
    return '${fmt(monday.isBefore(start) ? start : monday)} — ${fmt(sunday.isAfter(end) ? end : sunday)}';
  }
  String scheduledLabel(String local) {
    final d = DateTime.parse(local);
    const days = ['Lunes','Martes','Miércoles','Jueves','Viernes','Sábado','Domingo'];
    return '${days[d.weekday - 1]} ${d.day}/${d.month}/${d.year} · ${d.hour.toString().padLeft(2, '0')}:${d.minute.toString().padLeft(2, '0')}';
  }
  @override
  Widget build(BuildContext context) {
    final disabled = loading || busy;
    final rounds = data == null ? <Map<String, dynamic>>[] : rows(data!['rounds']);
    final chosen = rounds.where((r) => r['round_number'] == number).toList();
    final round = chosen.isEmpty ? null : chosen.first;
    final canManage = data?['canManage'] == true;
    final config = data == null ? <Map<String, dynamic>>[] : rows(data!['configuration']);
    final zone = config.isEmpty ? '' : config.first['zone_id'] as String;
    final games = data == null ? <Map<String, dynamic>>[] : rows(data!['games']).where((g) => g['round_number'] == number &&
      '${g['home_name']} ${g['away_name']} ${g['field_name']}'.toLowerCase().contains(search.text.toLowerCase())).toList();
    return PopScope(canPop: !busy, child: Scaffold(
      appBar: AppBar(
        leading: IconButton(tooltip: 'Volver a temporadas', icon: const Icon(Icons.arrow_back),
          onPressed: busy ? null : () { if (context.canPop()) { context.pop(); } else { context.go('/seasons'); } }),
        title: Text(data?['seasonName'] as String? ?? 'Planificación de jornadas'),
        actions: [IconButton(tooltip: 'Actualizar', onPressed: disabled ? null : load, icon: const Icon(Icons.refresh))],
      ),
      body: Align(alignment: Alignment.topCenter, child: ConstrainedBox(constraints: const BoxConstraints(maxWidth: 1150),
        child: ListView(padding: const EdgeInsets.all(24), children: [
          if (loading || busy) const LinearProgressIndicator(),
          if (error != null) Card(color: Theme.of(context).colorScheme.errorContainer, child: Padding(
            padding: const EdgeInsets.all(16), child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
              Text(error!), TextButton(onPressed: disabled ? null : load, child: const Text('Actualizar y reintentar')),
            ]))),
          if (data != null) ...[
            if (!canManage) const Padding(padding: EdgeInsets.all(8), child: Text('Vista de consulta. Las acciones requieren ADMIN u ORGANIZER.')),
            if (config.isEmpty) Card(child: Padding(padding: const EdgeInsets.all(24), child: Column(
              crossAxisAlignment: CrossAxisAlignment.start, children: [
                Text('Crea el calendario de partidos', style: Theme.of(context).textTheme.headlineSmall),
                const SizedBox(height: 12),
                const Text('Selecciona participantes y modalidad. Con equipos impares descansará uno por jornada.'),
                const SizedBox(height: 16),
                FilledButton.icon(onPressed: disabled || !canManage ? null : configure,
                  icon: const Icon(Icons.event_available), label: const Text('Configurar competición')),
              ]))),
            if (rounds.isNotEmpty) ...[
              Row(children: [
                IconButton(tooltip: 'Jornada anterior', onPressed: disabled || number <= 1 ? null : () => setState(() => number--), icon: const Icon(Icons.chevron_left)),
                Expanded(child: DropdownButtonFormField<int>(key: ValueKey(number), value: number,
                  decoration: const InputDecoration(labelText: 'Jornada', border: OutlineInputBorder()),
                  items: rounds.map((r) => DropdownMenuItem(value: r['round_number'] as int,
                    child: Text('Jornada ${r['round_number']} · ${label(r['status'] as String)}'))).toList(),
                  onChanged: disabled ? null : (v) => setState(() => number = v!))),
                IconButton(tooltip: 'Jornada siguiente', onPressed: disabled || number >= rounds.length ? null : () => setState(() => number++), icon: const Icon(Icons.chevron_right)),
              ]),
              if (round != null) ...[
                const SizedBox(height: 20),
                Wrap(spacing: 16, runSpacing: 12, crossAxisAlignment: WrapCrossAlignment.center, children: [
                  Chip(label: Text(label(round['status'] as String))),
                  Text('Semana ${round['week_number']} · ${weekRange(round)}'), Text('Zona: $zone'),
                ]),
                if (round['bye_name'] != null) Card(child: ListTile(
                  leading: const Icon(Icons.hotel_outlined), title: Text('Descansa: ${round['bye_name']}'),
                  subtitle: const Text('No tiene partido ni reserva de campo en esta jornada.'))),
                const SizedBox(height: 12),
                Wrap(spacing: 12, runSpacing: 8, children: [
                  if (round['status'] == 'UNSCHEDULED') FilledButton.icon(onPressed: disabled || !canManage ? null : () => act(round, 'schedule'), icon: const Icon(Icons.auto_awesome), label: const Text('Asignar horarios')),
                  if (round['status'] == 'DRAFT') FilledButton.icon(onPressed: disabled || !canManage ? null : () => act(round, 'confirm'), icon: const Icon(Icons.check), label: const Text('Confirmar jornada')),
                  if (round['status'] == 'DRAFT' || round['status'] == 'CANCELLED') OutlinedButton.icon(onPressed: disabled || !canManage ? null : () => act(round, 'regenerate'), icon: const Icon(Icons.refresh), label: const Text('Regenerar horarios')),
                  if (round['status'] != 'CANCELLED') OutlinedButton.icon(onPressed: disabled || !canManage ? null : () => act(round, 'cancel'), icon: const Icon(Icons.cancel_outlined), label: const Text('Cancelar jornada')),
                  OutlinedButton.icon(onPressed: disabled ? null : () => context.push('/seasons/${widget.seasonId}/availability'), icon: const Icon(Icons.stadium_outlined), label: const Text('Disponibilidad de campos')),
                ]),
                const SizedBox(height: 20),
                TextField(controller: search, onChanged: (_) => setState(() {}), decoration: const InputDecoration(
                  labelText: 'Buscar equipo o campo', prefixIcon: Icon(Icons.search), border: OutlineInputBorder())),
                const SizedBox(height: 16),
                if (games.isEmpty) const Padding(padding: EdgeInsets.all(24), child: Text('No hay partidos para este filtro.')),
                ...games.map((g) => Card(child: Padding(padding: const EdgeInsets.all(18), child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start, children: [
                    Text('${g['home_name']}  —  ${g['away_name']}', style: Theme.of(context).textTheme.titleMedium),
                    const SizedBox(height: 10),
                    if (g['local_start'] == null) const Text('Horario pendiente')
                    else Wrap(spacing: 24, runSpacing: 8, children: [
                      Text(scheduledLabel(g['local_start'] as String)), Text('${g['field_name']} · ${g['venue_name']}'),
                    ]),
                  ])))),
              ],
            ],
            if (rows(data!['legacyRounds']).isNotEmpty) ExpansionTile(
              title: const Text('Planificaciones anteriores · sin enfrentamientos'),
              subtitle: const Text('Se conservan. No se convierten en partidos automáticamente.'),
              children: rows(data!['legacyRounds']).map((legacy) => ExpansionTile(
                title: Text('Semana ${legacy['week_number']} · ${label(legacy['status'] as String)}'),
                children: [
                  ...rows(data!['legacyAssignments']).where((a) => a['round_id'] == legacy['id']).map((a) => ListTile(
                    title: Text(a['team_name'] as String? ?? 'Equipo no disponible'),
                    subtitle: Text('${dayLabel(a['day'] as String)} · ${a['hour']} · ${a['field_name']}'))),
                  if (legacy['status'] != 'CANCELLED') TextButton(onPressed: disabled || !canManage ? null : () async {
                    if (!await ask('Cancelar planificación anterior', 'Se cancelarán estas asignaciones antiguas. No se borran ni se inventan rivales.') || !mounted) return;
                    await mutate(() => ref.read(planningApiProvider).cancelLegacy(legacy['id'] as String));
                  }, child: const Text('Cancelar planificación anterior')),
                ],
              )).toList()),
            ExpansionTile(title: const Text('Distribución de horarios · jornadas confirmadas'),
              subtitle: const Text('Recuento y rachas por equipo. No certifica optimalidad global.'),
              children: rows(data!['fairness']).map((a) => ListTile(
                title: Text('${a['teamName']} · ${a['games']} partidos'),
                subtitle: Text('Días: ${a['days']}\nHoras: ${a['hours']}\nRacha máxima de día: ${a['maxDayStreak']} · de hora: ${a['maxHourStreak']}'),
              )).toList()),
            ExpansionTile(title: const Text('Registro de operaciones'), children: rows(data!['audit']).map((a) => ListTile(
              title: Text(a['action'] as String), subtitle: Text('${a['created_at']} · ${a['actor']}'))).toList()),
          ],
        ]),
      )),
    ));
  }
}
