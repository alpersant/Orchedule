import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../domain/planning_view.dart';
import '../data/planning_api.dart';

class PlanningState {
  final PlanningView? plan;
  final bool loading,saving;
  final String? error,notice;
  const PlanningState({this.plan,this.loading=false,this.saving=false,this.error,this.notice});
}
final planningControllerProvider=StateNotifierProvider.autoDispose.family<PlanningController,PlanningState,String>((ref,id){
  final controller=PlanningController(ref.watch(planningApiProvider),id);
  Future.microtask(controller.load);
  return controller;
});
class PlanningController extends StateNotifier<PlanningState>{
  final PlanningApi api;
  final String id;
  bool alive=true;
  int request=0;
  PlanningController(this.api,this.id):super(const PlanningState(loading:true));
  Future<void> load() async {
    if(!alive||state.saving)return;
    final current=++request;
    state=PlanningState(plan:state.plan,loading:true);
    try {
      final plan=await api.view(id);
      if(alive&&current==request)state=PlanningState(plan:plan);
    }catch(e){if(alive&&current==request)state=PlanningState(plan:state.plan,error:PlanningApi.message(e));}
  }
  Future<bool> execute(Future<void> Function() action,String notice) async {
    if(!alive||state.saving||state.loading)return false;
    ++request;state=PlanningState(plan:state.plan,saving:true);
    try {
      await action();
      if(!alive)return true;
      try {
        final plan=await api.view(id);
        if(alive)state=PlanningState(plan:plan,notice:notice);
      }catch(e){if(alive)state=PlanningState(plan:state.plan,notice:notice,error:'El cambio se guardó, pero no se pudo actualizar la vista. Pulsa Actualizar.');}
      return true;
    }catch(e){if(alive)state=PlanningState(plan:state.plan,error:PlanningApi.message(e));return false;}
  }
  Future<bool> configure(Map<String,dynamic> body)=>execute(()=>api.configure(id,body),'Calendario creado. Ya puedes revisar los enfrentamientos.');
  Future<bool> action(PlanningRound round,String action)=>execute(()=>api.action(id,round.number,action,round.version),
    const {'schedule':'Horarios preparados. Revisa los partidos antes de confirmar.',
      'confirm':'Jornada confirmada.','cancel':'Jornada cancelada.','regenerate':'Horarios recalculados. Revisa el resultado.'}[action]??'Cambio guardado.');
  Future<bool> cancelLegacy(String roundId)=>execute(()=>api.cancelLegacy(roundId),'Asignaciones anteriores canceladas.');
  @override
  void dispose(){alive=false;request++;super.dispose();}
}
