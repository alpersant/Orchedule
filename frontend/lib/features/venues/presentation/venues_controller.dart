import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../app/providers.dart';
import '../data/venues_api.dart';
import '../domain/venue.dart';

final venuesApiProvider = Provider<VenuesApi>((ref) {
  return VenuesApi(ref.watch(dioProvider));
});

final venuesProvider =
StateNotifierProvider<VenuesController, VenuesState>((ref) {
  return VenuesController(
    ref.watch(venuesApiProvider),
  )..load();
});

class VenuesState {
  const VenuesState({
    this.items = const [],
    this.loading = false,
    this.error,
  });

  final List<Venue> items;
  final bool loading;
  final String? error;

  VenuesState copyWith({
    List<Venue>? items,
    bool? loading,
    String? error,
    bool clearError = false,
  }) {
    return VenuesState(
      items: items ?? this.items,
      loading: loading ?? this.loading,
      error: clearError ? null : (error ?? this.error),
    );
  }
}

class VenuesController extends StateNotifier<VenuesState> {
  VenuesController(this._api) : super(const VenuesState());

  final VenuesApi _api;

  Future<void> load() async {
    state = state.copyWith(
      loading: true,
      clearError: true,
    );

    try {
      final venues = await _api.getAll();

      state = state.copyWith(
        items: venues,
        loading: false,
      );
    } on DioException catch (error, stackTrace) {
      _logDioError(
        operation: 'cargar campos',
        error: error,
        stackTrace: stackTrace,
      );

      state = state.copyWith(
        loading: false,
        error: _getApiErrorMessage(
          error,
          fallback: 'No se han podido cargar los campos.',
        ),
      );
    } catch (error, stackTrace) {
      _logError(
        operation: 'cargar campos',
        error: error,
        stackTrace: stackTrace,
      );

      state = state.copyWith(
        loading: false,
        error: 'No se han podido cargar los campos.',
      );
    }
  }

  Future<bool> save({
    String? id,
    required String name,
    required String city,
    String? address,
    int? capacity,
    required bool active,
  }) async {
    state = state.copyWith(
      loading: true,
      clearError: true,
    );

    try {
      final saved = id == null
          ? await _api.create(
        name: name,
        city: city,
        address: address,
        capacity: capacity,
      )
          : await _api.update(
        id: id,
        name: name,
        city: city,
        address: address,
        capacity: capacity,
        active: active,
      );

      final updatedItems = [...state.items];

      final index = updatedItems.indexWhere(
            (item) => item.id == saved.id,
      );

      if (index == -1) {
        updatedItems.add(saved);
      } else {
        updatedItems[index] = saved;
      }

      state = state.copyWith(
        items: updatedItems,
        loading: false,
        clearError: true,
      );

      return true;
    } on DioException catch (error, stackTrace) {
      _logDioError(
        operation: id == null ? 'crear campo' : 'actualizar campo',
        error: error,
        stackTrace: stackTrace,
      );

      state = state.copyWith(
        loading: false,
        error: _getApiErrorMessage(
          error,
          fallback: 'No se ha podido guardar el campo.',
        ),
      );

      return false;
    } catch (error, stackTrace) {
      _logError(
        operation: id == null ? 'crear campo' : 'actualizar campo',
        error: error,
        stackTrace: stackTrace,
      );

      state = state.copyWith(
        loading: false,
        error: 'No se ha podido guardar el campo.',
      );

      return false;
    }
  }

  Future<bool> setActive(
      Venue venue,
      bool active,
      ) async {
    if (venue.id == null) {
      state = state.copyWith(
        error: 'El campo no tiene identificador.',
      );

      return false;
    }

    try {
      final updatedVenue = active
          ? await _api.activate(venue.id!)
          : await _api.deactivate(venue.id!);

      final updatedItems = state.items.map((item) {
        return item.id == updatedVenue.id ? updatedVenue : item;
      }).toList();

      state = state.copyWith(
        items: updatedItems,
        clearError: true,
      );

      return true;
    } on DioException catch (error, stackTrace) {
      _logDioError(
        operation: active ? 'activar campo' : 'desactivar campo',
        error: error,
        stackTrace: stackTrace,
      );

      state = state.copyWith(
        error: _getApiErrorMessage(
          error,
          fallback: 'No se ha podido cambiar el estado del campo.',
        ),
      );

      return false;
    } catch (error, stackTrace) {
      _logError(
        operation: active ? 'activar campo' : 'desactivar campo',
        error: error,
        stackTrace: stackTrace,
      );

      state = state.copyWith(
        error: 'No se ha podido cambiar el estado del campo.',
      );

      return false;
    }
  }

  Future<bool> deleteById(String id) async {
    if (id.isEmpty) {
      state = state.copyWith(
        error: 'El campo no tiene identificador.',
      );

      return false;
    }

    try {
      await _api.deleteById(id);

      state = state.copyWith(
        items: state.items
            .where((venue) => venue.id != id)
            .toList(),
        clearError: true,
      );

      return true;
    } on DioException catch (error, stackTrace) {
      _logDioError(
        operation: 'eliminar campo',
        error: error,
        stackTrace: stackTrace,
      );

      state = state.copyWith(
        error: _getApiErrorMessage(
          error,
          fallback: 'No se ha podido eliminar el campo.',
        ),
      );

      return false;
    } catch (error, stackTrace) {
      _logError(
        operation: 'eliminar campo',
        error: error,
        stackTrace: stackTrace,
      );

      state = state.copyWith(
        error: 'No se ha podido eliminar el campo.',
      );

      return false;
    }
  }

  String _getApiErrorMessage(
      DioException error, {
        required String fallback,
      }) {
    final statusCode = error.response?.statusCode;
    final data = error.response?.data;

    if (data is Map<String, dynamic>) {
      final message = data['message'] ?? data['error'];

      if (message is String && message.trim().isNotEmpty) {
        return 'Error $statusCode: $message';
      }

      final errors = data['errors'];

      if (errors is List && errors.isNotEmpty) {
        return errors.join(', ');
      }
    }

    if (statusCode == 400) {
      return 'Los datos enviados no son válidos.';
    }

    if (statusCode == 401 || statusCode == 403) {
      return 'La sesión no es válida o no tienes permisos.';
    }

    if (statusCode == 404) {
      return 'El endpoint o el campo no existe.';
    }

    if (statusCode == 409) {
      return 'Ya existe un campo con esos datos.';
    }

    if (error.type == DioExceptionType.connectionError) {
      return 'No se puede conectar con el backend.';
    }

    return fallback;
  }

  void _logDioError({
    required String operation,
    required DioException error,
    required StackTrace stackTrace,
  }) {
    print('[$operation] DioException');
    print('URL: ${error.requestOptions.uri}');
    print('Método: ${error.requestOptions.method}');
    print('Status: ${error.response?.statusCode}');
    print('Respuesta: ${error.response?.data}');
    print('Mensaje: ${error.message}');
    print(stackTrace);
  }

  void _logError({
    required String operation,
    required Object error,
    required StackTrace stackTrace,
  }) {
    print('[$operation] Error: $error');
    print(stackTrace);
  }
}