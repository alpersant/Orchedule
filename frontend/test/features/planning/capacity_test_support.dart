import 'package:dio/dio.dart';
import '../../../lib/features/planning/data/calendar_capacity_api.dart';
import '../../../lib/features/planning/domain/planning_view.dart';
class FakeCalendarCapacityApi extends CalendarCapacityApi {
  FakeCalendarCapacityApi({this.weeks=40}):super(Dio());
  int weeks,checks=0;String start='2030-01-07';
  @override Future<CalendarCapacityPreview> check(String id,{required String mode,required List<String> teamIds,int? manualRoundCount}) async {
    checks++;final n=teamIds.length;
    final required=n<2?null:mode=='MANUAL'?(manualRoundCount==0?null:manualRoundCount):(n.isOdd?n:n-1)*(mode=='DOUBLE'?2:1);
    final date=DateTime.parse(start),end=DateTime.parse(start).add(Duration(days:weeks*7-1));
    final fits=required==null?null:required<=weeks,missing=required==null?null:required>weeks?required-weeks:0;
    return CalendarCapacityPreview(seasonId:id,seasonName:'Liga de prueba',startDate:start,endDate:capacityIso(end),selectedTeams:n,availableWeeks:weeks,
      requiredRounds:required,missingWeeks:missing,fits:fits,earliestEndDate:required==null?null:capacityIso(date.add(Duration(days:(required-1)*7))),
      suggestedEndDate:required==null?null:capacityIso(date.add(Duration(days:required*7-1))),alreadyConfigured:false,canConfigureBase:true,
      withinSupportedRange:required==null||required<=60,canContinue:fits==true&&(required??0)<=60,hasPastDates:false,
      message:fits==false?'Necesitas $required jornadas, pero la temporada contiene $weeks semanas. Faltan $missing semanas.':fits==true?'Las $required jornadas caben en $weeks semanas.':'Añade o selecciona los datos para comprobar.');
  }
  @override Future<void> extend(String id,{required String expectedStartDate,required String expectedEndDate,required String endDate}) async {
    weeks=DateTime.parse(endDate).difference(DateTime.parse(start)).inDays~/7+1;
  }
}
List<Map<String,dynamic>> capacityTeams()=>List.generate(24,(i)=>{'id':'team-${24-i}','name':'Equipo Test ${24-i}'});
PlanningView capacityPlan()=>PlanningView.fromJson({'seasonId':'season-1','seasonName':'Liga de prueba','startDate':'2030-01-07','endDate':'2031-12-31',
  'canManage':true,'configuration':[],'teams':capacityTeams(),'rounds':[],'games':[],'legacyRounds':[],'legacyAssignments':[],'fairness':[],'audit':[]});
