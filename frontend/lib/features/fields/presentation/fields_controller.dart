import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../app/providers.dart';
import '../data/fields_repository_impl.dart';
import '../domain/fields_repository.dart';
import '../domain/playing_field.dart';

final fieldsRepositoryProvider = Provider<FieldsRepository>((ref) {
  return FieldsRepositoryImpl(ref.watch(dioProvider));
});

class FieldsState {
  final bool busy;
  final bool loaded;
  final List<PlayingField> items;
  final String? error;
  const FieldsState({this.busy = false, this.loaded = false,
    this.items = const [], this.error});
}

class FieldsController extends StateNotifier<FieldsState> {
  final FieldsRepository _repository;
  final String venueId;
  bool _disposed = false;

  FieldsController(this._repository, this.venueId) : super(const FieldsState());

  @override
  void dispose() {
    _disposed = true;
    super.dispose();
  }

  Future<void> load() async {
    if (state.busy) { return; }
    state = FieldsState(busy: true, loaded: state.loaded, items: state.items);
    try {
      final items = await _repository.list(venueId);
      if (_disposed) { return; }
      state = FieldsState(loaded: true, items: items);
    } catch (error) {
      if (_disposed) { return; }
      state = FieldsState(loaded: state.loaded, items: state.items,
        error: _message(error));
    }
  }

  Future<bool> save(String name, {String? id}) async {
    final normalized = name.trim();
    if (state.busy) { return false; }
    if (normalized.isEmpty || normalized.length > 100) {
      state = FieldsState(loaded: state.loaded, items: state.items,
        error: 'El nombre debe tener entre 1 y 100 caracteres.');
      return false;
    }
    return _mutate(() => id == null
      ? _repository.create(venueId, normalized)
      : _repository.rename(id, normalized));
  }

  Future<bool> setActive(PlayingField field, bool active) =>
    _mutate(() => _repository.setActive(field.id, active));

  Future<bool> _mutate(Future<void> Function() operation) async {
    if (state.busy) { return false; }
    state = FieldsState(busy: true, loaded: state.loaded, items: state.items);
    try {
      await operation();
    } catch (error) {
      if (_disposed) { return false; }
      state = FieldsState(loaded: state.loaded, items: state.items,
        error: _message(error));
      return false;
    }
    if (_disposed) { return true; }
    try {
      final items = await _repository.list(venueId);
      if (!_disposed) { state = FieldsState(loaded: true, items: items); }
    } catch (_) {
      if (!_disposed) {
        state = FieldsState(loaded: state.loaded, items: state.items,
          error: 'El cambio se guardó, pero no se pudo actualizar la lista. Pulsa Actualizar.');
      }
    }
    return true;
  }

  String _message(Object error) {
    if (error is DioException) {
      switch (error.response?.statusCode) {
        case 401: return 'La sesión ha caducado. Vuelve a iniciar sesión.';
        case 403: return 'No tienes permisos para realizar esta operación.';
        case 404: return 'No se encuentra el recurso solicitado.';
        case 409: return 'Hay un conflicto con los datos guardados. Actualiza la lista.';
        case 400:
        case 422: return 'No se pudo validar el cambio. Revisa el nombre y si ya existe.';
      }
    }
    return 'No se pudo completar la operación. Comprueba la conexión y reintenta.';
  }
}

final fieldsControllerProvider = StateNotifierProvider.autoDispose
  .family<FieldsController, FieldsState, String>((ref, venueId) {
    return FieldsController(ref.watch(fieldsRepositoryProvider), venueId);
  });
