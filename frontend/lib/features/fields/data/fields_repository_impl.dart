import 'package:dio/dio.dart';
import '../domain/fields_repository.dart';
import '../domain/playing_field.dart';

/// Paths are relative to the shared base URL, which already includes /api.
class FieldsRepositoryImpl implements FieldsRepository {
  final Dio _dio;
  const FieldsRepositoryImpl(this._dio);

  @override
  Future<List<PlayingField>> list(String venueId) async {
    final response = await _dio.get<List<dynamic>>(
      '/fields', queryParameters: {'venueId': venueId},
    );
    if (response.data == null) {
      throw const FormatException('Expected a field list');
    }
    final fields = response.data!.map((item) => PlayingField.fromJson(
      Map<String, dynamic>.from(item as Map),
    )).toList();
    fields.sort((a, b) => a.name.toLowerCase().compareTo(b.name.toLowerCase()));
    return List.unmodifiable(fields);
  }

  @override
  Future<void> create(String venueId, String name) async {
    await _dio.post<dynamic>('/fields', data: {'venueId': venueId, 'name': name.trim()});
  }

  @override
  Future<void> rename(String id, String name) async {
    await _dio.put<dynamic>('/fields/$id', data: {'name': name.trim()});
  }

  @override
  Future<void> setActive(String id, bool active) async {
    await _dio.post<dynamic>('/fields/$id/${active ? 'activate' : 'deactivate'}');
  }
}
