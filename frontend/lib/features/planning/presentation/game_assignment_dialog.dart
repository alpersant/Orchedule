import 'package:flutter/material.dart';
import '../data/game_assignment_api.dart';
import '../data/planning_api.dart';
import '../domain/planning_view.dart';

String assignmentLocal(DateTime date,TimeOfDay time) {
  String two(int n)=>n.toString().padLeft(2,'0');
  return '${date.year.toString().padLeft(4,'0')}-${two(date.month)}-${two(date.day)}T${two(time.hour)}:${two(time.minute)}:00';
}

class GameAssignmentDialog extends StatefulWidget {
  final PlanningGame game;
  final PlanningRound round;
  final PlanningView plan;
  final GameAssignmentApi api;
  const GameAssignmentDialog({super.key,required this.game,required this.round,required this.plan,required this.api});
  @override State<GameAssignmentDialog> createState()=>_GameAssignmentDialogState();
}
class _GameAssignmentDialogState extends State<GameAssignmentDialog> {
  late DateTime date;
  late TimeOfDay time;
  bool busy=false,checked=false;
  String? error,fieldId;
  List<Map<String,dynamic>> fields=[];
  DateTime get minimum {
    final start=DateTime.parse(widget.plan.start);
    final monday=DateTime(start.year,start.month,start.day).subtract(Duration(days:start.weekday-1)).add(Duration(days:(widget.round.week-1)*7));
    return monday.isBefore(start)?DateTime(start.year,start.month,start.day):monday;
  }
  DateTime get maximum {
    final start=DateTime.parse(widget.plan.start),end=DateTime.parse(widget.plan.end);
    final last=DateTime(start.year,start.month,start.day).subtract(Duration(days:start.weekday-1)).add(Duration(days:(widget.round.week-1)*7+6));
    return last.isAfter(end)?DateTime(end.year,end.month,end.day):last;
  }
  @override void initState(){
    super.initState();
    date=widget.game.localStart??minimum;
    time=widget.game.localStart==null?const TimeOfDay(hour:18,minute:0):TimeOfDay.fromDateTime(widget.game.localStart!);
  }
  String get local=>assignmentLocal(date,time);
  void changed(){fields=[];fieldId=null;checked=false;error=null;}
  Future<void> chooseDate() async {
    final initial=date.isBefore(minimum)?minimum:date.isAfter(maximum)?maximum:date;
    final result=await showDatePicker(context:context,initialDate:initial,firstDate:minimum,lastDate:maximum,helpText:'Fecha de esta jornada');
    if(result!=null&&mounted)setState((){date=result;changed();});
  }
  Future<void> chooseTime() async {
    final result=await showTimePicker(context:context,initialTime:time);
    if(result!=null&&mounted)setState((){time=result;changed();});
  }
  Future<void> check() async {
    if(busy)return;
    setState((){busy=true;error=null;});
    try {
      final result=await widget.api.fields(widget.plan.seasonId,widget.game.id,widget.round.version,local);
      if(mounted)setState((){fields=result;checked=true;fieldId=null;});
    } catch(e){if(mounted)setState(()=>error=PlanningApi.message(e));}
    finally{if(mounted)setState(()=>busy=false);}
  }
  Future<void> save() async {
    if(busy)return;
    setState((){busy=true;error=null;});
    try {
      await widget.api.save(widget.plan.seasonId,widget.game.id,widget.round.version,local,fieldId);
      if(mounted)Navigator.pop(context,true);
    } catch(e){if(mounted)setState(()=>error=PlanningApi.message(e));}
    finally{if(mounted)setState(()=>busy=false);}
  }
  @override Widget build(BuildContext context)=>PopScope(canPop:!busy,child:AlertDialog(
    title:Text('${widget.game.home} — ${widget.game.away}'),scrollable:true,
    content:SizedBox(width:620,child:Column(mainAxisSize:MainAxisSize.min,crossAxisAlignment:CrossAxisAlignment.stretch,children:[
      Text('Jornada ${widget.round.number} · ${planningWeekRange(widget.plan,widget.round.week)}'),
      const SizedBox(height:12),
      const Text('La hora se interpreta en la zona horaria de la temporada, no en la del dispositivo. Puedes guardar una propuesta sin campo; no se publica hasta confirmarla.'),
      const SizedBox(height:16),
      Wrap(spacing:12,runSpacing:8,children:[
        OutlinedButton.icon(onPressed:busy?null:chooseDate,icon:const Icon(Icons.calendar_today),label:Text(planningDate(date))),
        OutlinedButton.icon(onPressed:busy?null:chooseTime,icon:const Icon(Icons.schedule),label:Text(time.format(context))),
      ]),const SizedBox(height:16),
      OutlinedButton.icon(onPressed:busy?null:check,icon:const Icon(Icons.stadium_outlined),label:const Text('Buscar campos disponibles en esta franja')),
      if(checked)...[
        const SizedBox(height:12),
        if(fields.isEmpty)const Text('No hay campos disponibles para esta franja. Puedes guardar la propuesta sin campo si supera las reglas de los equipos.'),
        DropdownButtonFormField<String>(key:ValueKey('$local-${fields.length}'),value:fieldId,isExpanded:true,
          decoration:const InputDecoration(labelText:'Campo',border:OutlineInputBorder()),
          items:[const DropdownMenuItem<String>(value:null,child:Text('Sin campo · propuesta provisional')),
            ...fields.map((f)=>DropdownMenuItem<String>(value:f['id'] as String,child:Text('${f['name']} · ${f['venue_name']}')))],
          onChanged:busy?null:(value)=>setState(()=>fieldId=value)),
      ],
      const SizedBox(height:12),
      const Text('Se validarán exclusiones, preferencias, rotación, bloqueos, descanso y máximo semanal. Guardar una asignación manual la protege del cálculo automático.'),
      if(error!=null)Padding(padding:const EdgeInsets.only(top:12),child:Text(error!,style:TextStyle(color:Theme.of(context).colorScheme.error))),
      if(busy)const Padding(padding:EdgeInsets.only(top:12),child:LinearProgressIndicator()),
    ])),
    actions:[TextButton(onPressed:busy?null:()=>Navigator.pop(context,false),child:const Text('Cancelar')),
      FilledButton(onPressed:busy?null:save,child:Text(fieldId==null?'Guardar sin campo':'Guardar fecha y campo'))],
  ));
}
