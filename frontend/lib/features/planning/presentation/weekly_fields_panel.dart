import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../data/planning_api.dart';
class WeeklyFieldsPanel extends ConsumerStatefulWidget {
  final String seasonId,range;final int week;final bool canManage,locked;
  final ValueChanged<bool> onDirty,onBusy;final VoidCallback onSaved,onBackToCalendar;
  const WeeklyFieldsPanel({super.key,required this.seasonId,required this.week,required this.range,required this.canManage,required this.locked,
    required this.onDirty,required this.onBusy,required this.onSaved,required this.onBackToCalendar});
  @override ConsumerState<WeeklyFieldsPanel> createState()=>_WeeklyFieldsPanelState();
}
class _WeeklyFieldsPanelState extends ConsumerState<WeeklyFieldsPanel>{
  bool loading=true,saving=false,loaded=false;String? error,notice;
  List<Map<String,dynamic>> fields=[];Set<String> selected={},original={};
  bool get dirty=>selected.length!=original.length||!selected.containsAll(original);
  @override void initState(){super.initState();Future.microtask(load);}
  Future<void> load() async {
    if(!mounted)return;setState((){loading=true;loaded=false;error=null;});
    try{
      final api=ref.read(planningApiProvider);final result=await Future.wait([api.fields(),api.availability(widget.seasonId,widget.week)]);
      if(!mounted)return;final ids=result[0].map((f)=>f['id'] as String).toSet();
      final enabled=result[1].where((a)=>a['enabled']==true).map((a)=>a['fieldId'] as String).where(ids.contains).toSet();
      setState((){fields=result[0];selected=enabled;original={...enabled};loaded=true;loading=false;});widget.onDirty(false);
    }catch(e){if(mounted)setState((){loading=false;error=PlanningApi.message(e);});}
  }
  Future<bool> ask(String title,String content) async=>await showDialog<bool>(context:context,builder:(ctx)=>AlertDialog(title:Text(title),content:Text(content),actions:[
    TextButton(onPressed:()=>Navigator.pop(ctx,false),child:const Text('Volver')),FilledButton(onPressed:()=>Navigator.pop(ctx,true),child:const Text('Continuar'))]))==true;
  Future<void> reload() async {if(dirty&&!await ask('¿Descartar cambios sin guardar?','Se recuperarán los campos guardados en el servidor.'))return;if(mounted)await load();}
  Future<void> save() async {
    if(saving||loading||!dirty)return;
    if(selected.isEmpty&&!await ask('¿Guardar esta semana sin campos?','No se podrán asignar horarios hasta habilitar al menos un campo.'))return;
    if(!mounted)return;setState((){saving=true;error=null;notice=null;});widget.onBusy(true);
    try{await ref.read(planningApiProvider).saveAvailability(widget.seasonId,widget.week,selected);
      if(mounted){setState((){original={...selected};notice='Campos de esta semana guardados.';});widget.onDirty(false);widget.onSaved();}}
    catch(e){if(mounted)setState(()=>error=PlanningApi.message(e));}
    finally{if(mounted){setState(()=>saving=false);widget.onBusy(false);}}
  }
  String day(String value)=>const {'MONDAY':'Lun','TUESDAY':'Mar','WEDNESDAY':'Mié','THURSDAY':'Jue','FRIDAY':'Vie','SATURDAY':'Sáb','SUNDAY':'Dom'}[value]??value;
  Future<void> edit(Map<String,dynamic> field) async {
    final days=(field['days'] as List).map((e)=>e as String).toSet();final hours=(field['hours'] as List).map((e)=>(e as String).substring(0,5)).toSet();
    final ok=await showDialog<bool>(context:context,barrierDismissible:false,builder:(ctx)=>StatefulBuilder(builder:(ctx,change)=>AlertDialog(title:Text('Apertura de ${field['name']}'),scrollable:true,
      content:SizedBox(width:560,child:Column(crossAxisAlignment:CrossAxisAlignment.start,children:[
        const Text('Esta apertura es general para todas las semanas donde se utilice este campo, no solo la semana seleccionada.'),const SizedBox(height:12),
        Wrap(spacing:8,children:[for(final d in const ['MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY','SUNDAY'])FilterChip(label:Text(day(d)),selected:days.contains(d),onSelected:(v)=>change((){if(v){days.add(d);}else{days.remove(d);}}))]),
        const SizedBox(height:12),const Text('Horas de inicio disponibles'),Wrap(spacing:8,children:[for(final h in ({...List.generate(14,(i)=>'${(i+9).toString().padLeft(2,'0')}:00'),...hours}.toList()..sort()))FilterChip(label:Text(h),selected:hours.contains(h),onSelected:(v)=>change((){if(v){hours.add(h);}else{hours.remove(h);}}))]),
      ])),actions:[TextButton(onPressed:()=>Navigator.pop(ctx,false),child:const Text('Cancelar')),FilledButton(onPressed:days.isEmpty||hours.isEmpty?null:()=>Navigator.pop(ctx,true),child:const Text('Guardar apertura general'))])));
    if(ok!=true||!mounted)return;setState((){saving=true;error=null;});widget.onBusy(true);
    try{await ref.read(planningApiProvider).saveOpening(field['id'] as String,days,hours);final updated=await ref.read(planningApiProvider).fields();if(mounted)setState(()=>fields=updated);}
    catch(e){if(mounted)setState(()=>error=PlanningApi.message(e));}
    finally{if(mounted){setState(()=>saving=false);widget.onBusy(false);}}
  }
  @override Widget build(BuildContext context){
    final disabled=loading||saving||!loaded||!widget.canManage||widget.locked;
    return Column(crossAxisAlignment:CrossAxisAlignment.stretch,children:[
      Text('Campos de la semana ${widget.week}',style:Theme.of(context).textTheme.headlineSmall),Text(widget.range),const SizedBox(height:12),
      const Text('Marca los campos disponibles para esta semana. Los días y horas de apertura se muestran debajo de cada nombre.'),
      if(widget.locked)const Text('Jornada confirmada: cancélala antes de cambiar sus campos.'),
      if(loading||saving)const Padding(padding:EdgeInsets.all(12),child:LinearProgressIndicator()),
      if(error!=null)Card(color:Theme.of(context).colorScheme.errorContainer,child:Padding(padding:const EdgeInsets.all(16),child:Column(crossAxisAlignment:CrossAxisAlignment.start,children:[Text(error!),TextButton(onPressed:saving?null:reload,child:const Text('Volver a cargar'))]))),
      if(notice!=null)Padding(padding:const EdgeInsets.all(12),child:Text(notice!)),
      if(!loading&&fields.isEmpty)const Padding(padding:EdgeInsets.all(24),child:Text('No hay campos activos. Crea o activa uno desde Instalaciones y campos.')),
      for(final f in fields)Card(child:Column(children:[CheckboxListTile(title:Text('${f['name']} · ${f['venueName']}'),
        subtitle:Text('${(f['days'] as List).map((d)=>day(d as String)).join(', ')}\n${((f['hours'] as List).map((h)=>(h as String).substring(0,5)).toList()..sort()).join(' · ')}'),
        value:selected.contains(f['id']),onChanged:disabled?null:(v){setState((){if(v==true){selected.add(f['id'] as String);}else{selected.remove(f['id']);}});widget.onDirty(dirty);}),
        Align(alignment:Alignment.centerRight,child:TextButton(onPressed:disabled?null:()=>edit(f),child:const Text('Ver o cambiar apertura general'))),
      ])),const SizedBox(height:16),Wrap(spacing:12,runSpacing:8,children:[
        FilledButton.icon(onPressed:disabled||!dirty?null:save,icon:const Icon(Icons.save_outlined),label:const Text('Guardar campos de esta semana')),
        OutlinedButton(onPressed:saving?null:widget.onBackToCalendar,child:const Text('Volver a la jornada')),
      ]),
    ]);
  }
}
