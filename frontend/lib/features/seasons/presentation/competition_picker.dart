import 'package:flutter/material.dart';
import '../domain/season.dart';
import '../domain/season_repository.dart';
import 'seasons_controller.dart';
Future<CompetitionOption?> pickSeasonCompetition(BuildContext context,
SeasonRepository repository) =>
showDialog<CompetitionOption>(context:context,
builder:(_)=>_CompetitionPicker(repository));
class _CompetitionPicker extends StatefulWidget {
  const _CompetitionPicker(this.repository);
  final SeasonRepository repository;
  @override State<_CompetitionPicker> createState()=>_CompetitionPickerState();
}
class _CompetitionPickerState extends State<_CompetitionPicker> {
  final _search=TextEditingController();
  CompetitionOptionsPage? _data;
  String? _error;
  bool _loading=true;
  int _request=0;
  @override void initState(){
    super.initState();
    _load(0);
  }
  @override void dispose(){
    ++_request;
    _search.dispose();
    super.dispose();
  }
  Future<void> _load(int page) async {
    final request=++_request;
    setState((){
      _loading=true;
      _error=null;
    });
    try {
      final result=await widget.repository.competitions(page:page,
      search:_search.text);
      if(mounted && request==_request)setState((){
        _data=result;
        _loading=false;
      });
    } catch(e){
      if(mounted && request==_request)setState((){
        _error=SeasonsController.message(e);
        _loading=false;
      });
    }
  }
  @override Widget build(BuildContext context)=>AlertDialog(
  title:const Text('Seleccionar competición'),
  content:SizedBox(width:480,
  height:400,
  child:Column(children:[
    TextField(controller:_search,
    maxLength:150,
    onSubmitted:(_)=>_load(0),
    decoration:InputDecoration(labelText:'Buscar por nombre',
    suffixIcon:IconButton(onPressed:()=>_load(0),
    icon:const Icon(Icons.search)))),
    Expanded(child:_loading?const Center(child:CircularProgressIndicator()):_error!=null?Center(child:Text(_error!)):
    _data!.items.isEmpty?const Center(child:Text('No hay competiciones. Crea una antes de la temporada.')):
    ListView.builder(itemCount:_data!.items.length,
    itemBuilder:(_,
    i){
      final item=_data!.items[
        i
      ];
      return ListTile(title:Text(item.name),
      onTap:()=>Navigator.pop(context,
      item));
    })),
    if(!_loading && _data!=null)Row(mainAxisAlignment:MainAxisAlignment.spaceBetween,
    children:[
      TextButton(onPressed:_data!.page>0?()=>_load(_data!.page-1):null,
      child:const Text('Anterior')),
      Text('Página ${_data!.page+1}'),
      TextButton(onPressed:!_data!.last?()=>_load(_data!.page+1):null,
      child:const Text('Siguiente')),
    ]),
    if(_error!=null)TextButton(onPressed:()=>_load(0),
    child:const Text('Reintentar')),
  ])),
  actions:[
    TextButton(onPressed:()=>Navigator.pop(context),
    child:const Text('Cancelar'))
  ],
  );
}
