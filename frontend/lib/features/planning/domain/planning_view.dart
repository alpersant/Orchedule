import 'team_name_order.dart';
List<Map<String,dynamic>> planningRows(dynamic value){
  if(value is! List)throw const FormatException('Se esperaba una lista');
  return value.map((e)=>Map<String,dynamic>.from(e as Map)).toList(growable:false);
}
class PlanningTeam {final String id,name;const PlanningTeam(this.id,this.name);}
class PlanningIssue {
  final String code,detail;final int total,dates,compatible,rotation,free;
  const PlanningIssue({required this.code,required this.detail,required this.total,required this.dates,required this.compatible,required this.rotation,required this.free});
  factory PlanningIssue.fromJson(Map<String,dynamic> j)=>PlanningIssue(code:j['code'] as String,detail:j['detail'] as String,
    total:j['total_slots'] as int,dates:j['date_slots'] as int,compatible:j['compatible_slots'] as int,rotation:j['rotation_slots'] as int,free:j['free_slots'] as int);
}
class PlanningRound {
  final String id,status;final int number,week,version,assignedCount,pendingCount;
  final String? byeId,byeName;final bool historyProvisional,earlierIncomplete,searchLimited;
  const PlanningRound({required this.id,required this.status,required this.number,required this.week,required this.version,this.byeId,this.byeName,
    this.assignedCount=0,this.pendingCount=0,this.historyProvisional=false,this.earlierIncomplete=false,this.searchLimited=false});
  factory PlanningRound.fromJson(Map<String,dynamic> j,{int? assignedFallback,int? pendingFallback})=>PlanningRound(
    id:j['id'] as String,status:j['status'] as String,number:j['round_number'] as int,week:j['week_number'] as int,version:j['version'] as int,
    byeId:j['bye_team_id'] as String?,byeName:j['bye_name'] as String?,assignedCount:j['assigned_count'] as int? ?? assignedFallback??0,
    pendingCount:j['pending_count'] as int? ?? pendingFallback??0,historyProvisional:j['history_provisional']==true,
    earlierIncomplete:j['earlier_incomplete']==true,searchLimited:j['search_limited']==true);
  bool get partial=>status=='DRAFT'&&pendingCount>0;
  bool get canConfirm=>status=='DRAFT'&&pendingCount==0&&assignedCount>0&&!earlierIncomplete;
  String get label=>partial?'Horarios incompletos':const {'UNSCHEDULED':'Sin planificar','DRAFT':'Pendiente de confirmar','CONFIRMED':'Confirmada','CANCELLED':'Cancelada'}[status]??status;
}
class PlanningGame {
  final bool deferred,manualAssignment,confirmed;final String? deferredReason;
  final String id,homeId,awayId,home,away;final int round;final String? field,fieldId,venue;final DateTime? localStart;final PlanningIssue? issue;
  const PlanningGame({this.deferred=false,this.deferredReason,this.manualAssignment=false,this.confirmed=false,required this.id,required this.homeId,required this.awayId,required this.home,required this.away,required this.round,this.field,this.fieldId,this.venue,this.localStart,this.issue});
  bool get scheduled=>localStart!=null&&(fieldId!=null||field!=null);
  factory PlanningGame.fromJson(Map<String,dynamic> j)=>PlanningGame(manualAssignment:j['manual_assignment']==true,confirmed:j['confirmation_status']=='CONFIRMED',deferred:j['deferred']==true,deferredReason:j['deferred_reason'] as String?,id:j['id'] as String,homeId:j['home_team_id'] as String,awayId:j['away_team_id'] as String,
    home:j['home_name'] as String,away:j['away_name'] as String,round:j['round_number'] as int,field:j['field_name'] as String?,fieldId:j['field_id'] as String?,venue:j['venue_name'] as String?,
    localStart:j['local_start']==null?null:DateTime.parse(j['local_start'] as String),issue:j['planning_issue']==null?null:PlanningIssue.fromJson(Map<String,dynamic>.from(j['planning_issue'] as Map)));
}
class PlanningView {
  final String seasonId,name,start,end;final bool canManage;final String? mode;
  final List<PlanningTeam> teams;final List<PlanningRound> rounds;final List<PlanningGame> games;
  final List<Map<String,dynamic>> legacy,legacyAssignments,fairness,audit;
  const PlanningView({required this.seasonId,required this.name,required this.start,required this.end,required this.canManage,required this.mode,required this.teams,
    required this.rounds,required this.games,required this.legacy,required this.legacyAssignments,required this.fairness,required this.audit});
  factory PlanningView.fromJson(Map<String,dynamic> j){
    final config=planningRows(j['configuration']),games=planningRows(j['games']).map(PlanningGame.fromJson).toList();
    final rounds=planningRows(j['rounds']).map((r){final group=games.where((g)=>g.round==r['round_number']).toList();
      return PlanningRound.fromJson(r,assignedFallback:group.where((g)=>g.scheduled).length,pendingFallback:group.where((g)=>!g.scheduled).length);}).toList();
    return PlanningView(seasonId:j['seasonId'] as String,name:j['seasonName'] as String,start:j['startDate'] as String,end:j['endDate'] as String,
      canManage:j['canManage'] as bool,mode:config.isEmpty?null:config.first['mode'] as String,
      teams:orderedTeams(planningRows(j['teams'])).map((t)=>PlanningTeam(t['id'] as String,t['name'] as String)).toList(),rounds:rounds,games:games,
      legacy:planningRows(j['legacyRounds']),legacyAssignments:planningRows(j['legacyAssignments']),fairness:planningRows(j['fairness']),audit:planningRows(j['audit']));
  }
  bool get configured=>mode!=null;
  List<PlanningTeam> get participants{
    if(!configured)return [...teams]..sort((a,b)=>compareTeamNames(a.name,b.name));
    final ids=<String>{...games.map((g)=>g.homeId),...games.map((g)=>g.awayId),...rounds.map((r)=>r.byeId).whereType<String>()};
    final names=<String,String>{for(final t in teams)t.id:t.name};for(final g in games){names[g.homeId]=g.home;names[g.awayId]=g.away;}
    for(final r in rounds){if(r.byeId!=null)names[r.byeId!]=r.byeName??'Equipo no disponible';}
    return ids.map((id)=>PlanningTeam(id,names[id]??'Equipo no disponible')).toList()..sort((a,b){final order=compareTeamNames(a.name,b.name);return order==0?a.id.compareTo(b.id):order;});
  }
}
String planningDate(DateTime d)=>'${d.day.toString().padLeft(2,'0')}/${d.month.toString().padLeft(2,'0')}/${d.year}';
DateTime planningDateOnly(String value){final d=DateTime.parse(value);return DateTime.utc(d.year,d.month,d.day);}
String planningWeekRange(PlanningView p,int week){
  final start=planningDateOnly(p.start),end=planningDateOnly(p.end),monday=planningDateOnly(p.start).subtract(Duration(days:planningDateOnly(p.start).weekday-1)).add(Duration(days:(week-1)*7));
  final sunday=monday.add(const Duration(days:6));return '${planningDate(monday.isBefore(start)?start:monday)} — ${planningDate(sunday.isAfter(end)?end:sunday)}';
}
String planningDay(DateTime d)=>const ['Lunes','Martes','Miércoles','Jueves','Viernes','Sábado','Domingo'][d.weekday-1];
String planningHour(DateTime d)=>'${d.hour.toString().padLeft(2,'0')}:${d.minute.toString().padLeft(2,'0')}';


