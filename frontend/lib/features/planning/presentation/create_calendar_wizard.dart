import 'package:flutter/material.dart';
import '../domain/planning_view.dart';
import '../data/calendar_capacity_api.dart';
import 'configure_plan_dialog.dart';
class CreateCalendarWizard extends StatelessWidget {
  final PlanningView plan;
  final CalendarCapacityApi? capacityApi;
  final VoidCallback? onSeasonDatesChanged;
  const CreateCalendarWizard({super.key,required this.plan,this.capacityApi,this.onSeasonDatesChanged});
  @override Widget build(BuildContext context)=>ConfigurePlanDialog(
    seasonId:plan.seasonId,seasonName:plan.name,startDate:plan.start,endDate:plan.end,
    capacityApi:capacityApi,onSeasonDatesChanged:onSeasonDatesChanged,
    teams:plan.teams.map((t)=><String,dynamic>{'id':t.id,'name':t.name}).toList());
}
