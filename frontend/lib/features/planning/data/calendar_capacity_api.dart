import 'dart:async';
import 'package:dio/dio.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../app/providers.dart';

final calendarCapacityApiProvider=Provider((ref)=>CalendarCapacityApi(ref.watch(dioProvider)));
class CalendarCapacityPreview {
  final String seasonId,seasonName,startDate,endDate,message;
  final int selectedTeams,availableWeeks;
  final int? requiredRounds,missingWeeks;
  final bool? fits;
  final String? earliestEndDate,suggestedEndDate;
  final bool alreadyConfigured,canConfigureBase,withinSupportedRange,canContinue,hasPastDates;
  const CalendarCapacityPreview({required this.seasonId,required this.seasonName,required this.startDate,required this.endDate,
    required this.message,required this.selectedTeams,required this.availableWeeks,required this.requiredRounds,required this.missingWeeks,
    required this.fits,required this.earliestEndDate,required this.suggestedEndDate,required this.alreadyConfigured,
    required this.canConfigureBase,required this.withinSupportedRange,required this.canContinue,required this.hasPastDates});
  factory CalendarCapacityPreview.fromJson(Map<String,dynamic> j)=>CalendarCapacityPreview(
    seasonId:j['seasonId'] as String,seasonName:j['seasonName'] as String,startDate:j['startDate'] as String,endDate:j['endDate'] as String,
    message:j['message'] as String,selectedTeams:j['selectedTeams'] as int,availableWeeks:j['availableWeeks'] as int,
    requiredRounds:j['requiredRounds'] as int?,missingWeeks:j['missingWeeks'] as int?,fits:j['fits'] as bool?,
    earliestEndDate:j['earliestEndDate'] as String?,suggestedEndDate:j['suggestedEndDate'] as String?,alreadyConfigured:j['alreadyConfigured'] as bool,
    canConfigureBase:j['canConfigureBase'] as bool,withinSupportedRange:j['withinSupportedRange'] as bool,canContinue:j['canContinue'] as bool,hasPastDates:j['hasPastDates'] as bool);
}
class CalendarCapacityTimeout implements Exception {
  const CalendarCapacityTimeout();
}
class CalendarCapacityApi {
  final Dio dio;
  final Duration deadline;
  CancelToken? _checkToken;
  CalendarCapacityApi(this.dio,{this.deadline=const Duration(seconds:3)});
  String path(String id)=>'/v1/planning/seasons/${Uri.encodeComponent(id)}';
  void cancelCheck([String reason='superseded']){
    _checkToken?.cancel(reason);_checkToken=null;
  }
  static bool isCancellation(Object e)=>e is DioException&&e.type==DioExceptionType.cancel;
  Future<CalendarCapacityPreview> check(String id,{required String mode,required List<String> teamIds,int? manualRoundCount}) async {
    if(id.trim().isEmpty)throw const FormatException('Falta el identificador de temporada');
    cancelCheck();final token=CancelToken();_checkToken=token;
    final watch=Stopwatch()..start();
    if(kDebugMode)debugPrint('[calendar-preflight] request started');
    try{
      // This deadline covers the entire Future: interceptors, connection and response.
      final r=await dio.post<Map<String,dynamic>>('${path(id)}/preflight',
        cancelToken:token,options:Options(sendTimeout:deadline,receiveTimeout:deadline),
        data:{'mode':mode,'teamIds':teamIds,if(mode=='MANUAL')'manualRoundCount':manualRoundCount??0})
        .timeout(deadline,onTimeout:(){token.cancel('deadline');throw const CalendarCapacityTimeout();});
      if(r.data==null)throw const FormatException('Respuesta de comprobación vacía');
      return CalendarCapacityPreview.fromJson(r.data!);
    }finally{
      watch.stop();if(identical(_checkToken,token))_checkToken=null;
      if(kDebugMode)debugPrint('[calendar-preflight] finished after ${watch.elapsedMilliseconds} ms');
    }
  }
  Future<void> extend(String id,{required String expectedStartDate,required String expectedEndDate,required String endDate}) async {
    await dio.patch<dynamic>('${path(id)}/end-date',data:{'expectedStartDate':expectedStartDate,'expectedEndDate':expectedEndDate,'endDate':endDate});
  }
  static String message(Object e){
    if(e is CalendarCapacityTimeout)return 'La comprobación no ha respondido en 3 segundos. No se ha creado ningún calendario. Puedes reintentar.';
    if(e is DioException){
      if([DioExceptionType.connectionTimeout,DioExceptionType.sendTimeout,DioExceptionType.receiveTimeout].contains(e.type))return 'La comprobación ha superado el tiempo de espera. Reintenta.';
      if(e.response?.statusCode==401)return 'La sesión ha caducado. Vuelve a iniciar sesión.';
      if(e.response?.statusCode==403)return 'No tienes permiso para configurar esta temporada.';
      final body=e.response?.data;
      if(body is Map){if(body['message'] is String)return body['message'] as String;if(body['detail'] is String)return body['detail'] as String;}
      if(e.response?.statusCode==404)return 'No se encuentra la temporada o el endpoint de comprobación. Revisa el despliegue del backend.';
    }
    return 'No se pudieron comprobar las fechas. Reintenta antes de continuar.';
  }
}
String capacityDate(String value){final d=DateTime.parse(value);return '${d.day.toString().padLeft(2,'0')}/${d.month.toString().padLeft(2,'0')}/${d.year}';}
String capacityIso(DateTime d)=>'${d.year.toString().padLeft(4,'0')}-${d.month.toString().padLeft(2,'0')}-${d.day.toString().padLeft(2,'0')}';
