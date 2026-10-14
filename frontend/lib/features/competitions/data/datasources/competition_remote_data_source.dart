import 'package:dio/dio.dart';

import '../dto/batch_delete_result.dart';
import '../dto/competition_dto.dart';
import '../dto/paged_response.dart';

abstract interface class CompetitionRemoteDataSource {
  Future<PagedResponse<CompetitionDto>> list({
    int page,
    int size,
    String? status,
    String? search,
  });

  Future<CompetitionDto> create({
    required String name,
    required String description,
  });

  Future<CompetitionDto> update({
    required String id,
    required String name,
    required String description,
  });

  Future<CompetitionDto> updateDefaults({
    required String id,
    required List<String> defaultDays,
    required List<String> defaultHours,
    required int defaultFieldCount,
  });

  Future<CompetitionDto> activate(String id);

  Future<CompetitionDto> close(String id);

  Future<CompetitionDto> duplicate(
      String id,
      String newName,
      );

  Future<void> delete(String id);

  Future<BatchDeleteResult> deleteMany(List<String> ids);
}

class CompetitionRemoteDataSourceImpl
    implements CompetitionRemoteDataSource {
  CompetitionRemoteDataSourceImpl(this._dio);

  final Dio _dio;

  // El baseUrl compartido ya contiene: http://localhost:8080/api
  static const String _basePath = '/competitions';

  @override
  Future<PagedResponse<CompetitionDto>> list({
    int page = 0,
    int size = 20,
    String? status,
    String? search,
  }) async {
    final response = await _dio.get<Map<String, dynamic>>(
      _basePath,
      queryParameters: {
        'page': page,
        'size': size,
        if (status != null) 'status': status,
        if (search != null && search.trim().isNotEmpty)
          'search': search.trim(),
      },
    );

    return PagedResponse.fromJson(
      response.data!,
      CompetitionDto.fromJson,
    );
  }

  @override
  Future<CompetitionDto> create({
    required String name,
    required String description,
  }) {
    return _write(
      _dio.post<Map<String, dynamic>>(
        _basePath,
        data: {
          'name': name,
          'description': description,
        },
      ),
    );
  }

  @override
  Future<CompetitionDto> update({
    required String id,
    required String name,
    required String description,
  }) {
    return _write(
      _dio.put<Map<String, dynamic>>(
        '$_basePath/$id',
        data: {
          'name': name,
          'description': description,
        },
      ),
    );
  }

  @override
  Future<CompetitionDto> updateDefaults({
    required String id,
    required List<String> defaultDays,
    required List<String> defaultHours,
    required int defaultFieldCount,
  }) {
    return _write(
      _dio.put<Map<String, dynamic>>(
        '$_basePath/$id/defaults',
        data: {
          'defaultDays': defaultDays,
          'defaultHours': defaultHours,
          'defaultFieldCount': defaultFieldCount,
        },
      ),
    );
  }

  @override
  Future<CompetitionDto> activate(String id) {
    return _write(
      _dio.post<Map<String, dynamic>>(
        '$_basePath/$id/activate',
      ),
    );
  }

  @override
  Future<CompetitionDto> close(String id) {
    return _write(
      _dio.post<Map<String, dynamic>>(
        '$_basePath/$id/close',
      ),
    );
  }

  @override
  Future<CompetitionDto> duplicate(
      String id,
      String newName,
      ) {
    return _write(
      _dio.post<Map<String, dynamic>>(
        '$_basePath/$id/duplicate',
        data: {
          'newName': newName,
        },
      ),
    );
  }

  @override
  Future<void> delete(String id) async {
    await _dio.delete<void>('$_basePath/$id');
  }

  @override
  Future<BatchDeleteResult> deleteMany(List<String> ids) async {
    final response = await _dio.post<Map<String, dynamic>>(
      '$_basePath/batch-delete',
      data: {
        'competitionIds': ids,
      },
    );

    return BatchDeleteResult.fromJson(response.data!);
  }

  Future<CompetitionDto> _write(
      Future<Response<Map<String, dynamic>>> request,
      ) async {
    final response = await request;

    return CompetitionDto.fromJson(response.data!);
  }
}