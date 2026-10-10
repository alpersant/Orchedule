import 'package:dio/dio.dart';
import 'package:flutter/foundation.dart';

import '../domain/venue.dart';

class VenuesApi {
  VenuesApi(this._dio);

  final Dio _dio;

  static const String _path = '/v1/venues';

  static final Options _jsonOptions = Options(
    contentType: Headers.jsonContentType,
  );

  Future<List<Venue>> getAll() async {
    try {
      final response = await _dio.get<List<dynamic>>(
        _path,
      );

      final data = response.data ?? <dynamic>[];

      return data
          .map(
            (item) => Venue.fromJson(
          item as Map<String, dynamic>,
        ),
      )
          .toList();
    } on DioException catch (error) {
      _logError(error);
      rethrow;
    }
  }

  Future<Venue> getById(String id) async {
    final response = await _dio.get<Map<String, dynamic>>(
      '$_path/$id',
    );

    return Venue.fromJson(response.data!);
  }

  Future<Venue> create({
    required String name,
    required String city,
    String? address,
    int? capacity,
  }) async {
    final response = await _dio.post<Map<String, dynamic>>(
      _path,
      data: {
        'name': name,
        'city': city,
        if (address != null && address.trim().isNotEmpty)
          'address': address.trim(),
        if (capacity != null)
          'capacity': capacity,
      },
      options: _jsonOptions,
    );

    return Venue.fromJson(response.data!);
  }

  Future<Venue> update({
    required String id,
    required String name,
    required String city,
    String? address,
    int? capacity,
    required bool active,
  }) async {
    final response = await _dio.put<Map<String, dynamic>>(
      '$_path/$id',
      data: {
        'name': name,
        'city': city,
        if (address != null && address.trim().isNotEmpty)
          'address': address.trim(),
        if (capacity != null)
          'capacity': capacity,
        'active': active,
      },
      options: _jsonOptions,
    );

    return Venue.fromJson(response.data!);
  }

  Future<Venue> activate(String id) async {
    final response = await _dio.patch<Map<String, dynamic>>(
      '$_path/$id/activate',
    );

    return Venue.fromJson(response.data!);
  }

  Future<Venue> deactivate(String id) async {
    final response = await _dio.patch<Map<String, dynamic>>(
      '$_path/$id/deactivate',
    );

    return Venue.fromJson(response.data!);
  }

  Future<void> deleteById(String id) async {
    await _dio.delete<void>(
      '$_path/$id',
    );
  }

  void _logError(DioException error) {
    debugPrint(
      '[VenuesApi] ${error.requestOptions.method} '
          '${error.requestOptions.uri}',
    );
    debugPrint('[VenuesApi] Tipo: ${error.type}');
    debugPrint('[VenuesApi] Detalle: ${error.error}');
    debugPrint(
      '[VenuesApi] Estado: ${error.response?.statusCode}',
    );
    debugPrint(
      '[VenuesApi] Respuesta: ${error.response?.data}',
    );
  }
}