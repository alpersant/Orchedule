import 'package:dio/dio.dart';

class GameAssignmentApi {
  final Dio dio;
  GameAssignmentApi(this.dio);
  String path(String season,String game)=>'/v1/planning/seasons/${Uri.encodeComponent(season)}/games/${Uri.encodeComponent(game)}/assignment';
  Future<void> save(String season,String game,int version,String localStart,String? fieldId) async {
    await dio.put<dynamic>(path(season,game),data:{'version':version,'localStart':localStart,'fieldId':fieldId});
  }
  Future<List<Map<String,dynamic>>> fields(String season,String game,int version,String localStart) async {
    final response=await dio.get<List<dynamic>>('${path(season,game)}/fields',queryParameters:{'version':version,'localStart':localStart});
    return (response.data??[]).map((e)=>Map<String,dynamic>.from(e as Map)).toList();
  }
  Future<void> clear(String season,String game,int version) async {
    await dio.delete<dynamic>(path(season,game),queryParameters:{'version':version});
  }
  Future<void> confirm(String season,String game,int version,bool confirmed) async {
    await dio.post<dynamic>('${path(season,game)}/${confirmed?'confirm':'unconfirm'}',data:{'version':version});
  }
}
