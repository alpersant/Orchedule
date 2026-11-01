import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../app/providers.dart';

final planningApiProvider = Provider((ref) => PlanningApi(ref.watch(dioProvider)));
class PlanningApi {
  final Dio dio;
  PlanningApi(this.dio);
  String path(String id) => '/v1/planning/seasons/${Uri.encodeComponent(id)}';
  Future<Map<String, dynamic>> read(String id) async {
    final r = await dio.get<Map<String, dynamic>>(path(id));
    if (r.data == null) throw const FormatException('Respuesta vacía');
    return r.data!;
  }
  Future<void> configure(String id, Map<String, dynamic> body) async {
    await dio.post<dynamic>(path(id), data: body);
  }
  Future<void> action(String id, int number, String action, int version) async {
    await dio.post<dynamic>('${path(id)}/rounds/$number/$action', data: {'version': version});
  }
  Future<void> cancelLegacy(String id) async {
    await dio.post<dynamic>('/schedules/rounds/${Uri.encodeComponent(id)}/cancel');
  }
  static String message(Object error) {
    if (error is DioException) {
      final status = error.response?.statusCode;
      if (status == 401) return 'La sesión ha caducado. Inicia sesión de nuevo.';
      if (status == 403) return 'No tienes permisos para esta operación.';
      final body = error.response?.data;
      if (body is Map && body['message'] is String) return body['message'] as String;
      if (body is Map && body['detail'] is String) return body['detail'] as String;
      if (status == 409) return 'Los datos han cambiado o hay un conflicto. Actualiza la pantalla.';
      if (status == 404) return 'No se encuentra la temporada o el nuevo endpoint. Comprueba el despliegue.';
      if (error.response == null) return 'No se pudo contactar con el servidor.';
    }
    return 'No se pudo completar la operación. Comprueba los datos y vuelve a intentarlo.';
  }
}
List<Map<String, dynamic>> rows(dynamic value) => (value as List<dynamic>)
    .map((e) => Map<String, dynamic>.from(e as Map)).toList();
