import 'package:flutter/material.dart';
import '../domain/planning_view.dart';
class PendingGameCard extends StatelessWidget {
  final PlanningGame game;
  final VoidCallback onFields,onTeams,onDates;
  final bool disabled;
  const PendingGameCard({super.key,required this.game,required this.onFields,required this.onTeams,required this.onDates,this.disabled=false});
  Future<void> details(BuildContext context) async {
    final issue=game.issue;
    await showDialog<void>(context:context,builder:(ctx)=>AlertDialog(title:Text('${game.home} — ${game.away}'),scrollable:true,
      content:SizedBox(width:620,child:Column(crossAxisAlignment:CrossAxisAlignment.start,children:[
        Text(issue?.detail??'Todavía no se ha intentado asignar un horario a este partido.'),
        if(issue!=null&&issue.code!='MISSING_PREFERENCES'&&issue.code!='NO_FIELDS_ENABLED')...[
          const SizedBox(height:16),const Text('Franjas que superan cada comprobación:'),
          Text('Franjas de los campos: ${issue.total}'),Text('Dentro de fechas y apertura: ${issue.dates}'),
          Text('Compatibles con ambos equipos: ${issue.compatible}'),Text('Tras aplicar la rotación: ${issue.rotation}'),Text('Sin ocupaciones o bloqueos externos: ${issue.free}'),
          const SizedBox(height:12),const Text('Estos recuentos no garantizan que todos los partidos puedan ocupar sus franjas simultáneamente.'),
        ],
      ])),actions:[TextButton(onPressed:()=>Navigator.pop(ctx),child:const Text('Cerrar'))]));
  }
  @override Widget build(BuildContext context){
    final code=game.issue?.code;
    final label=code=='MISSING_PREFERENCES'?'Revisar equipos':code=='NO_VALID_DATES'?'Revisar fechas':'Revisar campos y horarios';
    final action=code=='MISSING_PREFERENCES'?onTeams:code=='NO_VALID_DATES'?onDates:onFields;
    return Card(child:Padding(padding:const EdgeInsets.all(18),child:Column(crossAxisAlignment:CrossAxisAlignment.start,children:[
      Wrap(spacing:12,runSpacing:8,children:[Text('${game.home} — ${game.away}',style:Theme.of(context).textTheme.titleMedium),const Chip(label:Text('Pendiente de horario'))]),
      const SizedBox(height:8),Text(game.issue?.detail??'Sin horario todavía. Prepara la jornada para buscar una franja.'),
      const SizedBox(height:12),Wrap(spacing:8,runSpacing:4,children:[TextButton(onPressed:disabled?null:()=>details(context),child:const Text('Ver diagnóstico')),
        OutlinedButton(onPressed:disabled?null:action,child:Text(label))]),
    ])));
  }
}
