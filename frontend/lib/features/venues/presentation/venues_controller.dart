import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../app/providers.dart';
import '../data/venues_api.dart';
import '../domain/venue.dart';

final venuesApiProvider = Provider<VenuesApi>((ref) => VenuesApi(ref.watch(dioProvider)));
final venuesProvider = StateNotifierProvider<VenuesController, VenuesState>((ref) =>
    VenuesController(ref.watch(venuesApiProvider))..load());

class VenuesState {
  const VenuesState({this.items = const [], this.loading = false, this.error,
    this.page = 0, this.totalPages = 0, this.totalElements = 0, this.search = ''});
  final List<Venue> items;
  final bool loading;
  final String? error;
  final int page, totalPages, totalElements;
  final String search;
}

class VenuesController extends StateNotifier<VenuesState> {
  VenuesController(this._api) : super(const VenuesState());
  final VenuesApi _api;
  int _request = 0;
  bool _alive = true;
  bool _writing = false;

  Future<void> load({int? page, String? search}) async {
    if (!_alive || _writing) return;
    final token = ++_request;
    final targetSearch = search ?? state.search;
    final targetPage = page ?? (search != null ? 0 : state.page);
    final previous = state;
    state = VenuesState(items: previous.items, loading: true, page: previous.page,
      totalPages: previous.totalPages, totalElements: previous.totalElements, search: previous.search);
    try {
      final json = await _api.search(page: targetPage, search: targetSearch);
      if (!_alive || token != _request) return;
      final totalPages = json['totalPages'] as int;
      if (targetPage > 0 && targetPage >= totalPages) {
        await load(page: totalPages > 0 ? totalPages - 1 : 0, search: targetSearch);
        return;
      }
      state = VenuesState(
        items: (json['content'] as List).map((e) => Venue.fromJson(Map<String, dynamic>.from(e as Map))).toList(),
        page: json['page'] as int, totalPages: totalPages,
        totalElements: json['totalElements'] as int, search: targetSearch);
    } catch (error) {
      if (_alive && token == _request) state = VenuesState(items: previous.items,
        page: previous.page, totalPages: previous.totalPages, totalElements: previous.totalElements,
        search: previous.search, error: _message(error));
    }
  }

  Future<bool> save({String? id, required String name, required String city, String? address, int? capacity, required bool active}) =>
      _write(() => id == null ? _api.create(name: name, city: city, address: address, capacity: capacity) : _api.update(id: id, name: name, city: city, address: address, capacity: capacity, active: active));

  Future<bool> deleteById(String id) => _write(() => _api.deleteById(id));

  Future<bool> setActive(Venue venue, bool active) async {
    if (venue.id == null) return false;
    return _write(() => active ? _api.activate(venue.id!) : _api.deactivate(venue.id!));
  }

  Future<bool> _write(Future<dynamic> Function() action) async {
    if (!_alive || _writing || state.loading) return false;
    _writing = true;
    ++_request;
    final previous = state;
    state = VenuesState(items: previous.items, loading: true, page: previous.page,
      totalPages: previous.totalPages, totalElements: previous.totalElements, search: previous.search);
    try {
      await action();
    } catch (error) {
      _writing = false;
      if (_alive) state = VenuesState(items: previous.items, page: previous.page,
        totalPages: previous.totalPages, totalElements: previous.totalElements,
        search: previous.search, error: _message(error));
      return false;
    }
    _writing = false;
    if (_alive) await load();
    // A refresh failure must not report that the already committed write failed.
    return true;
  }

  String _message(Object error) {
    if (error is DioException) {
      final body = error.response?.data;
      if (body is Map && (body['message'] ?? body['detail']) is String)
        return (body['message'] ?? body['detail']) as String;
      if (error.response?.statusCode == 401 || error.response?.statusCode == 403)
        return 'No tienes permisos para esta operación.';
      if (error.response?.statusCode == 409) return 'El recurso está en uso o los datos han cambiado.';
    }
    return 'No se pudo completar la operación. Actualiza para comprobar el estado.';
  }

  @override
  void dispose() { _alive = false; ++_request; super.dispose(); }
}
