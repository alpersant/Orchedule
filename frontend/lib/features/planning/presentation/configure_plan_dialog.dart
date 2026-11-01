import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../app/providers.dart';
import '../data/calendar_capacity_api.dart';
import '../domain/team_name_order.dart';
import '../domain/manual_fixtures.dart';

class ConfigurePlanDialog extends StatefulWidget {
  final String seasonId;
  final List<Map<String,dynamic>> teams;
  final String? seasonName,startDate,endDate;
  final CalendarCapacityApi? capacityApi;
  final VoidCallback? onSeasonDatesChanged;
  const ConfigurePlanDialog({super.key,required this.seasonId,required this.teams,
    this.seasonName,this.startDate,this.endDate,this.capacityApi,this.onSeasonDatesChanged});
  @override State<ConfigurePlanDialog> createState()=>_ConfigurePlanDialogState();
}
class _ConfigurePlanDialogState extends State<ConfigurePlanDialog>{
  List<Map<String,dynamic>> teams=[];
  final selected=<String>{};
  final matches=<Map<String,dynamic>>[];
  String mode='SINGLE',filter='';
  int step=0,revision=0;
  String? home,away,error,sourceError,capacityError,notice;
  final round=TextEditingController(text:'1');
  final csv=TextEditingController(text:'round,homeTeamId,awayTeamId\n');
  List<Map<String,dynamic>>? manualRounds;
  CalendarCapacityPreview? preview,period;
  CalendarCapacityApi? api;
  Timer? debounce;
  bool initialized=false,checking=false,submitting=false,extending=false;
  bool get manual=>mode=='MANUAL';
  int get reviewStep=>manual?3:2;
  bool get busy=>submitting||extending;
  List<Map<String,dynamic>> get participants=>teams.where((t)=>selected.contains(t['id'])).toList();
  @override void initState(){
    super.initState();
    if(widget.seasonId.trim().isEmpty)sourceError='La pantalla no ha recibido una temporada válida. Abre de nuevo el calendario desde Temporadas.';
    try{teams=orderedTeams(widget.teams);selected.addAll(teams.map((t)=>t['id'] as String));}
    on FormatException catch(e){sourceError=e.message;}
  }
  @override void didChangeDependencies(){
    super.didChangeDependencies();if(initialized)return;initialized=true;
    // Private client per modal: disposing this dialog cannot cancel another dialog's request.
    api=widget.capacityApi??CalendarCapacityApi(ProviderScope.containerOf(context,listen:false).read(dioProvider));
    Future.microtask(check);
  }
  @override void dispose(){debounce?.cancel();revision++;api?.cancelCheck('dialog closed');round.dispose();csv.dispose();super.dispose();}
  String name(String id)=>teams.firstWhere((t)=>t['id']==id)['name'] as String;
  String? selectionError(){
    if(sourceError!=null)return sourceError;
    if(selected.length<2)return 'Selecciona al menos 2 equipos.';
    if(selected.length>64)return 'Esta versión admite un máximo de 64 equipos.';
    return null;
  }
  int get manualCount=>matches.fold<int>(0,(n,m)=>(m['round'] as int)>n?m['round'] as int:n);
  void changed(){
    debounce?.cancel();revision++;api?.cancelCheck();preview=null;capacityError=null;error=null;notice=null;checking=false;
    if(sourceError!=null||api==null||widget.seasonId.trim().isEmpty||selected.length>64)return;
    checking=true;debounce=Timer(const Duration(milliseconds:250),check);
  }
  Future<bool> check() async {
    debounce?.cancel();
    if(!mounted)return false;
    if(api==null||sourceError!=null||widget.seasonId.trim().isEmpty||selected.length>64){
      setState(()=>checking=false);return false;
    }
    final current=++revision;
    setState((){checking=true;capacityError=null;preview=null;});
    try{
      final result=await api!.check(widget.seasonId,mode:mode,teamIds:participants.map((t)=>t['id'] as String).toList(),manualRoundCount:manual?manualCount:null);
      if(!mounted||current!=revision)return false;
      setState((){preview=result;period=result;});return result.canContinue;
    }catch(e){
      if(mounted&&current==revision&&!CalendarCapacityApi.isCancellation(e))setState(()=>capacityError=CalendarCapacityApi.message(e));
      return false;
    }finally{
      if(mounted&&current==revision)setState(()=>checking=false);
    }
  }
  Future<void> next() async {
    if(busy)return;
    if(step==0){setState(()=>step=1);return;}
    final issue=selectionError();if(issue!=null){setState(()=>error=issue);return;}
    if(manual&&step==2){
      try{
        final text='round,homeTeamId,awayTeamId\n${matches.map((m)=>"${m['round']},${m['homeTeamId']},${m['awayTeamId']}").join('\n')}';
        manualRounds=ManualFixtures.parse(text,selected);
      }on FormatException catch(e){setState(()=>error=e.message);return;}
    }
    setState(()=>submitting=true);
    try{
      final fits=await check();if(!mounted)return;
      final unknown=manual&&step==1&&manualCount==0&&preview?.canConfigureBase==true;
      if(fits||unknown)setState((){error=null;step++;});
    }finally{if(mounted)setState(()=>submitting=false);}
  }
  Future<void> finish() async {
    if(busy)return;
    final issue=selectionError();if(issue!=null){setState(()=>error=issue);return;}
    setState(()=>submitting=true);
    try{
      if(!await check()||!mounted)return;
      Navigator.pop(context,<String,dynamic>{'mode':mode,'teamIds':participants.map((t)=>t['id'] as String).toList(),if(manual)'rounds':manualRounds});
    }finally{if(mounted)setState(()=>submitting=false);}
  }
  Future<void> extend() async {
    final p=preview??period;if(p==null||busy||checking)return;
    final old=DateTime.parse(p.endDate),maximum=DateTime(9999,12,31);
    final minimum=DateTime(old.year,old.month,old.day+1);
    if(minimum.isAfter(maximum)){setState(()=>capacityError='No se puede ampliar más la fecha. Cambia el formato o los participantes.');return;}
    final suggested=p.suggestedEndDate==null?minimum:DateTime.parse(p.suggestedEndDate!);
    final initial=suggested.isBefore(minimum)?minimum:suggested.isAfter(maximum)?maximum:suggested;
    final chosen=await showDatePicker(context:context,initialDate:initial,firstDate:minimum,lastDate:maximum,helpText:'Nueva fecha final de temporada');
    if(chosen==null||!mounted)return;
    final yes=await showDialog<bool>(context:context,builder:(ctx)=>AlertDialog(title:const Text('¿Ampliar la temporada?'),
      content:Text('La fecha final cambiará de ${capacityDate(p.endDate)} a ${capacityDate(capacityIso(chosen))}. Conservarás los equipos y cruces.\n\nLa nueva fecha se guardará aunque después cierres el asistente sin crear el calendario.'),
      actions:[TextButton(onPressed:()=>Navigator.pop(ctx,false),child:const Text('Conservar fecha actual')),
        FilledButton(onPressed:()=>Navigator.pop(ctx,true),child:const Text('Guardar nueva fecha final'))]));
    if(yes!=true||!mounted)return;
    setState((){extending=true;capacityError=null;});
    try{
      await api!.extend(widget.seasonId,expectedStartDate:p.startDate,expectedEndDate:p.endDate,endDate:capacityIso(chosen));
      if(!mounted)return;widget.onSeasonDatesChanged?.call();
      setState(()=>notice='Fecha final guardada.');await check();
    }catch(e){if(mounted)setState(()=>capacityError=CalendarCapacityApi.message(e));}
    finally{if(mounted)setState(()=>extending=false);}
  }
  void add(){
    final n=int.tryParse(round.text);
    if(n==null||n<1||n>126){setState(()=>error='La jornada debe estar entre 1 y 126.');return;}
    if(home==null||away==null||home==away){setState(()=>error='Elige dos equipos distintos.');return;}
    if(matches.any((m)=>m['round']==n&&[m['homeTeamId'],m['awayTeamId']].any((id)=>id==home||id==away))){setState(()=>error='Uno de esos equipos ya juega en esta jornada.');return;}
    setState((){matches.add({'round':n,'homeTeamId':home,'awayTeamId':away});home=null;away=null;changed();});
  }
  void importCsv(){
    try{
      final parsed=ManualFixtures.parse(csv.text,selected),incoming=<Map<String,dynamic>>[];
      for(var i=0;i<parsed.length;i++)for(final g in parsed[i]['games'] as List)incoming.add({'round':i+1,...Map<String,dynamic>.from(g as Map)});
      setState((){matches..clear()..addAll(incoming);changed();});
    }on FormatException catch(e){setState(()=>error=e.message);}
  }
  Widget banner(BuildContext context){
    final p=preview??period;
    return Card(color:preview?.fits==false?Theme.of(context).colorScheme.errorContainer:Theme.of(context).colorScheme.surfaceContainerLow,
      child:Padding(padding:const EdgeInsets.all(16),child:Column(crossAxisAlignment:CrossAxisAlignment.start,children:[
        if(p!=null)...[Text('${capacityDate(p.startDate)} — ${capacityDate(p.endDate)}',style:Theme.of(context).textTheme.titleSmall),
          Text('${p.availableWeeks} semanas · una jornada por semana')],
        if(checking)const Padding(padding:EdgeInsets.only(top:12),child:LinearProgressIndicator()),
        if(checking)const Padding(padding:EdgeInsets.only(top:6),child:Text('Comprobando fechas…')),
        if(preview!=null)Padding(padding:const EdgeInsets.only(top:8),child:Text(preview!.message)),
        if(preview?.hasPastDates==true)const Text('El periodo ya ha comenzado. No se podrán asignar partidos nuevos a fechas pasadas.'),
        if(capacityError!=null)...[const SizedBox(height:8),Text(capacityError!),TextButton(onPressed:busy||checking?null:check,child:const Text('Reintentar comprobación'))],
        if(notice!=null)Text(notice!),
        if(preview?.fits==false&&preview?.canConfigureBase==true&&preview?.withinSupportedRange==true)...[
          const SizedBox(height:8),Text('Para disponer de la última semana completa, puedes ampliar hasta el ${capacityDate(preview!.suggestedEndDate!)}.'),
          Wrap(spacing:8,children:[FilledButton.tonal(onPressed:busy||checking?null:extend,child:const Text('Ampliar fecha final')),
            TextButton(onPressed:busy?null:()=>setState(()=>step=0),child:const Text('Cambiar formato')),
            TextButton(onPressed:busy?null:()=>setState(()=>step=1),child:const Text('Revisar equipos'))]),
        ],
      ])));
  }
  @override Widget build(BuildContext context){
    final titles=['Formato','Equipos',if(manual)'Enfrentamientos','Revisión'];final p=preview;
    final permitted=step==0||p?.canContinue==true||manual&&step==1&&manualCount==0&&p?.canConfigureBase==true||manual&&step==2&&p?.canConfigureBase==true&&p?.withinSupportedRange==true&&p?.fits!=false;
    return PopScope(canPop:!busy,child:Dialog(insetPadding:const EdgeInsets.all(16),child:SizedBox(width:860,height:MediaQuery.sizeOf(context).height-48,child:Column(children:[
      Padding(padding:const EdgeInsets.fromLTRB(24,16,12,8),child:Row(children:[Expanded(child:Text('Crear calendario',style:Theme.of(context).textTheme.headlineSmall)),
        IconButton(tooltip:'Cerrar asistente',onPressed:busy?null:()=>Navigator.pop(context),icon:const Icon(Icons.close))])),
      Padding(padding:const EdgeInsets.symmetric(horizontal:24),child:Text('Paso ${step+1} de ${titles.length} · ${titles[step]}')),
      Expanded(child:SingleChildScrollView(padding:const EdgeInsets.all(24),child:Column(crossAxisAlignment:CrossAxisAlignment.stretch,children:[
        if(widget.seasonName!=null)Padding(padding:const EdgeInsets.only(bottom:12),child:Text(widget.seasonName!,style:Theme.of(context).textTheme.titleMedium)),
        banner(context),const SizedBox(height:16),
        if(step==0)...[
          Text('¿Cómo se crean los enfrentamientos?',style:Theme.of(context).textTheme.titleLarge),
          for(final item in const [['SINGLE','Una vuelta','Un encuentro por pareja.'],['DOUBLE','Ida y vuelta','Dos encuentros por pareja.'],['MANUAL','Los introduzco yo','Añade o importa tus jornadas.']])
            Card(child:RadioListTile<String>(value:item[0],groupValue:mode,onChanged:busy?null:(v)=>setState((){mode=v!;changed();}),title:Text(item[1]),subtitle:Text(item[2]))),
        ],
        if(step==1)...[
          Text('${selected.length} equipos seleccionados',style:Theme.of(context).textTheme.titleLarge),
          if(selected.length<2||selected.length>64)Text(selectionError()??'',style:TextStyle(color:Theme.of(context).colorScheme.error)),
          if(selected.length.isOdd)const Text('Un equipo descansará en cada jornada.'),const SizedBox(height:12),
          TextField(enabled:!busy,decoration:const InputDecoration(labelText:'Buscar equipo',prefixIcon:Icon(Icons.search),border:OutlineInputBorder()),onChanged:(v)=>setState(()=>filter=v)),
          Wrap(spacing:8,children:[TextButton(onPressed:busy?null:()=>setState((){selected.addAll(teams.map((t)=>t['id'] as String));changed();}),child:const Text('Seleccionar todos')),
            TextButton(onPressed:busy?null:()=>setState((){selected.clear();home=null;away=null;changed();}),child:const Text('Quitar selección'))]),
          for(final t in teams.where((t)=>(t['name'] as String).toLowerCase().contains(filter.toLowerCase())))CheckboxListTile(key:ValueKey('team-${t['id']}'),title:Text(t['name'] as String),
            value:selected.contains(t['id']),onChanged:busy?null:(v)=>setState((){if(v==true){selected.add(t['id'] as String);}else{selected.remove(t['id']);}home=null;away=null;changed();})),
        ],
        if(manual&&step==2)...[
          const Text('Añade los partidos. Si hay equipos impares, el único equipo sin partido descansará.'),
          TextField(controller:round,enabled:!busy,keyboardType:TextInputType.number,decoration:const InputDecoration(labelText:'Jornada')),
          DropdownButtonFormField<String>(value:home,isExpanded:true,decoration:const InputDecoration(labelText:'Local'),items:participants.map((t)=>DropdownMenuItem(value:t['id'] as String,child:Text(t['name'] as String))).toList(),onChanged:busy?null:(v)=>setState(()=>home=v)),
          DropdownButtonFormField<String>(value:away,isExpanded:true,decoration:const InputDecoration(labelText:'Visitante'),items:participants.map((t)=>DropdownMenuItem(value:t['id'] as String,child:Text(t['name'] as String))).toList(),onChanged:busy?null:(v)=>setState(()=>away=v)),
          OutlinedButton.icon(onPressed:busy?null:add,icon:const Icon(Icons.add),label:const Text('Añadir partido')),
          ...matches.map((m)=>ListTile(title:Text('${name(m['homeTeamId'] as String)} — ${name(m['awayTeamId'] as String)}'),subtitle:Text('Jornada ${m['round']}'),trailing:IconButton(tooltip:'Quitar partido',onPressed:busy?null:()=>setState((){matches.remove(m);changed();}),icon:const Icon(Icons.delete_outline)))),
          ExpansionTile(title:const Text('Importar plantilla (opcional)'),children:[
            const Text('Cabecera: round,homeTeamId,awayTeamId. Sustituye los cruces de este asistente, aún sin guardar.'),
            TextField(controller:csv,enabled:!busy,minLines:4,maxLines:8,decoration:const InputDecoration(labelText:'Contenido CSV',border:OutlineInputBorder())),
            TextButton(onPressed:busy?null:importCsv,child:const Text('Validar e importar')),
            ...participants.map((t)=>ListTile(title:Text(t['name'] as String),subtitle:SelectableText(t['id'] as String))),
          ]),
        ],
        if(step==reviewStep)...[
          Text('Revisa el calendario',style:Theme.of(context).textTheme.titleLarge),
          if(p?.requiredRounds!=null)Text('${selected.length} equipos · ${p!.requiredRounds} jornadas · ${selected.length~/2} ${selected.length~/2==1?'partido':'partidos'} por jornada'),
          const Text('La compatibilidad de campos y preferencias se comprobará al asignar horarios. Que las jornadas quepan en las fechas no garantiza esos horarios.'),
          if(selected.length.isOdd)const Text('Un equipo descansará en cada jornada.'),
          const Text('Esta versión no permite cambiar participantes y formato después de crear el calendario.'),
          for(final t in participants)ListTile(dense:true,leading:const Icon(Icons.check,size:18),title:Text(t['name'] as String)),
          if(manual&&manualRounds!=null)...List.generate(manualRounds!.length,(i)=>ExpansionTile(title:Text('Jornada ${i+1}'),children:[
            ...(manualRounds![i]['games'] as List).map((g)=>ListTile(title:Text('${name(g['homeTeamId'] as String)} — ${name(g['awayTeamId'] as String)}'))),
            if(manualRounds![i]['byeTeamId']!=null)ListTile(title:Text('Descansa: ${name(manualRounds![i]['byeTeamId'] as String)}')),
          ])),
        ],
        if(error!=null||sourceError!=null)Padding(padding:const EdgeInsets.only(top:16),child:Text(error??sourceError!,style:TextStyle(color:Theme.of(context).colorScheme.error))),
      ]))),
      const Divider(height:1),Padding(padding:const EdgeInsets.all(16),child:Row(children:[
        TextButton(onPressed:busy?null:step==0?()=>Navigator.pop(context):()=>setState((){step--;error=null;}),child:Text(step==0?'Cancelar':'Anterior')),
        const Spacer(),FilledButton(onPressed:busy||checking||sourceError!=null||!permitted?null:step==reviewStep?finish:next,
          child:Text(extending?'Guardando fecha…':checking||submitting?'Comprobando…':step==reviewStep?'Crear calendario':step==0?'Elegir equipos':'Continuar')),
      ])),
    ]))));
  }
}
