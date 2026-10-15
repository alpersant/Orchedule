import 'package:dio/dio.dart';
import '../domain/season.dart';
import '../domain/season_repository.dart';
class SeasonsApi implements SeasonRepository {
  SeasonsApi(this._dio);
  final Dio _dio;
  // Shared application's baseUrl already ends with /api.
  static const path = '/v1/seasons';
  Season _season(Map<String,
  dynamic> json) => Season(
  id: json[
    'id'
  ] as String,
  competitionId: json[
    'competitionId'
  ] as String?,
  name: json[
    'name'
  ] as String,
  startDate: DateTime.parse(json[
    'startDate'
  ] as String),
  endDate: DateTime.parse(json[
    'endDate'
  ] as String),
  status: SeasonStatus.values.firstWhere((s) => s.name.toUpperCase() == json[
    'status'
  ]),
  active: json[
    'active'
  ] as bool,
  );
  @override
  Future<SeasonPage> search({
    required int page,
    String? competitionId,
    String? search,
    SeasonStatus? status
  }) async {
    final response = await _dio.get<Map<String,
    dynamic>>('$path/search',
    queryParameters: {
      'page': page,
      'size': 20,
      if (competitionId != null) 'competitionId': competitionId,
      if (search != null && search.trim().isNotEmpty) 'search': search.trim(),
      if (status != null) 'status': status.name.toUpperCase(),
    });
    final json = response.data!;
    return SeasonPage((json[
      'content'
    ] as List<dynamic>).map((e) => _season(e as Map<String,
    dynamic>)).toList(),
    json[
      'page'
    ] as int,
    json[
      'totalPages'
    ] as int,
    json[
      'totalElements'
    ] as int);
  }
  Map<String,
  dynamic> _body(SeasonInput input) => {
    'name': input.name.trim(),
    'startDate': dateOnly(input.startDate),
    'endDate': dateOnly(input.endDate),
  };
  @override Future<Season> create(SeasonInput input) async => _season((await _dio.post<Map<String,
  dynamic>>(
  path,
  data: {
    ..._body(input),
    'competitionId': input.competitionId
  })).data!);
  @override Future<Season> update(String id,
  SeasonInput input) async => _season((await _dio.put<Map<String,
  dynamic>>(
  '$path/$id',
  data: {
    ..._body(input),
    'active': input.active
  })).data!);
  @override Future<Season> activate(String id) async => _season((await _dio.patch<Map<String,
  dynamic>>('$path/$id/activate')).data!);
  @override Future<Season> close(String id) async => _season((await _dio.patch<Map<String,
  dynamic>>('$path/$id/close')).data!);
  @override Future<void> delete(String id) async {
    await _dio.delete<void>('$path/$id');
  }
  @override Future<DeleteSeasonsResult> deleteMany(List<String> ids) async {
    final json = (await _dio.post<Map<String,
    dynamic>>('$path/batch-delete',
    data:{
      'seasonIds': ids
    })).data!;
    return DeleteSeasonsResult((json[
      'deletedIds'
    ] as List<dynamic>).cast<String>(),
    (json[
      'failures'
    ] as List<dynamic>).map((e) => SeasonDeleteFailure(e[
      'seasonId'
    ] as String,
    e[
      'reason'
    ] as String)).toList());
  }
  @override Future<CompetitionOptionsPage> competitions({
    int page = 0,
    String search = ''
  }) async {
    final json = (await _dio.get<Map<String,
    dynamic>>('/competitions',
    queryParameters:{
      'page':page,
      'size':20,
      if (search.trim().isNotEmpty) 'search':search.trim(),
    })).data!;
    return CompetitionOptionsPage((json[
      'content'
    ] as List<dynamic>).map((e) => CompetitionOption(e[
      'id'
    ] as String,
    e[
      'name'
    ] as String)).toList(),
    json[
      'page'
    ] as int,
    json[
      'last'
    ] as bool);
  }
}
