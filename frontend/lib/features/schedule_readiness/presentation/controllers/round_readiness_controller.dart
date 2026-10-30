import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../di/round_readiness_providers.dart';
import '../../domain/entities/round_readiness.dart';

class RoundReadinessState {
  final bool loading;
  final String? errorMessage;
  final RoundReadiness? readiness;

  const RoundReadinessState({
    this.loading = false,
    this.errorMessage,
    this.readiness,
  });

  RoundReadinessState copyWith({
    bool? loading,
    String? errorMessage,
    RoundReadiness? readiness,
    bool clearError = false,
  }) {
    return RoundReadinessState(
      loading: loading ?? this.loading,
      errorMessage: clearError ? null : errorMessage ?? this.errorMessage,
      readiness: readiness ?? this.readiness,
    );
  }
}

class RoundReadinessController
    extends AutoDisposeFamilyNotifier<RoundReadinessState, String> {
  bool _alive = true;
  int? _loadedWeek;

  String get seasonId => arg;

  @override
  RoundReadinessState build(String seasonId) {
    ref.onDispose(() => _alive = false);
    return const RoundReadinessState();
  }

  Future<void> loadWeek(int weekNumber) async {
    if (!_alive || state.loading) return;

    state = state.copyWith(
      loading: true,
      clearError: true,
      readiness: null,
    );

    try {
      final readiness = await ref.read(getRoundReadinessProvider)(
        seasonId: seasonId,
        weekNumber: weekNumber,
      );

      if (!_alive) return;

      _loadedWeek = weekNumber;
      state = state.copyWith(
        loading: false,
        readiness: readiness,
      );
    } catch (error) {
      if (!_alive) return;

      state = state.copyWith(
        loading: false,
        errorMessage: _message(error),
      );
    }
  }

  String _message(Object error) {
    if (error is DioException) {
      switch (error.response?.statusCode) {
        case 401:
          return 'La sesión ha caducado. Vuelve a iniciar sesión.';
        case 403:
          return 'No tienes permisos para consultar esta jornada.';
        case 404:
          return 'No se encuentra la temporada o la semana solicitada.';
        case 400:
        case 422:
          return 'La semana solicitada no es válida.';
      }

      if (error.response == null) {
        return 'No se pudo contactar con el servidor. Comprueba la conexión.';
      }
    }

    return 'No se pudo validar la preparación de la jornada.';
  }
}

final roundReadinessControllerProvider = NotifierProvider.autoDispose
    .family<RoundReadinessController, RoundReadinessState, String>(
  RoundReadinessController.new,
);