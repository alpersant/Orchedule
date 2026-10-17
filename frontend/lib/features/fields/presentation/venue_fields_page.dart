import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../domain/playing_field.dart';
import 'fields_controller.dart';

class VenueFieldsPage extends ConsumerStatefulWidget {
  final String venueId;
  final String venueName;
  final bool venueActive;
  const VenueFieldsPage({super.key, required this.venueId,
    required this.venueName, required this.venueActive});

  @override
  ConsumerState<VenueFieldsPage> createState() => _VenueFieldsPageState();
}

class _VenueFieldsPageState extends ConsumerState<VenueFieldsPage> {
  bool _dialogOpen = false;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (mounted) { ref.read(fieldsControllerProvider(widget.venueId).notifier).load(); }
    });
  }

  Future<void> _form([PlayingField? field]) async {
    if (_dialogOpen) { return; }
    _dialogOpen = true;
    try {
      final saved = await showDialog<bool>(context: context,
        barrierDismissible: false,
        builder: (_) => _FieldNameDialog(venueId: widget.venueId,
          venueName: widget.venueName, field: field));
      if (!mounted || saved != true) { return; }
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(
        content: Text(field == null ? 'Campo añadido a la instalación.' : 'Nombre actualizado.')));
    } finally { _dialogOpen = false; }
  }

  Future<void> _toggle(PlayingField field) async {
    if (_dialogOpen) { return; }
    _dialogOpen = true;
    try {
      final confirmed = await showDialog<bool>(context: context,
        builder: (ctx) => AlertDialog(
          title: Text(field.active ? 'Desactivar campo' : 'Activar campo'),
          content: Text(field.active
            ? '«${field.name}» dejará de estar disponible para nuevas generaciones. '
              'Esto no cancela ni modifica las asignaciones ya generadas.'
            : '¿Quieres activar «${field.name}»? Después podrás seleccionarlo en la disponibilidad semanal.'),
          actions: [
            TextButton(onPressed: () => Navigator.pop(ctx, false), child: const Text('Cancelar')),
            FilledButton(onPressed: () => Navigator.pop(ctx, true),
              child: Text(field.active ? 'Desactivar' : 'Activar')),
          ],
        ));
      if (!mounted || confirmed != true) { return; }
      final ok = await ref.read(fieldsControllerProvider(widget.venueId).notifier)
        .setActive(field, !field.active);
      if (!mounted || !ok) { return; }
      ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Estado actualizado.')));
    } finally { _dialogOpen = false; }
  }

  @override
  Widget build(BuildContext context) {
    final state = ref.watch(fieldsControllerProvider(widget.venueId));
    return Scaffold(
      appBar: AppBar(title: const Text('Campos de la instalación'), actions: [
        IconButton(tooltip: 'Actualizar', onPressed: state.busy ? null
          : () => ref.read(fieldsControllerProvider(widget.venueId).notifier).load(),
          icon: const Icon(Icons.refresh)),
      ]),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: state.busy || !widget.venueActive ? null : () => _form(),
        icon: const Icon(Icons.add), label: const Text('Añadir campo')),
      body: Align(alignment: Alignment.topCenter,
        child: ConstrainedBox(constraints: const BoxConstraints(maxWidth: 960),
          child: Column(crossAxisAlignment: CrossAxisAlignment.stretch, children: [
            Padding(padding: const EdgeInsets.all(20), child: Column(
              crossAxisAlignment: CrossAxisAlignment.start, children: [
                Text(widget.venueName, style: Theme.of(context).textTheme.headlineSmall),
                const SizedBox(height: 8),
                const Text('Cada campo es un espacio de juego independiente. '
                  'Después podrás elegir cuáles se utilizan en cada semana de la temporada.'),
                const SizedBox(height: 8),
                Text('${state.items.length} campos · ${state.items.where((f) => f.active).length} activos'),
                if (!widget.venueActive) const Padding(padding: EdgeInsets.only(top: 8),
                  child: Text('Esta instalación está inactiva. Actívala para añadir o activar campos.')),
              ])),
            if (state.busy) const LinearProgressIndicator(),
            if (state.error != null) MaterialBanner(content: Text(state.error!), actions: [
              TextButton(onPressed: state.busy ? null : () =>
                ref.read(fieldsControllerProvider(widget.venueId).notifier).load(),
                child: const Text('Actualizar')),
            ]),
            Expanded(child: state.items.isEmpty
              ? Center(child: Padding(padding: const EdgeInsets.all(24), child: Column(
                mainAxisSize: MainAxisSize.min, children: [
                  const Icon(Icons.sports_soccer_outlined, size: 48),
                  const SizedBox(height: 16),
                  Text(state.busy ? 'Cargando campos…' : state.error != null
                    ? 'No se pudo cargar la lista.' : 'Esta instalación todavía no tiene campos.'),
                  if (!state.busy && state.error == null) ...[
                    const SizedBox(height: 12),
                    FilledButton.icon(onPressed: widget.venueActive ? () => _form() : null,
                      icon: const Icon(Icons.add), label: const Text('Añadir primer campo')),
                  ],
                ])))
              : ListView.separated(padding: const EdgeInsets.fromLTRB(20, 8, 20, 100),
                itemCount: state.items.length,
                separatorBuilder: (_, __) => const SizedBox(height: 8),
                itemBuilder: (context, index) {
                  final field = state.items[index];
                  return Card(child: ListTile(
                    leading: Icon(field.active ? Icons.sports_soccer : Icons.pause_circle_outline),
                    title: Text(field.name),
                    subtitle: Text(field.active ? 'Activo' : 'Inactivo'),
                    onTap: state.busy ? null : () => _form(field),
                    trailing: PopupMenuButton<String>(enabled: !state.busy,
                      tooltip: 'Acciones del campo',
                      onSelected: (action) {
                        if (action == 'edit') { _form(field); }
                        if (action == 'toggle') { _toggle(field); }
                      },
                      itemBuilder: (_) => [
                        const PopupMenuItem(value: 'edit', child: Text('Cambiar nombre')),
                        PopupMenuItem(value: 'toggle', enabled: field.active || widget.venueActive,
                          child: Text(field.active ? 'Desactivar' : 'Activar')),
                      ]),
                  ));
                })),
          ]))),
    );
  }
}

class _FieldNameDialog extends ConsumerStatefulWidget {
  final String venueId;
  final String venueName;
  final PlayingField? field;
  const _FieldNameDialog({required this.venueId, required this.venueName, this.field});
  @override
  ConsumerState<_FieldNameDialog> createState() => _FieldNameDialogState();
}

class _FieldNameDialogState extends ConsumerState<_FieldNameDialog> {
  final _key = GlobalKey<FormState>();
  late final TextEditingController _name;
  bool _saving = false;
  String? _error;
  @override
  void initState() { super.initState(); _name = TextEditingController(text: widget.field?.name ?? ''); }
  @override
  void dispose() { _name.dispose(); super.dispose(); }

  Future<void> _save() async {
    if (_saving || !_key.currentState!.validate()) { return; }
    setState(() { _saving = true; _error = null; });
    final ok = await ref.read(fieldsControllerProvider(widget.venueId).notifier)
      .save(_name.text, id: widget.field?.id);
    if (!mounted) { return; }
    if (ok) { Navigator.of(context).pop(true); return; }
    setState(() {
      _saving = false;
      _error = ref.read(fieldsControllerProvider(widget.venueId)).error ?? 'No se pudo guardar.';
    });
  }

  @override
  Widget build(BuildContext context) => PopScope(canPop: !_saving,
    child: AlertDialog(
      title: Text(widget.field == null ? 'Añadir campo' : 'Cambiar nombre'),
      content: SizedBox(width: 440, child: Form(key: _key, child: Column(
        mainAxisSize: MainAxisSize.min, crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text('Instalación: ${widget.venueName}'),
          const SizedBox(height: 16),
          TextFormField(controller: _name, autofocus: true, enabled: !_saving,
            maxLength: 100, textInputAction: TextInputAction.done,
            decoration: const InputDecoration(labelText: 'Nombre del campo',
              hintText: 'Por ejemplo: Campo principal', border: OutlineInputBorder()),
            validator: (value) => (value ?? '').trim().isEmpty ? 'Escribe el nombre del campo.'
              : (value ?? '').trim().length > 100 ? 'Máximo 100 caracteres.' : null,
            onFieldSubmitted: (_) => _save()),
          if (_error != null) Text(_error!, style: TextStyle(color: Theme.of(context).colorScheme.error)),
          if (_saving) const LinearProgressIndicator(),
        ]))),
      actions: [
        TextButton(onPressed: _saving ? null : () => Navigator.of(context).pop(false),
          child: const Text('Cancelar')),
        FilledButton(onPressed: _saving ? null : _save,
          child: Text(_saving ? 'Guardando…' : 'Guardar campo')),
      ],
    ));
}
