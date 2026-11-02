import 'package:flutter/material.dart';
import '../domain/planning_view.dart';
class RoundPlanSummary extends StatelessWidget {
  final PlanningRound round;
  final bool busy,canManage;
  final VoidCallback onSchedule,onResolve,onReorganize,onConfirm,onFields;
  const RoundPlanSummary({super.key,required this.round,required this.busy,required this.canManage,
    required this.onSchedule,required this.onResolve,required this.onReorganize,required this.onConfirm,required this.onFields});
  @override Widget build(BuildContext context){
    final draft=round.status=='DRAFT',confirmed=round.status=='CONFIRMED',cancelled=round.status=='CANCELLED';
    return Card(child:Padding(padding:const EdgeInsets.all(20),child:Column(crossAxisAlignment:CrossAxisAlignment.start,children:[
      Text(confirmed?'Jornada confirmada':draft&&round.pendingCount>0?'Hay partidos pendientes de horario':cancelled?'Jornada cancelada':'Prepara los horarios de esta jornada',style:Theme.of(context).textTheme.titleLarge),
      const SizedBox(height:12),Wrap(spacing:16,runSpacing:8,children:[
        Chip(label:Text('${round.assignedCount} ${confirmed?'publicados':'con horario'}')),
        Chip(label:Text('${round.pendingCount} pendientes')),
      ]),
      if(draft)const Text('El borrador está guardado. Puedes salir y volver: no perderás los horarios válidos ni los partidos pendientes.'),
      if(cancelled)const Text('Los horarios mostrados son históricos y sus reservas están liberadas.'),
      if(round.historyProvisional&&draft)const Padding(padding:EdgeInsets.only(top:8),child:Text('Borrador provisional: las jornadas anteriores todavía no están completas o su histórico debe revalidarse. Puedes seguir preparando partidos.')),
      if(round.searchLimited)const Padding(padding:EdgeInsets.only(top:8),child:Text('Se alcanzó el límite de búsqueda. Se ha guardado la propuesta válida encontrada; no es una prueba de que los pendientes sean imposibles.')),
      if(draft&&round.pendingCount>0)const Padding(padding:EdgeInsets.only(top:8),child:Text('La jornada completa no se puede confirmar hasta resolver los pendientes. Los partidos asignados no se publican todavía.')),
      if(draft&&round.earlierIncomplete)const Padding(padding:EdgeInsets.only(top:8),child:Text('Antes de publicar esta jornada hay que completar y confirmar las anteriores. Preparar sus horarios sí está permitido.')),
      if(!confirmed)...[const SizedBox(height:16),Wrap(spacing:12,runSpacing:8,children:[
        if(round.status=='UNSCHEDULED')FilledButton.icon(onPressed:busy||!canManage?null:onSchedule,icon:const Icon(Icons.auto_awesome),label:const Text('Preparar horarios')),
        if(cancelled)FilledButton.icon(onPressed:busy||!canManage?null:onReorganize,icon:const Icon(Icons.refresh),label:const Text('Preparar de nuevo')),
        if(draft&&round.pendingCount>0)FilledButton.icon(onPressed:busy||!canManage?null:onResolve,icon:const Icon(Icons.build_outlined),label:const Text('Resolver pendientes')),
        if(draft&&round.pendingCount==0&&!round.canConfirm)FilledButton.icon(onPressed:busy||!canManage?null:onResolve,icon:const Icon(Icons.fact_check_outlined),label:const Text('Revisar horarios')),
        if(draft)OutlinedButton.icon(onPressed:busy||!canManage||!round.canConfirm?null:onConfirm,icon:const Icon(Icons.check),label:const Text('Confirmar jornada')),
        TextButton(onPressed:busy?null:onFields,child:const Text('Campos y horarios')),
      ])],
    ])));
  }
}
