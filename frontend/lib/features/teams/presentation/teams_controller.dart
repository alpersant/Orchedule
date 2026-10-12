import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../app/providers.dart';
import '../data/teams_api.dart';
import '../domain/team.dart';

final teamsApiProvider = Provider<TeamsApi>((ref) {
  return TeamsApi(ref.watch(dioProvider));
});

final teamsProvider = StateNotifierProvider<TeamsController, TeamsState>((ref) {
  return TeamsController(ref.watch(teamsApiProvider))..load();
});

class TeamsState {
  const TeamsState({
    this.items = const [],
    this.loading = false,
    this.error,
  });

  final List<Team> items;
  final bool loading;
  final String? error;

  TeamsState copyWith({
    List<Team>? items,
    bool? loading,
    String? error,
    bool clearError = false,
  }) {
    return TeamsState(
      items: items ?? this.items,
      loading: loading ?? this.loading,
      error: clearError ? null : (error ?? this.error),
    );
  }
}

class TeamsController extends StateNotifier<TeamsState> {
  TeamsController(this._api) : super(const TeamsState());

  final TeamsApi _api;

  Future<void> load() async {
    state = state.copyWith(loading: true, clearError: true);
    try {
      final teams = await _api.getAll();
      state = state.copyWith(items: teams, loading: false);
    } on DioException catch (error) {
      state = state.copyWith(
        loading: false,
        error: _message(error, 'No se han podido cargar los equipos.'),
      );
    }
  }

  Future<bool> save({
    String? id,
    required String name,
    required bool active,
  }) async {
    try {
      final saved = id == null
          ? await _api.create(name)
          : await _api.update(id: id, name: name, active: active);
      final items = [...state.items];
      final index = items.indexWhere((team) => team.id == saved.id);
      if (index == -1) {
        items.add(saved);
      } else {
        items[index] = saved;
      }
      state = state.copyWith(items: items, clearError: true);
      return true;
    } on DioException catch (error) {
      state = state.copyWith(
        error: _message(error, 'No se ha podido guardar el equipo.'),
      );
      return false;
    }
  }

  Future<bool> setActive(Team team, bool active) {
    return save(id: team.id, name: team.name, active: active);
  }

  Future<bool> deleteById(String teamId) async {
    try {
      await _api.deleteById(teamId);
      state = state.copyWith(
        items: state.items.where((team) => team.id != teamId).toList(),
        clearError: true,
      );
      return true;
    } on DioException catch (error) {
      state = state.copyWith(
        error: _message(error, 'No se ha podido eliminar el equipo.'),
      );
      return false;
    }
  }

  Future<bool> deleteBulk(List<String> teamIds) async {
    if (teamIds.isEmpty) return true;
    try {
      await _api.deleteBulk(teamIds);
      final ids = teamIds.toSet();
      state = state.copyWith(
        items: state.items.where((team) => !ids.contains(team.id)).toList(),
        clearError: true,
      );
      return true;
    } on DioException catch (error) {
      state = state.copyWith(
        error: _message(error, 'No se han podido eliminar los equipos.'),
      );
      return false;
    }
  }

  String _message(DioException error, String fallback) {
    final status = error.response?.statusCode;
    final body = error.response?.data;
    if (body is Map<String, dynamic>) {
      final message = body['message'];
      if (message is String && message.isNotEmpty) return message;
    }
    if (status == 401 || status == 403) return 'No tienes permisos para esta operación.';
    if (status == 404) return 'El equipo ya no existe.';
    if (status == 409) return 'El equipo está en uso y no se puede eliminar.';
    return fallback;
  }
}
