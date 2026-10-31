import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';


import '../../../app/providers.dart';
import '../data/dto/round_readiness_dto.dart';
import '../data/dto/schedule_round_dto.dart';
import '../data/schedule_round_api.dart';

final scheduleRoundApiProvider = Provider<ScheduleRoundApi>((ref) {
  return ScheduleRoundApi(ref.watch(dioProvider));
});

class ScheduleRoundState {
  final bool loading;
  final bool mutating;
  final int weekNumber;
  final RoundReadinessDto? readiness;
  final ScheduleRoundDto? round;
  final String? error;

  const ScheduleRoundState({
    this.loading = false,
    this.mutating = false,
    this.weekNumber = 1,
    this.readiness,
    this.round,
    this.error,
  });

  ScheduleRoundState copyWith({
    bool? loading,
    bool? mutating,
    int? weekNumber,
    RoundReadinessDto? readiness,
    ScheduleRoundDto? round,
    String? error,
    bool clearReadiness = false,
    bool clearRound = false,
    bool clearError = false,
  }) {
    return ScheduleRoundState(
      loading: loading ?? this.loading,
      mutating: mutating ?? this.mutating,
      weekNumber: weekNumber ?? this.weekNumber,
      readiness: clearReadiness ? null : readiness ?? this.readiness,
      round: clearRound ? null : round ?? this.round,
      error: clearError ? null : error ?? this.error,
    );
  }
}

class ScheduleRoundController
    extends AutoDisposeFamilyNotifier<ScheduleRoundState, String> {
  bool alive = true;

  @override
  ScheduleRoundState build(String seasonId) {
    ref.onDispose(() => alive = false);
    return const ScheduleRoundState();
  }

  String get seasonId => arg;

  Future<void> checkReadiness() async {
    if (!alive || state.loading || state.mutating) return;

    state = state.copyWith(
      loading: true,
      clearError: true,
      clearReadiness: true,
    );

    try {
      final readiness = await ref.read(scheduleRoundApiProvider).getReadiness(
            seasonId: seasonId,
            weekNumber: state.weekNumber,
          );

      state = state.copyWith(loading: false, readiness: readiness);
    } on DioException catch (error) {
      state = state.copyWith(
        loading: false,
        error: _message(error, 'No se pudo validar la preparación.'),
      );
    }
  }

  Future<void> generate() async {
    await _mutate(
      action: () => ref.read(scheduleRoundApiProvider).generate(
            seasonId: seasonId,
            weekNumber: state.weekNumber,
          ),
      fallbackError: 'No se pudo generar la jornada.',
      clearReadiness: true,
    );
  }

  Future<void> confirm() async {
    final round = state.round;
    if (round == null) return;

    await _mutate(
      action: () => ref.read(scheduleRoundApiProvider).confirm(round.id),
      fallbackError: 'No se pudo confirmar la jornada.',
    );
  }

  Future<void> cancel() async {
    final round = state.round;
    if (round == null) return;

    await _mutate(
      action: () => ref.read(scheduleRoundApiProvider).cancel(round.id),
      fallbackError: 'No se pudo cancelar la jornada.',
    );
  }

  Future<void> regenerate() async {
    final round = state.round;
    if (round == null) return;

    await _mutate(
      action: () => ref.read(scheduleRoundApiProvider).regenerate(round.id),
      fallbackError: 'No se pudo regenerar la jornada.',
      clearReadiness: true,
    );
  }

  Future<void> _mutate({
    required Future<ScheduleRoundDto> Function() action,
    required String fallbackError,
    bool clearReadiness = false,
  }) async {
    if (!alive || state.loading || state.mutating) return;

    state = state.copyWith(mutating: true, clearError: true);

    try {
      final round = await action();

      state = state.copyWith(
        mutating: false,
        round: round,
        clearReadiness: clearReadiness,
      );

      await checkReadiness();
    } on DioException catch (error) {
      state = state.copyWith(
        mutating: false,
        error: _message(error, fallbackError),
      );
    }
  }

  String _message(DioException error, String fallback) {
    final status = error.response?.statusCode;
    final body = error.response?.data;

    if (body is Map<String, dynamic>) {
      final message = body['message'];
      if (message is String && message.isNotEmpty) return message;
    }

    return switch (status) {
      401 => 'La sesión ha caducado. Inicia sesión de nuevo.',
      403 => 'No tienes permisos para esta operación.',
      404 => 'No se encontró la temporada, la semana o la jornada.',
      409 => 'La jornada ya existe o no permite esta operación.',
      400 || 422 => 'Revisa la preparación de la jornada.',
      null => 'No se pudo contactar con el servidor.',
      _ => fallback,
    };
  }
}

final scheduleRoundControllerProvider = NotifierProvider.autoDispose
    .family<ScheduleRoundController, ScheduleRoundState, String>(
  ScheduleRoundController.new,
);
