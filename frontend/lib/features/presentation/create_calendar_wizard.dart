import 'package:flutter/material.dart';
import '../domain/planning_view.dart';
import '../domain/manual_fixtures.dart';

class CreateCalendarWizard extends StatefulWidget {
  final PlanningView plan;
  const CreateCalendarWizard({super.key,required this.plan});
  @override
  State<CreateCalendarWizard> createState()=>_CreateCalendarWizardState();
}
class _CreateCalendarWizardState extends State<CreateCalendarWizard>{
  String mode='SINGLE',filter='';
  int step=0;
  final selected=<String>{};
  final matches=<Map<String,dynamic>>[];
  final roundInput=TextEditingController(text:'1');
  final csv=TextEditingController(text:'round,homeTeamId,awayTeamId\n');
  String? home,away,error;
  List<Map<String,dynamic>>? parsed;
  @override
  void initState(){super.initState();selected.addAll(widget.plan.teams.map((t)=>t.id));}
  @override
  void dispose(){roundInput.dispose();csv.dispose();super.dispose();}
  bool get manual=>mode=='MANUAL';
  int get reviewStep=>manual?3:2;
  String teamName(String id)=>widget.plan.teams.firstWhere((t)=>t.id==id).name;
  String get modeName=>const {'SINGLE':'Liga a una vuelta','DOUBLE':'Liga de ida y vuelta','MANUAL':'Enfrentamientos del organizador'}[mode]!;
  int get totalRounds=>manual?(parsed?.length??0):(selected.length.isOdd?selected.length:selected.length-1)*(mode=='DOUBLE'?2:1);
  void validateManual(){
    final body='round,homeTeamId,awayTeamId\n${matches.map((m)=>"${m['round']},${m['homeTeamId']},${m['awayTeamId']}").join('\n')}';
    parsed=ManualFixtures.parse(body,selected);
  }
  void next(){
    setState(()=>error=null);
    if(step==1&&(selected.length<2||selected.length>64)){setState(()=>error='Selecciona entre 2 y 64 equipos.');return;}
    if(manual&&step==2){
      try{validateManual();}on FormatException catch(e){setState(()=>error=e.message);return;}
    }
    setState(()=>step++);
  }
  void finish(){
    Navigator.pop(context,<String,dynamic>{'mode':mode,'teamIds':selected.toList(),if(manual)'rounds':parsed});
  }
  void addMatch(){
    final n=int.tryParse(roundInput.text);
    if(n==null||n<1||n>126||home==null||away==null||home==away){setState(()=>error='Elige una jornada de 1 a 126 y dos equipos distintos.');return;}
    if(matches.any((m)=>m['round']==n&&[m['homeTeamId'],m['awayTeamId']].any((id)=>id==home||id==away))){setState(()=>error='Uno de esos equipos ya juega en esta jornada.');return;}
    setState((){matches.add({'round':n,'homeTeamId':home,'awayTeamId':away});home=null;away=null;error=null;});
  }
  void importCsv(){
    try{
      final imported=ManualFixtures.parse(csv.text,selected);
      final converted=<Map<String,dynamic>>[];
      for(var i=0;i<imported.length;i++)for(final g in imported[i]['games'] as List)converted.add({'round':i+1,...Map<String,dynamic>.from(g as Map)});
      setState((){matches..clear()..addAll(converted);parsed=null;error=null;});
    }on FormatException catch(e){setState(()=>error=e.message);}
  }
  Future<void> close() async {
    if(step>0||matches.isNotEmpty){
      final leave=await showDialog<bool>(context:context,builder:(ctx)=>AlertDialog(title:const Text('¿Salir sin crear el calendario?'),
        content:const Text('No se ha guardado ningún cambio. Perderás esta selección.'),actions:[
          TextButton(onPressed:()=>Navigator.pop(ctx,false),child:const Text('Seguir editando')),
          FilledButton(onPressed:()=>Navigator.pop(ctx,true),child:const Text('Salir'))]));
      if(leave!=true||!mounted)return;
    }
    if(mounted)Navigator.pop(context);
  }
  @override
  Widget build(BuildContext context){
    final titles=['Formato','Equipos',if(manual)'Enfrentamientos','Revisión'];
    return Dialog(insetPadding:const EdgeInsets.all(16),child:SizedBox(width:860,height:MediaQuery.sizeOf(context).height-48,
      child:Column(children:[
        Padding(padding:const EdgeInsets.fromLTRB(24,16,12,8),child:Row(children:[
          Expanded(child:Text('Crear calendario',style:Theme.of(context).textTheme.headlineSmall)),
          IconButton(tooltip:'Salir de la configuración',onPressed:close,icon:const Icon(Icons.close))])),
        Padding(padding:const EdgeInsets.symmetric(horizontal:24),child:Column(crossAxisAlignment:CrossAxisAlignment.start,children:[
          Text('Paso ${step+1} de ${titles.length} · ${titles[step]}'),const SizedBox(height:8),
          LinearProgressIndicator(value:(step+1)/titles.length)])),
        Expanded(child:SingleChildScrollView(padding:const EdgeInsets.all(24),child:Column(crossAxisAlignment:CrossAxisAlignment.stretch,children:[
          if(step==0)...[
            Text('¿Cómo quieres crear los enfrentamientos?',style:Theme.of(context).textTheme.titleLarge),const SizedBox(height:16),
            for(final entry in const [
              ['SINGLE','Una vuelta','Cada equipo juega una vez contra los demás.'],
              ['DOUBLE','Ida y vuelta','Cada pareja juega dos veces, intercambiando local y visitante.'],
              ['MANUAL','Los introduzco yo','Añade los cruces por nombre o importa una plantilla. La aplicación asignará sus horarios.'],
            ])Card(color:mode==entry[0]?Theme.of(context).colorScheme.primaryContainer:null,child:RadioListTile<String>(
              value:entry[0],groupValue:mode,onChanged:(v)=>setState(()=>mode=v!),title:Text(entry[1]),subtitle:Text(entry[2]))),
          ],
          if(step==1)...[
            Text('¿Qué equipos participan?',style:Theme.of(context).textTheme.titleLarge),
            const SizedBox(height:8),const Text('Solo se muestran equipos activos con preferencias configuradas para la competición.'),
            const SizedBox(height:16),TextField(decoration:const InputDecoration(labelText:'Buscar equipo',prefixIcon:Icon(Icons.search),border:OutlineInputBorder()),onChanged:(v)=>setState(()=>filter=v)),
            const SizedBox(height:12),Text('${selected.length} equipos seleccionados'),
            Wrap(spacing:8,children:[TextButton(onPressed:()=>setState(()=>selected.addAll(widget.plan.teams.map((t)=>t.id))),child:const Text('Seleccionar todos')),
              TextButton(onPressed:()=>setState((){selected.clear();home=null;away=null;}),child:const Text('Quitar selección'))]),
            for(final t in widget.plan.teams.where((t)=>t.name.toLowerCase().contains(filter.toLowerCase())))CheckboxListTile(
              title:Text(t.name),value:selected.contains(t.id),onChanged:(v)=>setState((){if(v==true){selected.add(t.id);}else{selected.remove(t.id);}home=null;away=null;})),
            if(widget.plan.teams.isEmpty)const Text('No hay equipos disponibles. Configura primero sus preferencias en Equipos.'),
            if(selected.length.isOdd)const Padding(padding:EdgeInsets.only(top:12),child:Text('Al ser impares, un equipo descansará en cada jornada.')),
          ],
          if(manual&&step==2)...[
            Text('Añade los partidos de cada jornada',style:Theme.of(context).textTheme.titleLarge),const SizedBox(height:8),
            const Text('Cada equipo puede jugar una vez por jornada. Si son impares, el equipo que no tenga partido descansará.'),
            const SizedBox(height:16),TextField(controller:roundInput,keyboardType:TextInputType.number,decoration:const InputDecoration(labelText:'Jornada',border:OutlineInputBorder())),
            const SizedBox(height:12),DropdownButtonFormField<String>(value:home,isExpanded:true,decoration:const InputDecoration(labelText:'Equipo local'),
              items:selected.map((id)=>DropdownMenuItem(value:id,child:Text(teamName(id)))).toList(),onChanged:(v)=>setState(()=>home=v)),
            const SizedBox(height:12),DropdownButtonFormField<String>(value:away,isExpanded:true,decoration:const InputDecoration(labelText:'Equipo visitante'),
              items:selected.map((id)=>DropdownMenuItem(value:id,child:Text(teamName(id)))).toList(),onChanged:(v)=>setState(()=>away=v)),
            const SizedBox(height:12),OutlinedButton.icon(onPressed:addMatch,icon:const Icon(Icons.add),label:const Text('Añadir partido')),
            ...matches.map((m)=>Card(child:ListTile(title:Text('${teamName(m['homeTeamId'] as String)} — ${teamName(m['awayTeamId'] as String)}'),
              subtitle:Text('Jornada ${m['round']}'),trailing:IconButton(tooltip:'Quitar este partido',icon:const Icon(Icons.delete_outline),onPressed:()=>setState(()=>matches.remove(m)))))),
            ExpansionTile(title:const Text('Importar una plantilla existente'),subtitle:const Text('Opcional · pegar contenido CSV'),children:[
              const Text('Cabecera: round,homeTeamId,awayTeamId. Importar sustituye la lista de cruces de este asistente, todavía sin guardar.'),
              TextField(controller:csv,minLines:4,maxLines:8,decoration:const InputDecoration(labelText:'Contenido de la plantilla',border:OutlineInputBorder())),
              OutlinedButton(onPressed:importCsv,child:const Text('Validar e importar cruces')),
              ...widget.plan.teams.where((t)=>selected.contains(t.id)).map((t)=>ListTile(title:Text(t.name),subtitle:SelectableText(t.id))),
            ]),
          ],
          if(step==reviewStep)...[
            Text('Revisa antes de crear',style:Theme.of(context).textTheme.titleLarge),const SizedBox(height:16),
            Text(widget.plan.name,style:Theme.of(context).textTheme.titleMedium),
            Text('${planningDate(planningDateOnly(widget.plan.start))} — ${planningDate(planningDateOnly(widget.plan.end))}'),
            const SizedBox(height:16),Text(modeName),Text('${selected.length} equipos · $totalRounds jornadas · ${selected.length~/2} partidos por jornada'),
            if(selected.length.isOdd)const Text('Un equipo descansará en cada jornada.'),
            const SizedBox(height:16),const Text('Primero se crean los enfrentamientos. Después podrás asignar horarios, revisar y confirmar cada jornada.'),
            const SizedBox(height:12),const Text('Los participantes y el formato no se podrán cambiar desde esta versión después de crear el calendario.'),
            if(manual&&parsed!=null)...List.generate(parsed!.length,(i)=>ExpansionTile(title:Text('Jornada ${i+1}'),children:[
              ...(parsed![i]['games'] as List).map((g)=>ListTile(title:Text('${teamName(g['homeTeamId'] as String)} — ${teamName(g['awayTeamId'] as String)}'))),
              if(parsed![i]['byeTeamId']!=null)ListTile(leading:const Icon(Icons.hotel_outlined),title:Text('Descansa: ${teamName(parsed![i]['byeTeamId'] as String)}')),
            ])),
          ],
          if(error!=null)Padding(padding:const EdgeInsets.only(top:16),child:Text(error!,style:TextStyle(color:Theme.of(context).colorScheme.error))),
        ]))),
        const Divider(height:1),Padding(padding:const EdgeInsets.all(16),child:Row(children:[
          TextButton(onPressed:step==0?close:()=>setState((){step--;error=null;}),child:Text(step==0?'Cancelar':'Anterior')),
          const Spacer(),FilledButton(onPressed:step==reviewStep?finish:next,child:Text(step==reviewStep?'Crear calendario':'Continuar')),
        ])),
      ])));
  }
}
