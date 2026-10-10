import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../domain/venue.dart';
import 'venues_controller.dart';

class VenueFormPage extends ConsumerStatefulWidget {
  const VenueFormPage({
    this.venue,
    super.key,
  });

  final Venue? venue;

  @override
  ConsumerState<VenueFormPage> createState() => _VenueFormPageState();
}

class _VenueFormPageState extends ConsumerState<VenueFormPage> {
  final _formKey = GlobalKey<FormState>();

  late final TextEditingController _nameController;
  late final TextEditingController _cityController;
  late final TextEditingController _addressController;
  late final TextEditingController _capacityController;

  bool _saving = false;

  @override
  void initState() {
    super.initState();

    final venue = widget.venue;

    _nameController = TextEditingController(
      text: venue?.name ?? '',
    );

    _cityController = TextEditingController(
      text: venue?.city ?? '',
    );

    _addressController = TextEditingController(
      text: venue?.address ?? '',
    );

    _capacityController = TextEditingController(
      text: venue?.capacity?.toString() ?? '',
    );
  }

  @override
  void dispose() {
    _nameController.dispose();
    _cityController.dispose();
    _addressController.dispose();
    _capacityController.dispose();
    super.dispose();
  }

  Future<void> _save() async {
    if (!_formKey.currentState!.validate()) {
      return;
    }

    final capacityText = _capacityController.text.trim();

    final capacity = capacityText.isEmpty
        ? null
        : int.tryParse(capacityText);

    if (capacityText.isNotEmpty && capacity == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text(
            'La capacidad debe ser un número entero.',
          ),
        ),
      );

      return;
    }

    setState(() {
      _saving = true;
    });

    final saved = await ref.read(venuesProvider.notifier).save(
      id: widget.venue?.id,
      name: _nameController.text.trim(),
      city: _cityController.text.trim(),
      address: _addressController.text.trim().isEmpty
          ? null
          : _addressController.text.trim(),
      capacity: capacity,
      active: widget.venue?.active ?? true,
    );

    if (!mounted) {
      return;
    }

    setState(() {
      _saving = false;
    });

    if (saved) {
      Navigator.of(context).pop(true);
      return;
    }

    final error = ref.read(venuesProvider).error;

    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        backgroundColor: Theme.of(context).colorScheme.error,
        content: Text(
          error ?? 'No se ha podido guardar el campo.',
        ),
        duration: const Duration(seconds: 8),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final isEditing = widget.venue != null;

    return Scaffold(
      appBar: AppBar(
        title: Text(
          isEditing ? 'Editar campo' : 'Nuevo campo',
        ),
      ),
      body: Center(
        child: ConstrainedBox(
          constraints: const BoxConstraints(maxWidth: 600),
          child: Form(
            key: _formKey,
            child: ListView(
              padding: const EdgeInsets.all(24),
              children: [
                TextFormField(
                  controller: _nameController,
                  textInputAction: TextInputAction.next,
                  maxLength: 120,
                  decoration: const InputDecoration(
                    labelText: 'Nombre',
                    hintText: 'Campo Municipal Norte',
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
                ),
                const SizedBox(height: 16),
                TextFormField(
                  controller: _cityController,
                  textInputAction: TextInputAction.next,
                  maxLength: 100,
                  decoration: const InputDecoration(
                    labelText: 'Ciudad',
                    hintText: 'Cangas do Morrazo',
                    border: OutlineInputBorder(),
                  ),
                  validator: (value) {
                    if (value == null || value.trim().isEmpty) {
                      return 'La ciudad es obligatoria.';
                    }

                    if (value.trim().length > 100) {
                      return 'Máximo 100 caracteres.';
                    }

                    return null;
                  },
                ),
                const SizedBox(height: 16),
                TextFormField(
                  controller: _addressController,
                  textInputAction: TextInputAction.next,
                  maxLength: 255,
                  decoration: const InputDecoration(
                    labelText: 'Dirección',
                    hintText: 'Avenida de Galicia 10',
                    border: OutlineInputBorder(),
                  ),
                  validator: (value) {
                    if (value != null && value.length > 255) {
                      return 'Máximo 255 caracteres.';
                    }

                    return null;
                  },
                ),
                const SizedBox(height: 16),
                TextFormField(
                  controller: _capacityController,
                  keyboardType: TextInputType.number,
                  decoration: const InputDecoration(
                    labelText: 'Capacidad',
                    hintText: '500',
                    border: OutlineInputBorder(),
                  ),
                  validator: (value) {
                    if (value == null || value.trim().isEmpty) {
                      return null;
                    }

                    final parsed = int.tryParse(value.trim());

                    if (parsed == null) {
                      return 'Introduce un número entero.';
                    }

                    if (parsed < 0) {
                      return 'La capacidad no puede ser negativa.';
                    }

                    return null;
                  },
                ),
                const SizedBox(height: 24),
                FilledButton.icon(
                  onPressed: _saving ? null : _save,
                  icon: _saving
                      ? const SizedBox(
                    width: 18,
                    height: 18,
                    child: CircularProgressIndicator(
                      strokeWidth: 2,
                    ),
                  )
                      : const Icon(Icons.save_outlined),
                  label: Text(
                    _saving ? 'Guardando...' : 'Guardar',
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}