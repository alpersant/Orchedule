import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../app/providers.dart';
import '../data/seasons_api.dart';
import '../domain/season.dart';
import '../domain/season_repository.dart';
final seasonsRepositoryProvider = Provider<SeasonRepository>((ref) => SeasonsApi(ref.watch(dioProvider)));
final seasonsProvider = StateNotifierProvider.autoDispose<SeasonsController,
SeasonsState>((ref) {
  final controller = SeasonsController(ref.watch(seasonsRepositoryProvider));
  controller.load();
  return controller;
});
class SeasonsState {
  const SeasonsState({
    this.items = const [
    ],
    this.selected = const {
    },
    this.loading = false,
    this.saving = false,
    this.error,
    this.page = 0,
    this.totalPages = 0,
    this.total = 0
  });
  final List<Season> items;
  final Set<String> selected;
  final bool loading;
  final bool saving;
  final String? error;
  final int page;
  final int totalPages;
  final int total;
}
class SeasonsController extends StateNotifier<SeasonsState> {
  SeasonsController(this.repository) : super(const SeasonsState());
  final SeasonRepository repository;
  String search = '';
  CompetitionOption? competition;
  SeasonStatus? status;
  int _request = 0;
  bool _alive = true;
  void _emit({
    List<Season>? items,
    Set<String>? selected,
    bool? loading,
    bool? saving,
    String? error,
    int? page,
    int? totalPages,
    int? total
  }) {
    if (!_alive) return;
    state = SeasonsState(items: items ?? state.items,
    selected: selected ?? state.selected,
    loading: loading ?? state.loading,
    saving: saving ?? state.saving,
    error: error,
    page: page ?? state.page,
    totalPages: totalPages ?? state.totalPages,
    total: total ?? state.total);
  }
  Future<void> load({
    int? page
  }) async {
    final request = ++_request;
    _emit(loading:true);
    try {
      var result = await repository.search(page:page ?? state.page,
      search:search,
      competitionId:competition?.id,
      status:status);
      if (!_alive || request != _request) return;
      if (result.items.isEmpty && result.page > 0) {
        result = await repository.search(page: result.totalPages > 0 ? result.totalPages-1 : 0,
        search:search,
        competitionId:competition?.id,
        status:status);
      }
      if (!_alive || request != _request) return;
      _emit(items:result.items,
      page:result.page,
      totalPages:result.totalPages,
      total:result.totalElements,
      selected:state.selected.where((id)=>result.items.any((s)=>s.id==id)).toSet(),
      loading:false);
    } catch (e) {
      if (_alive && request == _request) _emit(loading:false,
      error:message(e));
    }
  }
  void toggle(String id) {
    if (state.saving || state.loading) return;
    final selected = {
      ...state.selected
    };
    selected.contains(id) ? selected.remove(id) : selected.add(id);
    _emit(selected:selected);
  }
  void selectPage(bool selected) {
    if (!state.saving && !state.loading) _emit(selected:selected ? state.items.map((s)=>s.id).toSet() : <String>{
    });
  }
  Future<bool> save(SeasonInput input,
  {
    String? id
  }) => _mutate(() async {
    if (id == null) {
      await repository.create(input);
    } else {
      await repository.update(id,
      input);
    }
  });
  Future<bool> activate(String id) => _mutate(() async {
    await repository.activate(id);
  });
  Future<bool> close(String id) => _mutate(() async {
    await repository.close(id);
  });
  Future<bool> delete(String id) => _mutate(() => repository.delete(id));
  Future<bool> _mutate(Future<void> Function() action) async {
    if (state.saving || state.loading) return false;
    _emit(saving:true);
    try {
      await action();
      await load();
      return true;
    }
    catch (e) {
      _emit(error:message(e));
      return false;
    }
    finally {
      if (_alive) _emit(saving:false,
      error:state.error);
    }
  }
  Future<DeleteSeasonsResult?> deleteMany(List<String> ids) async {
    if (state.saving || state.loading || ids.isEmpty || ids.length > 100) return null;
    _emit(saving:true);
    try {
      final result = await repository.deleteMany(ids);
      _emit(selected:result.failures.map((f)=>f.seasonId).toSet());
      await load();
      return result;
    } catch(e) {
      _emit(error:message(e));
      return null;
    }
    finally {
      if (_alive) _emit(saving:false,
      error:state.error);
    }
  }
  static String message(Object error) {
    if (error is DioException) {
      final status = error.response?.statusCode;
      if (status == 401) return 'La sesión no es válida. Inicia sesión de nuevo.';
      if (status == 403) return 'No se ha autorizado la operación. Comprueba tu sesión y permisos.';
      if (status == 404) return 'El recurso ya no existe. Actualiza el listado.';
      if (status == 409) return 'Conflicto: nombre repetido o dependencias que impiden la operación.';
      if (status == 400 || status == 422) return 'Revisa los datos y las reglas de la temporada.';
      if (status == null) return 'No se pudo contactar con el servidor.';
    }
    return 'No se pudo completar la operación. Inténtalo de nuevo.';
  }
  @override void dispose() {
    _alive=false;
    ++_request;
    super.dispose();
  }
}
