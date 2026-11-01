import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../data/planning_api.dart';

class WeeklyFieldsPanel extends ConsumerStatefulWidget {
  final String seasonId,range;
  final int week;
  final bool canManage,locked;
  final ValueChanged<bool> onDirty,onBusy;
  final VoidCallback onSaved,onBackToCalendar;
  const WeeklyFieldsPanel({super.key,required this.seasonId,required this.week,required this.range,
    required this.canManage,required this.locked,required this.onDirty,required this.onBusy,
    required this.onSaved,required this.onBackToCalendar});
  @override
  ConsumerState<WeeklyFieldsPanel> createState()=>_WeeklyFieldsPanelState();
}
class _WeeklyFieldsPanelState extends ConsumerState<WeeklyFieldsPanel>{
  bool loading=true,saving=false;
  String? error,notice;
  List<Map<String,dynamic>> fields=[];
  Set<String> selected={},original={};
  bool get dirty=>selected.length!=original.length||!selected.containsAll(original);
  @override
  void initState(){super.initState();Future.microtask(load);}
  Future<void> load() async {
    if(!mounted)return;setState((){loading=true;error=null;});
    try{
      final api=ref.read(planningApiProvider);
      final results=await Future.wait([api.fields(),api.availability(widget.seasonId,widget.week)]);
      if(!mounted)return;
      final ids=results[0].map((f)=>f['id'] as String).toSet();
      final enabled=results[1].where((a)=>a['enabled']==true).map((a)=>a['fieldId'] as String).where(ids.contains).toSet();
      setState((){fields=results[0];selected=enabled;original={...enabled};loading=false;});widget.onDirty(false);
    }catch(e){if(mounted)setState((){loading=false;error=PlanningApi.message(e);});}
  }
  Future<void> save() async {
    if(saving||loading||!dirty)return;
    if(selected.isEmpty){
      final ok=await showDialog<bool>(context:context,builder:(ctx)=>AlertDialog(title:const Text('¿Dejar esta semana sin campos?'),
        content:const Text('No se podrán asignar horarios mientras no haya campos disponibles.'),actions:[
          TextButton(onPressed:()=>Navigator.pop(ctx,false),child:const Text('Seguir eligiendo')),
          FilledButton(onPressed:()=>Navigator.pop(ctx,true),child:const Text('Guardar sin campos'))]));
      if(ok!=true||!mounted)return;
    }
    setState((){saving=true;error=null;notice=null;});widget.onBusy(true);
    try{
      await ref.read(planningApiProvider).saveAvailability(widget.seasonId,widget.week,selected);
      if(!mounted)return;
      setState((){original={...selected};notice='Campos guardados para la semana ${widget.week}.';});widget.onDirty(false);widget.onSaved();
    }catch(e){if(mounted)setState(()=>error=PlanningApi.message(e));}
    finally{if(mounted){setState(()=>saving=false);widget.onBusy(false);}}
  }
  Future<void> edit(Map<String,dynamic> field) async {
    final days=(field['days'] as List).map((e)=>e as String).toSet();
    final hours=(field['hours'] as List).map((e)=>(e as String).substring(0,5)).toSet();
    final ok=await showDialog<bool>(context:context,barrierDismissible:false,builder:(ctx)=>StatefulBuilder(builder:(ctx,change)=>AlertDialog(
      title:Text('Apertura de ${field['name']}'),scrollable:true,
      content:SizedBox(width:560,child:Column(crossAxisAlignment:CrossAxisAlignment.start,children:[
        const Text('Esta apertura es general: se utiliza en todas las semanas donde se habilite este campo. No cambia solo la semana seleccionada.'),
        const SizedBox(height:16),const Text('Días de apertura'),Wrap(spacing:8,children:[
          for(final e in const [['MONDAY','Lun'],['TUESDAY','Mar'],['WEDNESDAY','Mié'],['THURSDAY','Jue'],['FRIDAY','Vie'],['SATURDAY','Sáb'],['SUNDAY','Dom']])
            FilterChip(label:Text(e[1]),selected:days.contains(e[0]),onSelected:(v)=>change((){if(v){days.add(e[0]);}else{days.remove(e[0]);}})),
        ]),const SizedBox(height:16),const Text('Horas de inicio disponibles'),Wrap(spacing:8,children:[
          for(final h in {...List.generate(14,(i)=>'${(i+9).toString().padLeft(2,'0')}:00'),...hours}.toList()..sort())
            FilterChip(label:Text(h),selected:hours.contains(h),onSelected:(v)=>change((){if(v){hours.add(h);}else{hours.remove(h);}})),
        ]),
      ])),actions:[TextButton(onPressed:()=>Navigator.pop(ctx,false),child:const Text('Cancelar')),
        FilledButton(onPressed:days.isEmpty||hours.isEmpty?null:()=>Navigator.pop(ctx,true),child:const Text('Guardar apertura general'))],
    )));
    if(ok!=true||!mounted)return;
    setState((){saving=true;error=null;});widget.onBusy(true);
    try{
      await ref.read(planningApiProvider).saveOpening(field['id'] as String,days,hours);
      final updated=await ref.read(planningApiProvider).fields();
      if(mounted)setState(()=>fields=updated);
    }catch(e){if(mounted)setState(()=>error=PlanningApi.message(e));}
    finally{if(mounted){setState(()=>saving=false);widget.onBusy(false);}}
  }
  String day(String key)=>const {'MONDAY':'Lun','TUESDAY':'Mar','WEDNESDAY':'Mié','THURSDAY':'Jue','FRIDAY':'Vie','SATURDAY':'Sáb','SUNDAY':'Dom'}[key]??key;
  @override
  Widget build(BuildContext context){
    final disabled=loading||saving||!widget.canManage||widget.locked||error!=null;
    return Column(crossAxisAlignment:CrossAxisAlignment.stretch,children:[
      Text('Campos de la semana ${widget.week}',style:Theme.of(context).textTheme.headlineSmall),
      const SizedBox(height:6),Text(widget.range),const SizedBox(height:12),
      const Text('Marca los campos que se podrán utilizar en esta semana. Sus días y horas se muestran debajo del nombre.'),
      if(widget.locked)const Padding(padding:EdgeInsets.only(top:12),child:Text('Esta jornada está confirmada. Cancélala antes de cambiar su disponibilidad.')),
      if(loading||saving)const Padding(padding:EdgeInsets.all(16),child:LinearProgressIndicator()),
      if(error!=null)Card(color:Theme.of(context).colorScheme.errorContainer,child:Padding(padding:const EdgeInsets.all(16),child:Column(crossAxisAlignment:CrossAxisAlignment.start,children:[
        Text(error!),TextButton(onPressed:saving?null:load,child:const Text('Volver a cargar'))]))),
      if(notice!=null)Padding(padding:const EdgeInsets.all(12),child:Text(notice!)),
      if(!loading&&fields.isEmpty)const Padding(padding:EdgeInsets.all(24),child:Text('No hay campos activos. Crea o activa uno desde Instalaciones/Campos.')),
      for(final f in fields)Card(child:Padding(padding:const EdgeInsets.all(8),child:Column(children:[
        CheckboxListTile(value:selected.contains(f['id']),onChanged:disabled?null:(v){setState((){if(v==true){selected.add(f['id'] as String);}else{selected.remove(f['id']);}});widget.onDirty(dirty);},
          title:Text('${f['name']} · ${f['venueName']}'),
          subtitle:Text('${(f['days'] as List).map((d)=>day(d as String)).join(', ')}\n${((f['hours'] as List).map((h)=>(h as String).substring(0,5)).toList()..sort()).join(' · ')}')),
        Align(alignment:Alignment.centerRight,child:TextButton(onPressed:disabled?null:()=>edit(f),child:const Text('Ver o cambiar apertura general'))),
      ]))),
      const SizedBox(height:16),Wrap(spacing:12,runSpacing:8,children:[
        FilledButton.icon(onPressed:disabled||!dirty?null:save,icon:const Icon(Icons.save_outlined),label:const Text('Guardar campos de esta semana')),
        OutlinedButton(onPressed:saving?null:widget.onBackToCalendar,child:const Text('Volver a la jornada')),
      ]),
    ]);
  }
}
