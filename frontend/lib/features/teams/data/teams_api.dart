import 'package:dio/dio.dart';

import '../domain/team.dart';

class TeamsApi {
  TeamsApi(this._dio);

  final Dio _dio;
  static const String _path = '/teams';

  Future<List<Team>> getAll() async {
    final response = await _dio.get<List<dynamic>>(_path);
    return (response.data ?? [])
        .map((item) => Team.fromJson(item as Map<String, dynamic>))
        .toList();
  }

  Future<Team> create(String name) async {
    final response = await _dio.post<Map<String, dynamic>>(
      _path,
      data: {'name': name},
      options: Options(contentType: Headers.jsonContentType),
    );
    return Team.fromJson(response.data!);
  }

  Future<Team> update({
    required String id,
    required String name,
    required bool active,
  }) async {
    final response = await _dio.put<Map<String, dynamic>>(
      '$_path/$id',
      data: {'name': name, 'active': active},
      options: Options(contentType: Headers.jsonContentType),
    );
    return Team.fromJson(response.data!);
  }

  Future<void> deleteById(String teamId) {
    return _dio.delete<void>('$_path/$teamId');
  }

  Future<void> deleteBulk(List<String> teamIds) {
    return _dio.delete<void>(
      '$_path/bulk',
      data: {'teamIds': teamIds},
      options: Options(contentType: Headers.jsonContentType),
    );
  }
}
