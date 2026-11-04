import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../app/providers.dart';
import '../domain/scheduling_update_models.dart';

final schedulingUpdateApiProvider = Provider<SchedulingUpdateApi>((ref) {
  return SchedulingUpdateApi(ref.watch(dioProvider));
});

class SchedulingUpdateApi {
  final Dio dio;

  SchedulingUpdateApi(this.dio);

  String _season(String seasonId) =>
      'v1/planning/seasons/${Uri.encodeComponent(seasonId)}';

  String _game(String gameId) =>
      'v1/planning/games/${Uri.encodeComponent(gameId)}';

  Future<SeasonRules> seasonRules(String seasonId) async {
    final response = await dio.get<Map<String, dynamic>>(
      '${_season(seasonId)}/rules',
    );
    return SeasonRules.fromJson(response.data!);
  }

  Future<SeasonRules> updateSeasonRules(
    String seasonId,
    int minRestHours,
    int maxMatchesPerWeek,
  ) async {
    final response = await dio.put<Map<String, dynamic>>(
      '${_season(seasonId)}/rules',
      data: {
        'defaultMinRestHours': minRestHours,
        'defaultMaxMatchesPerWeek': maxMatchesPerWeek,
      },
    );
    return SeasonRules.fromJson(response.data!);
  }

  Future<TeamRule> saveTeamRule(
    String seasonId,
    String teamId,
    int minRestHours,
    int maxMatchesPerWeek,
  ) async {
    final response = await dio.put<Map<String, dynamic>>(
      '${_season(seasonId)}/teams/${Uri.encodeComponent(teamId)}/rules',
      data: {
        'minRestHours': minRestHours,
        'maxMatchesPerWeek': maxMatchesPerWeek,
      },
    );
    return TeamRule.fromJson(response.data!);
  }

  Future<void> confirmGame(String gameId) async {
    await dio.patch<void>('${_game(gameId)}/confirm');
  }

  Future<void> unconfirmGame(String gameId) async {
    await dio.patch<void>('${_game(gameId)}/unconfirm');
  }

  Future<List<GameAlternative>> alternatives(String gameId) async {
    final response = await dio.get<List<dynamic>>(
      '${_game(gameId)}/alternatives',
    );
    return (response.data ?? const [])
        .map((item) => GameAlternative.fromJson(item as Map<String, dynamic>))
        .toList(growable: false);
  }

  Future<GameAlternative> createAlternative({
    required String gameId,
    required int alternativeWeek,
    required String proposedDay,
    required String proposedTime,
    String? reason,
  }) async {
    final response = await dio.post<Map<String, dynamic>>(
      '${_game(gameId)}/alternatives',
      data: {
        'alternativeWeek': alternativeWeek,
        'proposedDay': proposedDay,
        'proposedTime': proposedTime,
        'reason': reason,
      },
    );
    return GameAlternative.fromJson(response.data!);
  }

  Future<void> decideAlternative(String alternativeId, bool accept) async {
    await dio.patch<void>(
      'v1/planning/alternatives/${Uri.encodeComponent(alternativeId)}/'
      '${accept ? 'accept' : 'reject'}',
    );
  }

  Future<List<SchedulingHistoryEntry>> history(String seasonId) async {
    final response = await dio.get<List<dynamic>>(
      '${_season(seasonId)}/history',
    );
    return (response.data ?? const [])
        .map((item) =>
            SchedulingHistoryEntry.fromJson(item as Map<String, dynamic>))
        .toList(growable: false);
  }

  static String message(Object error) {
    if (error is DioException) {
      final status = error.response?.statusCode;
      if (status == 401) return 'Tu sesión ha caducado. Vuelve a iniciar sesión.';
      if (status == 403) return 'No tienes permisos para realizar este cambio.';
      if (status == 404) return 'No se encuentra el partido o la temporada.';
      if (status == 409) return 'Hay un conflicto. Actualiza la planificación antes de continuar.';
      final body = error.response?.data;
      if (body is Map) {
        final detail = body['message'] ?? body['detail'];
        if (detail is String) return detail;
      }
    }
    return 'No se pudo completar la operación. Actualiza y reintenta.';
  }
}
