import 'dart:async';
import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../app/providers.dart';
import '../domain/planning_view.dart';
import '../presentation/spanish_weekdays.dart';
final planningApiProvider=Provider((ref)=>PlanningApi(ref.watch(dioProvider)));
class PlanningApi {
  final Dio dio;
  PlanningApi(this.dio);
  String path(String id)=>'/v1/planning/seasons/${Uri.encodeComponent(id)}';
  Future<T?> get<T>(String url,{Map<String,dynamic>? query}) async {
    final token=CancelToken();
    final response=await dio.get<T>(url,queryParameters:query,cancelToken:token,
      options:Options(sendTimeout:const Duration(seconds:5),receiveTimeout:const Duration(seconds:5)))
      .timeout(const Duration(seconds:5),onTimeout:(){token.cancel('read deadline');throw TimeoutException('read deadline');});
    return response.data;
  }
  Future<Map<String,dynamic>> read(String id) async {
    final data=await get<Map<String,dynamic>>(path(id));
    if(data==null)throw const FormatException('Respuesta vacía');
    return localizePlanningWeekdays(data);
  }
  Future<PlanningView> view(String id) async {
    final data=PlanningView.fromJson(await read(id));
    if(data.seasonId!=id)throw const FormatException('La respuesta no corresponde a la temporada solicitada');
    return data;
  }
  Future<void> configure(String id,Map<String,dynamic> body) async {
    final safe=Map<String,dynamic>.from(body)..remove('zoneId')..remove('durationMinutes');
    await dio.post<dynamic>(path(id),data:safe);
  }
  Future<void> action(String id,int number,String action,int version) async {
    await dio.post<dynamic>('${path(id)}/rounds/$number/$action',data:{'version':version});
  }
  Future<void> cancelLegacy(String id) async {await dio.post<dynamic>('/schedules/rounds/${Uri.encodeComponent(id)}/cancel');}
  // Keep opening-hour enums intact: UI labels them, server still receives MONDAY etc.
  Future<List<Map<String,dynamic>>> fields() async=>planningRows(await get<dynamic>('/v1/planning/fields'));
  Future<void> saveOpening(String id,Set<String> days,Set<String> hours) async {
    await dio.put<dynamic>('/v1/planning/fields/${Uri.encodeComponent(id)}/opening',data:{'days':days.toList(),'hours':hours.toList()});
  }
  Future<List<Map<String,dynamic>>> availability(String season,int week) async=>planningRows(await get<dynamic>('/v1/fields/availability',query:{'seasonId':season,'weekNumber':week}));
  Future<void> saveAvailability(String season,int week,Set<String> ids) async {
    await dio.put<dynamic>('/v1/planning/fields/availability',data:{'seasonId':season,'weekNumber':week,'enabledFieldIds':ids.toList()});
  }
  static String message(Object error){
    if(error is TimeoutException)return 'La lectura no ha respondido en 5 segundos. Actualiza y reintenta.';
    if(error is DioException){
      if(error.response?.statusCode==401)return 'La sesión ha caducado. Inicia sesión de nuevo.';
      if(error.response?.statusCode==403)return 'No tienes permiso para esta operación.';
      final body=error.response?.data;
      if(body is Map){
        final detail=body['message']??body['detail'];
        if(detail is String){
          final id=body['errorId'];
          return id is String?'$detail\nReferencia del error: $id':detail;
        }
      }
      if(error.response?.statusCode==404)return 'No se encuentra el recurso. Comprueba el despliegue del backend.';
      if(error.response==null)return 'No se pudo contactar con el servidor. Actualiza para comprobar el estado antes de repetir cambios.';
    }
    return 'No se pudo completar la operación. Comprueba los datos y actualiza.';
  }
}
List<Map<String,dynamic>> rows(dynamic value)=>planningRows(value);
