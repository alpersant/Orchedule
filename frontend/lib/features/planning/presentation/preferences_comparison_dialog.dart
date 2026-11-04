import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../data/planning_api.dart';
import '../domain/planning_view.dart';
import 'spanish_weekdays.dart';
class PreferencesComparisonDialog extends ConsumerStatefulWidget {
  final String seasonId;final PlanningGame game;
  const PreferencesComparisonDialog({super.key,required this.seasonId,required this.game});
  @override ConsumerState<PreferencesComparisonDialog> createState()=>_PreferencesComparisonDialogState();
}
class _PreferencesComparisonDialogState extends ConsumerState<PreferencesComparisonDialog>{
  late final Future<List<Map<String,dynamic>?>> future;
  @override void initState(){super.initState();future=load();}
  Future<List<Map<String,dynamic>?>> load() async {
    final api=ref.read(planningApiProvider);final season=await api.get<Map<String,dynamic>>('/v1/seasons/${widget.seasonId}');
    if(season==null)throw const FormatException('No se encuentra la temporada');
    Future<Map<String,dynamic>?> pref(String id) async {
      try{return await api.get<Map<String,dynamic>>('/teams/$id/preferences',query:{'competitionId':season['competitionId']});}
      on DioException catch(e){if(e.response?.statusCode==404)return null;rethrow;}
    }
    return Future.wait([pref(widget.game.homeId),pref(widget.game.awayId)]);
  }
  String hour(dynamic value)=>value.toString().replaceFirst('H','').replaceAll('_',':');
  Widget team(String name,Map<String,dynamic>? p){
    if(p==null)return ListTile(title:Text(name),subtitle:const Text('No hay preferencias guardadas para esta competición.'));
    final days=planningRows(p['dayPreferences']),hours=planningRows(p['timePreferences']);
    return Padding(padding:const EdgeInsets.symmetric(vertical:12),child:Column(crossAxisAlignment:CrossAxisAlignment.start,children:[
      Text(name,style:Theme.of(context).textTheme.titleMedium),const SizedBox(height:8),
      if(p['excludedDay']!=null)Text('Día excluido: ${weekdayEs(p['excludedDay'] as String)}'),
      if(p['excludedHour']!=null)Text('Hora excluida: ${hour(p['excludedHour'])}'),
      Text('Días preferentes: ${days.where((d)=>d['priority']=='FIRST').map((d)=>weekdayEs(d['day'] as String)).join(', ')}'),
      Text('Días alternativos: ${days.where((d)=>d['priority']=='SECOND').map((d)=>weekdayEs(d['day'] as String)).join(', ')}'),
      Text('Horas preferentes: ${hours.where((h)=>h['priority']=='FIRST').map((h)=>hour(h['hour'])).join(', ')}'),
      Text('Horas alternativas: ${hours.where((h)=>h['priority']=='SECOND').map((h)=>hour(h['hour'])).join(', ')}'),
    ]));
  }
  @override Widget build(BuildContext context)=>AlertDialog(title:const Text('Comparar preferencias de los equipos'),scrollable:true,
    content:SizedBox(width:680,child:FutureBuilder<List<Map<String,dynamic>?>>(future:future,builder:(context,snapshot){
      if(snapshot.connectionState!=ConnectionState.done)return const Padding(padding:EdgeInsets.all(24),child:CircularProgressIndicator());
      if(snapshot.hasError)return Text(PlanningApi.message(snapshot.error!));
      final p=snapshot.data!;return Column(crossAxisAlignment:CrossAxisAlignment.start,children:[
        team(widget.game.home,p[0]),const Divider(),team(widget.game.away,p[1]),const Divider(),
        const Text('Esta comparación no cambia las preferencias. Los cambios deben realizarse expresamente en la gestión de equipos y pueden afectar a otras jornadas de la competición.'),
      ]);
    })),actions:[TextButton(onPressed:()=>Navigator.pop(context),child:const Text('Cerrar'))]);
}
