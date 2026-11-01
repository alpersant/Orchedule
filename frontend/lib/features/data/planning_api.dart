import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../app/providers.dart';
import '../domain/planning_view.dart';

final planningApiProvider=Provider((ref)=>PlanningApi(ref.watch(dioProvider)));
class PlanningApi {
  final Dio dio;
  PlanningApi(this.dio);
  String path(String id)=>'/v1/planning/seasons/${Uri.encodeComponent(id)}';
  Future<Map<String,dynamic>> read(String id) async {
    final response=await dio.get<Map<String,dynamic>>(path(id));
    if(response.data==null)throw const FormatException('Respuesta vacía');
    return response.data!;
  }
  Future<PlanningView> view(String id) async=>PlanningView.fromJson(await read(id));
  Future<void> configure(String id,Map<String,dynamic> body) async {
    // Zone and duration are server defaults, not organizer input.
    final safe=Map<String,dynamic>.from(body)..remove('zoneId')..remove('durationMinutes');
    await dio.post<dynamic>(path(id),data:safe);
  }
  Future<void> action(String id,int number,String action,int version) async {
    await dio.post<dynamic>('${path(id)}/rounds/$number/$action',data:{'version':version});
  }
  Future<void> cancelLegacy(String id) async {await dio.post<dynamic>('/schedules/rounds/${Uri.encodeComponent(id)}/cancel');}
  Future<List<Map<String,dynamic>>> fields() async {
    final r=await dio.get<dynamic>('/v1/planning/fields');return planningRows(r.data);
  }
  Future<void> saveOpening(String field,Set<String> days,Set<String> hours) async {
    await dio.put<dynamic>('/v1/planning/fields/${Uri.encodeComponent(field)}/opening',data:{'days':days.toList(),'hours':hours.toList()});
  }
  Future<List<Map<String,dynamic>>> availability(String season,int week) async {
    final r=await dio.get<dynamic>('/v1/fields/availability',queryParameters:{'seasonId':season,'weekNumber':week});
    return planningRows(r.data);
  }
  Future<void> saveAvailability(String season,int week,Set<String> ids) async {
    await dio.put<dynamic>('/v1/fields/availability',data:{'seasonId':season,'weekNumber':week,'enabledFieldIds':ids.toList()});
  }
  static String message(Object error){
    if(error is DioException){
      if(error.response?.statusCode==401)return 'Tu sesión ha caducado. Vuelve a iniciar sesión.';
      if(error.response?.statusCode==403)return 'No tienes permiso para realizar este cambio.';
      final body=error.response?.data;
      if(body is Map){
        if(body['message'] is String)return body['message'] as String;
        if(body['detail'] is String)return body['detail'] as String;
      }
      if(error.response?.statusCode==409)return 'Los datos han cambiado. Actualiza antes de continuar.';
      if(error.response?.statusCode==404)return 'No se encuentra el recurso. Comprueba que backend y frontend están actualizados.';
      if(error.response==null)return 'No se pudo conectar con el servidor. Tus cambios no se han confirmado.';
    }
    return 'No se pudo completar la operación. Actualiza y vuelve a intentarlo.';
  }
}
// Compatibility for files from the previous package.
List<Map<String,dynamic>> rows(dynamic value)=>planningRows(value);
