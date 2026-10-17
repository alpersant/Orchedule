import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:dio/dio.dart';

import '../../di/field_weekly_availability_providers.dart';
import '../../domain/entities/field_weekly_availability.dart';

class FieldWeeklyAvailabilityState {
  final bool loading;
  final String? errorMessage;
  final List<FieldWeeklyAvailability> availability;
  final Set<String> selectedFieldIds;

  const FieldWeeklyAvailabilityState({
    this.loading = false,
    this.errorMessage,
    this.availability = const [],
    this.selectedFieldIds = const {},
  });

  FieldWeeklyAvailabilityState copyWith({
    bool? loading,
    String? errorMessage,
    List<FieldWeeklyAvailability>? availability,
    Set<String>? selectedFieldIds,
    bool clearError = false,
  }) {
    return FieldWeeklyAvailabilityState(
      loading: loading ?? this.loading,
      errorMessage: clearError ? null : errorMessage ?? this.errorMessage,
      availability: availability ?? this.availability,
      selectedFieldIds: selectedFieldIds ?? this.selectedFieldIds,
    );
  }
}

class FieldWeeklyAvailabilityController
    extends AutoDisposeFamilyNotifier<FieldWeeklyAvailabilityState, String> {
  bool _alive = true;
  int? _loadedWeek;

  @override
  FieldWeeklyAvailabilityState build(String seasonId) {
    _alive = true;
    _loadedWeek = null;
    ref.onDispose(() { _alive = false; });
    return const FieldWeeklyAvailabilityState();
  }

  String get _seasonId => arg;

  void toggleField(String fieldId) {
    if (!_alive || state.loading || _loadedWeek == null || state.errorMessage != null || fieldId.trim().isEmpty) {
      return;
    }

    final selectedFieldIds = Set<String>.of(state.selectedFieldIds);

    if (!selectedFieldIds.remove(fieldId)) {
      selectedFieldIds.add(fieldId);
    }

    state = state.copyWith(
      selectedFieldIds: Set<String>.unmodifiable(selectedFieldIds),
    );
  }

  Future<void> loadWeek(int weekNumber) async {
    if (!_alive || state.loading) {
      return;
    }

    _loadedWeek = null;
    state = state.copyWith(
      loading: true,
      clearError: true,
      availability: const [],
      selectedFieldIds: const {},
    );

    try {
      final availability = await ref.read(
        getFieldWeeklyAvailabilityProvider,
      )(
        seasonId: _seasonId,
        weekNumber: weekNumber,
      );

      if (!_alive) { return; }
      _loadedWeek = weekNumber;
      _setLoadedAvailability(availability);
    } catch (error) {
      if (!_alive) { return; }
      state = state.copyWith(
        loading: false,
        errorMessage: _errorMessage(error, 'No se pudo cargar la disponibilidad de la semana.'),
      );
    }
  }

  Future<bool> saveWeek(int weekNumber) async {
    if (!_alive || state.loading || _loadedWeek != weekNumber) {
      return false;
    }

    final enabledFieldIds = state.selectedFieldIds.toList()..sort();

    state = state.copyWith(
      loading: true,
      clearError: true,
    );

    try {
      final availability = await ref.read(
        setFieldWeeklyAvailabilityProvider,
      )(
        seasonId: _seasonId,
        weekNumber: weekNumber,
        enabledFieldIds: enabledFieldIds,
      );

      if (!_alive) { return false; }
      _setLoadedAvailability(availability);

      return true;
    } catch (error) {
      if (!_alive) { return false; }
      state = state.copyWith(
        loading: false,
        errorMessage: _errorMessage(error, 'No se pudo guardar la disponibilidad.'),
      );

      return false;
    }
  }

  Future<bool> applyToWeeks(List<int> weekNumbers) async {
    if (!_alive || state.loading) {
      return false;
    }

    if (weekNumbers.isEmpty ||
        weekNumbers.any((week) => week < 1 || week > 60)) {
      state = state.copyWith(
        errorMessage: 'Selecciona semanas válidas entre la 1 y la 60.',
      );

      return false;
    }

    final targetWeeks = weekNumbers.toSet().toList()..sort();
    final enabledFieldIds = state.selectedFieldIds.toList()..sort();

    state = state.copyWith(
      loading: true,
      clearError: true,
    );

    try {
      await ref.read(
        applyFieldWeeklyAvailabilityProvider,
      )(
        seasonId: _seasonId,
        weekNumbers: targetWeeks,
        enabledFieldIds: enabledFieldIds,
      );

      if (!_alive) { return false; }
      state = state.copyWith(loading: false, clearError: true);
      return true;
    } catch (error) {
      if (!_alive) { return false; }
      state = state.copyWith(
        loading: false,
        errorMessage: _errorMessage(error, 'No se pudo aplicar la configuración a las semanas.'),
      );

      return false;
    }
  }

  Future<bool> clearWeek(int weekNumber) async {
    if (!_alive || state.loading) {
      return false;
    }

    state = state.copyWith(
      loading: true,
      clearError: true,
    );

    try {
      final availability = await ref.read(
        clearFieldWeeklyAvailabilityProvider,
      )(
        seasonId: _seasonId,
        weekNumber: weekNumber,
      );

      if (!_alive) { return false; }
      _setLoadedAvailability(availability);

      return true;
    } catch (error) {
      if (!_alive) { return false; }
      state = state.copyWith(
        loading: false,
        errorMessage: _errorMessage(error, 'No se pudo dejar la semana sin campos disponibles.'),
      );

      return false;
    }
  }

  String _errorMessage(Object error, String fallback) {
    if (error is DioException) {
      switch (error.response?.statusCode) {
        case 401:
          return 'La sesión ha caducado. Vuelve a iniciar sesión.';
        case 403:
          return 'No tienes permisos para realizar esta operación.';
        case 404:
          return 'No se encuentra la temporada o la ruta de disponibilidad.';
        case 409:
          return 'La semana tiene una configuración en conflicto o una jornada generada. Actualiza y revisa el calendario.';
        case 400:
        case 422:
          return 'La semana o los campos seleccionados no son válidos. Actualiza los datos.';
      }
      if (error.response == null) {
        return 'No se pudo contactar con el servidor. Comprueba la conexión.';
      }
    }
    if (error is FormatException || error is TypeError) {
      return 'La respuesta del servidor no coincide con el formato esperado.';
    }
    return fallback;
  }

  void _setLoadedAvailability(
      List<FieldWeeklyAvailability> availability,
      ) {
    final selectedFieldIds = availability
        .where((item) => item.enabled)
        .map((item) => item.fieldId);

    state = state.copyWith(
      loading: false,
      availability: List<FieldWeeklyAvailability>.unmodifiable(
        availability,
      ),
      selectedFieldIds: Set<String>.unmodifiable(
        selectedFieldIds,
      ),
      clearError: true,
    );
  }
}

final fieldWeeklyAvailabilityControllerProvider =
NotifierProvider.autoDispose.family<
    FieldWeeklyAvailabilityController,
    FieldWeeklyAvailabilityState,
    String>(
  FieldWeeklyAvailabilityController.new,
);
