import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../../planning/data/planning_api.dart';
import '../../../planning/domain/planning_view.dart';
import '../../../planning/presentation/planning_controller.dart';
import '../../../planning/presentation/create_calendar_wizard.dart';
import '../../../planning/presentation/weekly_fields_panel.dart';
import '../../../planning/presentation/round_plan_summary.dart';
import '../../../planning/presentation/pending_game_card.dart';
import '../../../planning/presentation/planning_audit_tile.dart';

final _weekAvailability=FutureProvider.autoDispose.family<List<Map<String,dynamic>>,({String season,int week})>((ref,key)=>ref.watch(planningApiProvider).availability(key.season,key.week));
class ScheduleRoundPage extends ConsumerStatefulWidget {
  final String seasonId;
  const ScheduleRoundPage({super.key,required this.seasonId});
  @override ConsumerState<ScheduleRoundPage> createState()=>_ScheduleRoundPageState();
}
class _ScheduleRoundPageState extends ConsumerState<ScheduleRoundPage>{
  int tab=0,number=1;
  bool fieldsDirty=false,fieldsBusy=false,dialogOpen=false;
  String filter='';
  bool get working {final s=ref.read(planningControllerProvider(widget.seasonId));return s.loading||s.saving||fieldsBusy||dialogOpen;}
  Future<bool> discard() async {
    if(!fieldsDirty)return true;
    final ok=await showDialog<bool>(context:context,builder:(ctx)=>AlertDialog(title:const Text('Hay cambios sin guardar'),content:const Text('Guarda los campos elegidos o descarta los cambios antes de salir.'),actions:[
      TextButton(onPressed:()=>Navigator.pop(ctx,false),child:const Text('Seguir editando')),FilledButton(onPressed:()=>Navigator.pop(ctx,true),child:const Text('Descartar cambios'))]));
    if(ok==true&&mounted){setState(()=>fieldsDirty=false);return true;}return false;
  }
  Future<void> switchTab(int v) async {if(working||v==tab)return;if(!await discard()||!mounted)return;setState(()=>tab=v);}
  Future<void> switchRound(int v) async {if(working||v==number)return;if(!await discard()||!mounted)return;setState((){number=v;filter='';});}
  Future<void> back() async {if(working||!await discard()||!mounted)return;context.go('/seasons');}
  Future<void> refresh() async {
    if(working||!await discard()||!mounted)return;
    ref.invalidate(_weekAvailability);await ref.read(planningControllerProvider(widget.seasonId).notifier).load();
  }
  Future<void> create(PlanningView plan) async {
    if(working)return;
    if(plan.seasonId!=widget.seasonId)return;
    var datesChanged=false;setState(()=>dialogOpen=true);
    final body=await showDialog<Map<String,dynamic>>(context:context,barrierDismissible:false,builder:(_)=>CreateCalendarWizard(
      plan:plan,onSeasonDatesChanged:()=>datesChanged=true));
    if(!mounted)return;setState(()=>dialogOpen=false);
    final controller=ref.read(planningControllerProvider(widget.seasonId).notifier);
    if(body!=null){
      final ok=await controller.configure(body);
      if(ok&&mounted)setState((){tab=0;number=1;});
    }else if(datesChanged){await controller.load();}
  }
  Future<void> act(PlanningRound round,String action) async {
    if(working)return;
    if(action!='schedule' && action!='resolve'){
      setState(()=>dialogOpen=true);
      final text=const {'confirm':'Publicarás los horarios. Revisa antes los partidos y campos.',
        'cancel':'Los partidos quedarán cancelados. Se conservarán los enfrentamientos y descansos.',
        'regenerate':'Se sustituirán los horarios. No cambian rivales ni descansos.'}[action]!;
      final ok=await showDialog<bool>(context:context,builder:(ctx)=>AlertDialog(title:Text('Jornada ${round.number}'),content:Text(text),actions:[
        TextButton(onPressed:()=>Navigator.pop(ctx,false),child:const Text('Volver')),
        FilledButton(onPressed:()=>Navigator.pop(ctx,true),child:Text(const {'confirm':'Confirmar jornada','cancel':'Cancelar jornada','regenerate':'Recalcular horarios'}[action]!))]));
      if(!mounted)return;setState(()=>dialogOpen=false);if(ok!=true)return;
    }
    await ref.read(planningControllerProvider(widget.seasonId).notifier).action(round,action);
  }
  Future<void> setPending(PlanningGame game) async {
    if(working)return;
    setState(()=>dialogOpen=true);
    bool? confirmed;
    try {
      confirmed=await showDialog<bool>(context:context,builder:(ctx)=>AlertDialog(
        title:Text(game.deferred?'Volver a resolver este partido':'Dejar este partido pendiente'),
        content:Text(game.deferred?'Se incluirá en la próxima búsqueda de horarios.':'Se conservará el enfrentamiento sin horario. No se incluirá en los cálculos automáticos hasta que decidas retomarlo. Puedes continuar con otras jornadas.'),
        actions:[TextButton(onPressed:()=>Navigator.pop(ctx,false),child:const Text('Cancelar')),
          FilledButton(onPressed:()=>Navigator.pop(ctx,true),child:const Text('Confirmar'))]));
    } finally {if(mounted)setState(()=>dialogOpen=false);}
    if(confirmed!=true||!mounted)return;
    final api=ref.read(planningApiProvider);
    await ref.read(planningControllerProvider(widget.seasonId).notifier).execute(
      () async {await api.dio.put<dynamic>('/v1/planning/seasons/${Uri.encodeComponent(widget.seasonId)}/games/${Uri.encodeComponent(game.id)}/pending',
        data:{'pending':!game.deferred,'reason':game.deferred?null:'Pendiente por decisión del organizador'});},
      game.deferred?'El partido vuelve a estar disponible para resolver.':'Partido dejado pendiente. Puedes continuar con otras jornadas.');
  }
  Future<void> oldReservations(PlanningView plan,Map<String,dynamic> old) async {
    if(working)return;setState(()=>dialogOpen=true);
    final next=await showDialog<bool>(context:context,builder:(ctx)=>AlertDialog(title:Text('Reservas anteriores · semana ${old['week_number']}'),scrollable:true,
      content:SizedBox(width:650,child:Column(crossAxisAlignment:CrossAxisAlignment.start,children:[
        const Text('Son reservas individuales, no enfrentamientos. Para reutilizar la semana debes cancelarlas explícitamente. No se borran ni se inventan rivales.'),
        ...plan.legacyAssignments.where((a)=>a['round_id']==old['id']).map((a)=>ListTile(title:Text(a['team_name'] as String? ?? 'Equipo no disponible'),subtitle:Text('${a['day']} · ${a['hour']} · ${a['field_name']}'))),
      ])),actions:[TextButton(onPressed:()=>Navigator.pop(ctx,false),child:const Text('Cerrar')),
        if(plan.canManage&&old['status']!='CANCELLED')FilledButton(onPressed:()=>Navigator.pop(ctx,true),child:const Text('Revisar cancelación'))]));
    if(!mounted)return;setState(()=>dialogOpen=false);if(next!=true)return;
    setState(()=>dialogOpen=true);
    final ok=await showDialog<bool>(context:context,builder:(ctx)=>AlertDialog(title:const Text('¿Cancelar las reservas anteriores?'),content:const Text('Dejarán de estar activas. Se conserva su histórico.'),actions:[
      TextButton(onPressed:()=>Navigator.pop(ctx,false),child:const Text('Conservar reservas')),FilledButton(onPressed:()=>Navigator.pop(ctx,true),child:const Text('Cancelar reservas'))]));
    if(!mounted)return;setState(()=>dialogOpen=false);
    if(ok==true)await ref.read(planningControllerProvider(widget.seasonId).notifier).cancelLegacy(old['id'] as String);
  }
  @override Widget build(BuildContext context){
    final state=ref.watch(planningControllerProvider(widget.seasonId));final plan=state.plan;
    final busy=state.loading||state.saving||fieldsBusy||dialogOpen;
    final choices=plan?.rounds.where((r)=>r.number==number).toList();final round=choices==null||choices.isEmpty?null:choices.first;
    final week=round?.week??1;
    final available=ref.watch(_weekAvailability((season:widget.seasonId,week:week)));
    final enabled=available.valueOrNull?.where((a)=>a['enabled']==true).length;
    final rawError=state.error;
    final error=rawError==null?null:plan==null?rawError:plan.participants.fold<String>(rawError,(message,team)=>message.replaceAll(team.id,team.name));
    return PopScope(canPop:!fieldsDirty&&!busy,onPopInvokedWithResult:(didPop,result){if(!didPop&&!busy)back();},child:Scaffold(
      appBar:AppBar(title:const Text('Calendario de temporada'),leading:IconButton(tooltip:'Volver a temporadas',onPressed:busy?null:back,icon:const Icon(Icons.arrow_back)),
        actions:[IconButton(tooltip:'Actualizar datos',onPressed:busy?null:refresh,icon:const Icon(Icons.refresh))]),
      body:Align(alignment:Alignment.topCenter,child:ConstrainedBox(constraints:const BoxConstraints(maxWidth:1120),child:ListView(
        padding:EdgeInsets.all(MediaQuery.sizeOf(context).width<600?16:28),children:[
          if(state.loading||state.saving)const LinearProgressIndicator(),
          if(error!=null)Card(color:Theme.of(context).colorScheme.errorContainer,child:Padding(padding:const EdgeInsets.all(16),child:Column(crossAxisAlignment:CrossAxisAlignment.start,children:[
            Text(error),TextButton(onPressed:busy?null:refresh,child:const Text('Actualizar y reintentar'))]))),
          if(state.notice!=null)Padding(padding:const EdgeInsets.symmetric(vertical:12),child:Text(state.notice!)),
          if(plan!=null)...[
            Text(plan.name,style:Theme.of(context).textTheme.headlineMedium),const SizedBox(height:8),
            Text('${capacityDateLabel(plan.start)} — ${capacityDateLabel(plan.end)}'),
            if(plan.configured)Text('${plan.participants.length} equipos · ${plan.rounds.length} jornadas'),
            const SizedBox(height:20),Wrap(spacing:8,runSpacing:8,children:[
              for(final entry in const [(0,'Calendario',Icons.calendar_month),(1,'Equipos',Icons.groups_outlined),(2,'Campos y horarios',Icons.stadium_outlined)])ChoiceChip(
                avatar:Icon(entry.$3,size:18),label:Text(entry.$2),selected:tab==entry.$1,onSelected:busy?null:(_)=>switchTab(entry.$1)),
            ]),const SizedBox(height:24),
            if(!plan.canManage)const Text('Vista de consulta: solo el organizador puede modificar el calendario.'),
            if(tab==0)...calendar(context,plan,round,enabled,available.hasError,available.isLoading,busy),
            if(tab==1)...[
              Text(plan.configured?'Equipos participantes':'Equipos disponibles',style:Theme.of(context).textTheme.headlineSmall),
              if(plan.participants.length.isOdd)const Text('Un equipo descansará en cada jornada.'),
              for(final t in plan.participants)Card(child:ListTile(leading:const Icon(Icons.groups_outlined),title:Text(t.name),
                subtitle:plan.configured?Text('Descansos: ${plan.rounds.where((r)=>r.byeId==t.id).length}'):null)),
              OutlinedButton(onPressed:busy?null:()=>context.push('/teams'),child:const Text('Abrir gestión de equipos')),
            ],
            if(tab==2)...[
              if(plan.rounds.isNotEmpty)...[selector(plan,round,busy),const SizedBox(height:20)],
              WeeklyFieldsPanel(key:ValueKey('${widget.seasonId}-$week'),seasonId:widget.seasonId,week:week,range:planningWeekRange(plan,week),
                canManage:plan.canManage,locked:round?.status=='CONFIRMED',onDirty:(v){if(mounted)setState(()=>fieldsDirty=v);},
                onBusy:(v){if(mounted)setState(()=>fieldsBusy=v);},onSaved:()=>ref.invalidate(_weekAvailability((season:widget.seasonId,week:week))),onBackToCalendar:()=>switchTab(0)),
            ],
          ],const SizedBox(height:40),
        ],
      ))),
    ));
  }
  String capacityDateLabel(String value)=>planningDate(planningDateOnly(value));
  Widget selector(PlanningView plan,PlanningRound? round,bool busy)=>Row(children:[
    IconButton(tooltip:'Jornada anterior',onPressed:busy||number<=1?null:()=>switchRound(number-1),icon:const Icon(Icons.chevron_left)),
    Expanded(child:InputDecorator(decoration:const InputDecoration(labelText:'Jornada',border:OutlineInputBorder()),child:DropdownButtonHideUnderline(child:DropdownButton<int>(value:round?.number,isExpanded:true,
      items:plan.rounds.map((r)=>DropdownMenuItem(value:r.number,child:Text('Jornada ${r.number} · ${r.label}',overflow:TextOverflow.ellipsis))).toList(),onChanged:busy?null:(v){if(v!=null)switchRound(v);})))),
    IconButton(tooltip:'Jornada siguiente',onPressed:busy||number>=plan.rounds.length?null:()=>switchRound(number+1),icon:const Icon(Icons.chevron_right)),
  ]);
  List<Widget> calendar(BuildContext context,PlanningView plan,PlanningRound? round,int? enabled,bool availabilityError,bool loading,bool busy){
    if(!plan.configured)return [Card(child:Padding(padding:const EdgeInsets.all(24),child:Column(crossAxisAlignment:CrossAxisAlignment.start,children:[
      Text('Vamos a crear tu calendario',style:Theme.of(context).textTheme.headlineSmall),const SizedBox(height:12),
      const Text('Elige formato y equipos. Comprobaremos las fechas antes de crear enfrentamientos.'),const SizedBox(height:20),
      FilledButton.icon(onPressed:busy||!plan.canManage?null:()=>create(plan),icon:const Icon(Icons.add),label:const Text('Crear calendario')),
    ]))),...legacySection(plan,busy)];
    if(round==null)return [const Text('No se encuentra la jornada. Actualiza el calendario.')];
    final games=plan.games.where((g)=>g.round==round.number&&'${g.home} ${g.away} ${g.field??''}'.toLowerCase().contains(filter.toLowerCase())).toList();
    const later=false;
    final old=plan.legacy.where((r)=>r['week_number']==round.week&&r['status']!='CANCELLED').toList();
    return [selector(plan,round,busy),const SizedBox(height:16),Wrap(spacing:12,crossAxisAlignment:WrapCrossAlignment.center,children:[Chip(label:Text(round.label)),Text('Semana ${round.week} · ${planningWeekRange(plan,round.week)}')]),
      if(old.isNotEmpty)Card(child:ListTile(title:const Text('Reservas anteriores activas'),
        trailing:TextButton(onPressed:busy?null:()=>oldReservations(plan,old.first),child:const Text('Revisar')))),
      RoundPlanSummary(round:round,busy:busy,canManage:plan.canManage,
        onSchedule:()=>act(round,'schedule'),onResolve:()=>act(round,'resolve'),
        onReorganize:()=>act(round,'regenerate'),onConfirm:()=>act(round,'confirm'),onFields:()=>switchTab(2)),
      if(number<plan.rounds.length)Align(alignment:Alignment.centerRight,child:OutlinedButton.icon(
        onPressed:busy?null:()=>switchRound(number+1),icon:const Icon(Icons.arrow_forward),label:const Text('Continuar a la siguiente jornada'))),
      if(round.byeName!=null)Card(child:ListTile(leading:const Icon(Icons.hotel_outlined),title:Text('Descansa: ${round.byeName}'),subtitle:const Text('No tiene partido en esta jornada.'))),
      const SizedBox(height:20),Row(children:[Expanded(child:Text('Partidos de la jornada',style:Theme.of(context).textTheme.titleLarge)),
        if(plan.canManage&&round.status!='CANCELLED')PopupMenuButton<String>(enabled:!busy&&!later,tooltip:'Más acciones',onSelected:(v)=>act(round,v),itemBuilder:(_)=>[
          if(round.status=='DRAFT')const PopupMenuItem(value:'regenerate',child:Text('Recalcular horarios')),const PopupMenuItem(value:'cancel',child:Text('Cancelar jornada'))])]),
      const SizedBox(height:12),TextField(key:ValueKey('filter-$number'),onChanged:(v)=>setState(()=>filter=v),decoration:const InputDecoration(labelText:'Buscar equipo o campo',prefixIcon:Icon(Icons.search),border:OutlineInputBorder())),
      const SizedBox(height:16),if(games.isEmpty)const Text('No hay partidos para esta búsqueda.'),
      ...List.generate(games.length,(i){final g=games[i];final date=g.localStart;
        if(!g.scheduled)return Column(crossAxisAlignment:CrossAxisAlignment.stretch,children:[
          PendingGameCard(game:g,disabled:busy,onFields:()=>switchTab(2),onTeams:()=>switchTab(1),onDates:()=>switchTab(2)),
          if(plan.canManage&&round.status!='CANCELLED')Align(alignment:Alignment.centerRight,child:TextButton.icon(
            onPressed:busy?null:()=>setPending(g),icon:Icon(g.deferred?Icons.play_arrow:Icons.pause),
            label:Text(g.deferred?'Retomar y resolver este partido':'Dejar pendiente y continuar'))),
        ]);
        final heading=date!=null&&(i==0||games[i-1].localStart==null||planningDate(games[i-1].localStart!)!=planningDate(date));
        return Column(crossAxisAlignment:CrossAxisAlignment.stretch,children:[
          if(heading&&date!=null)Padding(padding:const EdgeInsets.symmetric(vertical:12),child:Text('${planningDay(date)} ${planningDate(date)}',style:Theme.of(context).textTheme.titleMedium)),
          Card(child:Padding(padding:const EdgeInsets.all(18),child:LayoutBuilder(builder:(context,limits){
            final names=Column(crossAxisAlignment:CrossAxisAlignment.start,children:[Text('${g.home} — ${g.away}',style:Theme.of(context).textTheme.titleMedium),const SizedBox(height:8),Text(g.field==null?'Campo pendiente':'${g.field} · ${g.venue??''}')]);
            final time=Text(date==null?'Sin horario':planningHour(date),style:Theme.of(context).textTheme.titleMedium);
            return limits.maxWidth<550?Column(crossAxisAlignment:CrossAxisAlignment.start,children:[time,const SizedBox(height:12),names]):Row(crossAxisAlignment:CrossAxisAlignment.start,children:[SizedBox(width:110,child:time),Expanded(child:names)]);
          }))),
        ]);
      }),const SizedBox(height:24),
      ExpansionTile(title:const Text('Distribución y registro de cambios'),children:[
        ...plan.fairness.map((a)=>ListTile(title:Text('${a['teamName']} · ${a['games']} partidos'),subtitle:Text('Días: ${a['days']}\nHoras: ${a['hours']}\nRachas: día ${a['maxDayStreak']} · hora ${a['maxHourStreak']}'))),
        ...plan.audit.take(20).map((a)=>PlanningAuditTile(entry:a)),
      ]),...legacySection(plan,busy),
    ];
  }
  List<Widget> legacySection(PlanningView plan,bool busy)=>plan.legacy.isEmpty?[]:[const SizedBox(height:16),ExpansionTile(title:const Text('Asignaciones anteriores'),children:[
    for(final old in plan.legacy)ListTile(title:Text('Semana ${old['week_number']}'),subtitle:Text(old['status']=='CANCELLED'?'Canceladas':'Reservas anteriores activas'),trailing:TextButton(onPressed:busy?null:()=>oldReservations(plan,old),child:const Text('Consultar'))),
  ])];
}

