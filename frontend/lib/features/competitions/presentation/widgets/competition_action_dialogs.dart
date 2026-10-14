import 'package:flutter/material.dart';

import '../../domain/entities/competition.dart';

class CompetitionDetailsResult {
  const CompetitionDetailsResult({
    required this.name,
    required this.description,
  });

  final String name;
  final String description;
}

class CompetitionEditDialog extends StatefulWidget {
  const CompetitionEditDialog({
    super.key,
    required this.competition,
  });

  final Competition competition;

  @override
  State<CompetitionEditDialog> createState() =>
      _CompetitionEditDialogState();
}

class _CompetitionEditDialogState extends State<CompetitionEditDialog> {
  final _formKey = GlobalKey<FormState>();

  late String _name;
  late String _description;

  @override
  void initState() {
    super.initState();
    _name = widget.competition.name;
    _description = widget.competition.description;
  }

  void _submit() {
    if (_formKey.currentState?.validate() != true) {
      return;
    }

    _formKey.currentState!.save();

    Navigator.of(context).pop(
      CompetitionDetailsResult(
        name: _name,
        description: _description,
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      title: const Text('Editar competición'),
      scrollable: true,
      content: SizedBox(
        width: 440,
        child: Form(
          key: _formKey,
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              TextFormField(
                initialValue: _name,
                autofocus: true,
                maxLength: 150,
                decoration: const InputDecoration(
                  labelText: 'Nombre',
                  border: OutlineInputBorder(),
                ),
                validator: (value) {
                  if (value == null || value.trim().isEmpty) {
                    return 'El nombre es obligatorio.';
                  }

                  if (value.trim().length > 150) {
                    return 'Máximo 150 caracteres.';
                  }

                  return null;
                },
                onSaved: (value) {
                  _name = value!.trim();
                },
              ),
              const SizedBox(height: 12),
              TextFormField(
                initialValue: _description,
                maxLength: 1000,
                minLines: 3,
                maxLines: 5,
                decoration: const InputDecoration(
                  labelText: 'Descripción',
                  border: OutlineInputBorder(),
                ),
                validator: (value) {
                  if (value != null && value.length > 1000) {
                    return 'Máximo 1000 caracteres.';
                  }

                  return null;
                },
                onSaved: (value) {
                  _description = value?.trim() ?? '';
                },
              ),
            ],
          ),
        ),
      ),
      actions: [
        TextButton(
          onPressed: () => Navigator.of(context).pop(),
          child: const Text('Cancelar'),
        ),
        FilledButton(
          onPressed: _submit,
          child: const Text('Guardar'),
        ),
      ],
    );
  }
}

class CompetitionDuplicateDialog extends StatefulWidget {
  const CompetitionDuplicateDialog({
    super.key,
    required this.competition,
  });

  final Competition competition;

  @override
  State<CompetitionDuplicateDialog> createState() =>
      _CompetitionDuplicateDialogState();
}

class _CompetitionDuplicateDialogState
    extends State<CompetitionDuplicateDialog> {
  final _formKey = GlobalKey<FormState>();

  String _name = '';

  void _submit() {
    if (_formKey.currentState?.validate() != true) {
      return;
    }

    _formKey.currentState!.save();
    Navigator.of(context).pop(_name);
  }

  @override
  Widget build(BuildContext context) {
    final suggestedName = '${widget.competition.name} - copia';
    final initialName = suggestedName.length <= 120
        ? suggestedName
        : '${widget.competition.name.substring(0, 112)} - copia';

    return AlertDialog(
      title: const Text('Duplicar competición'),
      scrollable: true,
      content: SizedBox(
        width: 440,
        child: Form(
          key: _formKey,
          child: TextFormField(
            initialValue: initialName,
            autofocus: true,
            maxLength: 120,
            decoration: const InputDecoration(
              labelText: 'Nombre de la copia',
              border: OutlineInputBorder(),
            ),
            validator: (value) {
              if (value == null || value.trim().isEmpty) {
                return 'El nombre es obligatorio.';
              }

              if (value.trim().length > 120) {
                return 'Máximo 120 caracteres.';
              }

              return null;
            },
            onSaved: (value) {
              _name = value!.trim();
            },
          ),
        ),
      ),
      actions: [
        TextButton(
          onPressed: () => Navigator.of(context).pop(),
          child: const Text('Cancelar'),
        ),
        FilledButton(
          onPressed: _submit,
          child: const Text('Duplicar'),
        ),
      ],
    );
  }
}

class CompetitionDeleteDialog extends StatefulWidget {
  const CompetitionDeleteDialog({
    super.key,
    required this.competition,
  });

  final Competition competition;

  @override
  State<CompetitionDeleteDialog> createState() =>
      _CompetitionDeleteDialogState();
}

class _CompetitionDeleteDialogState
    extends State<CompetitionDeleteDialog> {
  final _formKey = GlobalKey<FormState>();

  void _submit() {
    if (_formKey.currentState?.validate() == true) {
      Navigator.of(context).pop(true);
    }
  }

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;

    return AlertDialog(
      title: const Text('Eliminar competición'),
      scrollable: true,
      content: SizedBox(
        width: 440,
        child: Form(
          key: _formKey,
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                'Vas a eliminar "${widget.competition.name}" '
                    'y su configuración propia.',
              ),
              const SizedBox(height: 8),
              const Text(
                'Los equipos y campos se conservarán. '
                    'Esta acción no se puede deshacer.',
              ),
              const SizedBox(height: 16),
              TextFormField(
                autofocus: true,
                decoration: const InputDecoration(
                  labelText: 'Escribe el nombre para confirmar',
                  border: OutlineInputBorder(),
                ),
                validator: (value) {
                  if (value?.trim() != widget.competition.name.trim()) {
                    return 'El nombre no coincide.';
                  }

                  return null;
                },
                onFieldSubmitted: (_) => _submit(),
              ),
            ],
          ),
        ),
      ),
      actions: [
        TextButton(
          onPressed: () => Navigator.of(context).pop(false),
          child: const Text('Cancelar'),
        ),
        FilledButton(
          style: FilledButton.styleFrom(
            backgroundColor: colors.error,
            foregroundColor: colors.onError,
          ),
          onPressed: _submit,
          child: const Text('Eliminar definitivamente'),
        ),
      ],
    );
  }
}