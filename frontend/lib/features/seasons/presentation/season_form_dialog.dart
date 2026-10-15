import 'package:flutter/material.dart';
import '../domain/season.dart';
import '../domain/season_repository.dart';
import 'competition_picker.dart';
class SeasonFormDialog extends StatefulWidget {
  const SeasonFormDialog({
    super.key,
    required this.repository,
    required this.onSave,
    this.season,
    this.competition
  });
  final SeasonRepository repository;
  final Future<bool> Function(SeasonInput) onSave;
  final Season? season;
  final CompetitionOption? competition;
  @override State<SeasonFormDialog> createState()=>_SeasonFormDialogState();
}
class _SeasonFormDialogState extends State<SeasonFormDialog> {
  final _form=GlobalKey<FormState>();
  late final TextEditingController _name;
  late DateTime _start;
  late DateTime _end;
  CompetitionOption? _competition;
  bool _saving=false;
  String? _error;
  @override void initState(){
    super.initState();
    final now=DateUtils.dateOnly(DateTime.now());
    _name=TextEditingController(text:widget.season?.name??'');
    _start=widget.season?.startDate??now;
    _end=widget.season?.endDate??now.add(const Duration(days:365));
    _competition=widget.competition;
  }
  @override void dispose(){
    _name.dispose();
    super.dispose();
  }
  Future<void> _chooseDate(bool start) async {
    final value=await showDatePicker(context:context,
    initialDate:start?_start:_end,
    firstDate:DateTime(1900),
    lastDate:DateTime(2200));
    if(mounted && value!=null)setState((){
      if(start){
        _start=value;
      }else{
        _end=value;
      }
    });
  }
  Future<void> _save() async {
    if(_saving || _form.currentState?.validate()!=true)return;
    if(widget.season==null && _competition==null){
      setState(()=>_error='Selecciona una competición.');
      return;
    }
    if(_end.isBefore(_start)){
      setState(()=>_error='El final no puede ser anterior al inicio.');
      return;
    }
    if(widget.season==null && _start.isBefore(DateUtils.dateOnly(DateTime.now()))){
      setState(()=>_error='Una temporada nueva debe comenzar hoy o después.');
      return;
    }
    setState((){
      _saving=true;
      _error=null;
    });
    try{
      final ok=await widget.onSave(SeasonInput(name:_name.text.trim(),
      startDate:_start,
      endDate:_end,
      competitionId:widget.season?.competitionId??_competition?.id,
      active:widget.season?.active??false));
      if(!mounted)return;
      if(ok){
        Navigator.pop(context,
        true);
      }else{
        setState(()=>_error='No se guardó. Revisa los datos, el nombre y tus permisos.');
      }
    }catch(_){
      if(mounted)setState(()=>_error='No se pudo guardar la temporada.');
    }
    finally{
      if(mounted)setState(()=>_saving=false);
    }
  }
  @override Widget build(BuildContext context)=>PopScope(
  canPop:!_saving,
  child:AlertDialog(title:Text(widget.season==null?'Nueva temporada':'Editar temporada'),
  scrollable:true,
  content:SizedBox(width:480,
  child:Form(key:_form,
  child:Column(mainAxisSize:MainAxisSize.min,
  crossAxisAlignment:CrossAxisAlignment.stretch,
  children:[
    if(widget.season==null)OutlinedButton.icon(
    onPressed:_saving?null:() async{
      final selected=await pickSeasonCompetition(context,
      widget.repository);
      if(mounted && selected!=null)setState(()=>_competition=selected);
    },
    icon:const Icon(Icons.emoji_events_outlined),
    label:Text(_competition?.name??'Seleccionar competición'))
    else Text('Competición: ${widget.season!.competitionId??"Pendiente de migración"}'),
    const SizedBox(height:16),
    TextFormField(controller:_name,
    enabled:!_saving,
    maxLength:120,
    decoration:const InputDecoration(labelText:'Nombre',
    hintText:'Ej. Temporada 2026/2027',
    border:OutlineInputBorder()),
    validator:(v)=>v==null||v.trim().isEmpty?'El nombre es obligatorio.':v.trim().length>120?'Máximo 120 caracteres.':null),
    const SizedBox(height:12),
    OutlinedButton.icon(onPressed:_saving?null:()=>_chooseDate(true),
    icon:const Icon(Icons.calendar_today),
    label:Text('Inicio: ${dateOnly(_start)}')),
    OutlinedButton.icon(onPressed:_saving?null:()=>_chooseDate(false),
    icon:const Icon(Icons.event),
    label:Text('Final: ${dateOnly(_end)}')),
    if(widget.season!=null)const Padding(padding:EdgeInsets.only(top:8),
    child:Text('La competición y el estado se conservan. Usa el menú para activar o cerrar.')),
    if(_error!=null)Padding(padding:const EdgeInsets.only(top:12),
    child:Text(_error!,
    style:TextStyle(color:Theme.of(context).colorScheme.error))),
  ]))),
  actions:[
    TextButton(onPressed:_saving?null:()=>Navigator.pop(context),
    child:const Text('Cancelar')),
    FilledButton(onPressed:_saving?null:_save,
    child:_saving?const SizedBox(width:18,
    height:18,
    child:CircularProgressIndicator(strokeWidth:2)):const Text('Guardar')),
  ],
  ),
  );
}
