import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../domain/season.dart';
import 'seasons_controller.dart';
import 'season_form_dialog.dart';
import 'season_delete_dialog.dart';
import 'competition_picker.dart';
class SeasonsPage extends ConsumerStatefulWidget {
  const SeasonsPage({
    super.key
  });
  @override ConsumerState<SeasonsPage> createState()=>_SeasonsPageState();
}
class _SeasonsPageState extends ConsumerState<SeasonsPage> {
  final _search=TextEditingController();
  bool _dialogOpen=false;
  @override void dispose(){
    _search.dispose();
    super.dispose();
  }
  void _message(String text){
    if(mounted)ScaffoldMessenger.of(context).showSnackBar(SnackBar(content:Text(text)));
  }
  Future<void> _form({
    Season? season
  }) async {
    if(_dialogOpen)return;
    _dialogOpen=true;
    final controller=ref.read(seasonsProvider.notifier);
    try {
      final saved=await showDialog<bool>(context:context,
      barrierDismissible:false,
      builder:(_)=>SeasonFormDialog(
      repository:ref.read(seasonsRepositoryProvider),
      season:season,
      competition:season==null?controller.competition:null,
      onSave:(input)=>controller.save(input,
      id:season?.id)));
      if(saved==true)_message('Temporada guardada.');
    }finally{
      _dialogOpen=false;
    }
  }
  Future<void> _delete(List<Season> seasons) async {
    if(_dialogOpen || seasons.isEmpty)return;
    _dialogOpen=true;
    try{
      final confirmed=await showDialog<bool>(context:context,
      barrierDismissible:false,
      builder:(_)=>SeasonDeleteDialog(names:seasons.map((s)=>s.name).toList()));
      if(!mounted || confirmed!=true)return;
      final controller=ref.read(seasonsProvider.notifier);
      if(seasons.length==1){
        final ok=await controller.delete(seasons.single.id);
        if(mounted)_message(ok?'Temporada eliminada.':ref.read(seasonsProvider).error??'No se pudo eliminar.');
      }else{
        final result=await controller.deleteMany(seasons.map((s)=>s.id).toList());
        if(!mounted)return;
        if(result==null){
          _message(ref.read(seasonsProvider).error??'No se pudo completar el borrado.');
          return;
        }
        _message('${result.deletedIds.length} eliminadas; ${result.failures.length} incidencias.');
        if(result.failures.isNotEmpty){
          await showDialog<void>(context:context,
          builder:(ctx)=>AlertDialog(title:const Text('Incidencias del borrado'),
          scrollable:true,
          content:Column(mainAxisSize:MainAxisSize.min,
          crossAxisAlignment:CrossAxisAlignment.start,
          children:result.failures.map((failure){
            final matches=seasons.where((s)=>s.id==failure.seasonId);
            final name=matches.isEmpty?failure.seasonId:matches.first.name;
            return Padding(padding:const EdgeInsets.only(bottom:12),
            child:Text('$name: ${failure.reason}'));
          }).toList()),
          actions:[
            TextButton(onPressed:()=>Navigator.pop(ctx),
            child:const Text('Cerrar'))
          ]));
        }
      }
    }finally{
      _dialogOpen=false;
    }
  }
  Future<void> _transition(Season season,
  bool activate) async {
    if(_dialogOpen)return;
    _dialogOpen=true;
    try{
      final confirmed=await showDialog<bool>(context:context,
      builder:(ctx)=>AlertDialog(
      title:Text(activate?'Activar temporada':'Cerrar temporada'),
      content:Text('¿Confirmas la operación para "${season.name}"?'),
      actions:[
        TextButton(onPressed:()=>Navigator.pop(ctx,
        false),
        child:const Text('Cancelar')),
        FilledButton(onPressed:()=>Navigator.pop(ctx,
        true),
        child:const Text('Confirmar'))
      ]));
      if(!mounted||confirmed!=true)return;
      final c=ref.read(seasonsProvider.notifier);
      final ok=activate?await c.activate(season.id):await c.close(season.id);
      if(mounted)_message(ok?'Estado actualizado.':ref.read(seasonsProvider).error??'No se pudo actualizar.');
    }finally{
      _dialogOpen=false;
    }
  }
  @override Widget build(BuildContext context){
    final state=ref.watch(seasonsProvider);
    final controller=ref.read(seasonsProvider.notifier);
    final busy=state.loading||state.saving;
    return Scaffold(
    appBar:AppBar(title:Text(state.selected.isEmpty?'Temporadas':'${state.selected.length} seleccionadas'),
    actions:[
      if(state.selected.isNotEmpty)IconButton(tooltip:'Cancelar selección',
      onPressed:busy?null:()=>controller.selectPage(false),
      icon:const Icon(Icons.close)),
      if(state.selected.isNotEmpty)IconButton(tooltip:'Eliminar seleccionadas',
      onPressed:busy?null:()=>_delete(state.items.where((s)=>state.selected.contains(s.id)).toList()),
      icon:Icon(Icons.delete_sweep_outlined,
      color:Theme.of(context).colorScheme.error)),
      IconButton(tooltip:'Actualizar',
      onPressed:busy?null:()=>controller.load(),
      icon:const Icon(Icons.refresh)),
    ]),
    floatingActionButton:FloatingActionButton.extended(onPressed:busy?null:()=>_form(),
    icon:const Icon(Icons.add),
    label:const Text('Nueva temporada')),
    body:Column(children:[
      Padding(padding:const EdgeInsets.all(16),
      child:Column(children:[
        TextField(controller:_search,
        maxLength:150,
        enabled:!busy,
        textInputAction:TextInputAction.search,
        onSubmitted:(v){
          controller.search=v;
          controller.load(page:0);
        },
        decoration:InputDecoration(labelText:'Buscar temporada',
        prefixIcon:const Icon(Icons.search),
        suffixIcon:IconButton(tooltip:'Buscar',
        onPressed:busy?null:(){
          controller.search=_search.text;
          controller.load(page:0);
        },
        icon:const Icon(Icons.arrow_forward)),
        border:const OutlineInputBorder())),
        Wrap(spacing:12,
        runSpacing:8,
        crossAxisAlignment:WrapCrossAlignment.center,
        children:[
          OutlinedButton.icon(icon:const Icon(Icons.emoji_events_outlined),
          label:Text(controller.competition?.name??'Todas las competiciones'),
          onPressed:busy?null:()async{
            final selected=await pickSeasonCompetition(context,
            ref.read(seasonsRepositoryProvider));
            if(!mounted||selected==null)return;
            controller.competition=selected;
            controller.load(page:0);
          }),
          if(controller.competition!=null)TextButton(onPressed:busy?null:(){
            controller.competition=null;
            controller.load(page:0);
          },
          child:const Text('Quitar filtro')),
          DropdownButton<SeasonStatus>(value:controller.status,
          hint:const Text('Todos los estados'),
          onChanged:busy?null:(v){
            controller.status=v;
            controller.load(page:0);
          },
          items:SeasonStatus.values.map((s)=>DropdownMenuItem(value:s,
          child:Text(seasonStatusLabel(s)))).toList()),
          if(controller.status!=null)TextButton(onPressed:busy?null:(){
            controller.status=null;
            controller.load(page:0);
          },
          child:const Text('Todos los estados')),
        ]),
      ])),
      if(state.loading||state.saving)const LinearProgressIndicator(),
      if(state.error!=null)MaterialBanner(content:Text(state.error!),
      actions:[
        TextButton(onPressed:busy?null:()=>controller.load(),
        child:const Text('Reintentar'))
      ]),
      if(state.items.isNotEmpty)Row(children:[
        Checkbox(value:state.selected.length==state.items.length,
        onChanged:busy?null:(v)=>controller.selectPage(v??false)),
        const Text('Seleccionar esta página'),
        const Spacer(),
        Padding(padding:const EdgeInsets.only(right:16),
        child:Text('${state.total} temporadas')),
      ]),
      Expanded(child:state.items.isEmpty?Center(child:Text(state.loading?'Cargando temporadas…':'No hay temporadas para estos filtros.')):
      ListView.separated(padding:const EdgeInsets.fromLTRB(16,
      8,
      16,
      90),
      itemCount:state.items.length,
      separatorBuilder:(_,
      __)=>const SizedBox(height:8),
      itemBuilder:(_,
      i){
        final season=state.items[
          i
        ];
        return Card(child:ListTile(
        leading:Checkbox(value:state.selected.contains(season.id),
        onChanged:busy?null:(_)=>controller.toggle(season.id)),
        title:Text(season.name),
        subtitle:Text('${seasonStatusLabel(season.status)} · ${dateOnly(season.startDate)} — ${dateOnly(season.endDate)}${season.competitionId==null?' · Pendiente de asociar competición':''}'),
        onTap:busy?null:()=>_form(season:season),
        trailing:PopupMenuButton<String>(enabled:!busy,
        tooltip:'Acciones de temporada',
        onSelected:(action){
          switch(action){
            case 'edit':_form(season:season);
            break;
            case 'activate':_transition(season,
            true);
            break;
            case 'close':_transition(season,
            false);
            break;
            case 'delete':_delete([
              season
            ]);
            break;
          }
        },
        itemBuilder:(_)=>[
          const PopupMenuItem(value:'edit',
          child:Text('Editar')),
          if(season.status!=SeasonStatus.closed && !(season.status==SeasonStatus.active&&season.active))const PopupMenuItem(value:'activate',
          child:Text('Activar')),
          if(season.status!=SeasonStatus.closed)const PopupMenuItem(value:'close',
          child:Text('Cerrar')),
          const PopupMenuDivider(),
          const PopupMenuItem(value:'delete',
          child:Text('Eliminar')),
        ]),
        ));
      })),
      SafeArea(top:false,
      child:Row(mainAxisAlignment:MainAxisAlignment.center,
      children:[
        TextButton(onPressed:!busy&&state.page>0?()=>controller.load(page:state.page-1):null,
        child:const Text('Anterior')),
        Text('Página ${state.page+1} de ${state.totalPages==0?1:state.totalPages}'),
        TextButton(onPressed:!busy&&state.page+1<state.totalPages?()=>controller.load(page:state.page+1):null,
        child:const Text('Siguiente')),
      ])),
    ]),
    );
  }
}
