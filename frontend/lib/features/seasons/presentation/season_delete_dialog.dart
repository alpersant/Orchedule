import 'package:flutter/material.dart';
class SeasonDeleteDialog extends StatefulWidget {
  const SeasonDeleteDialog({
    super.key,
    required this.names
  });
  final List<String> names;
  @override State<SeasonDeleteDialog> createState()=>_SeasonDeleteDialogState();
}
class _SeasonDeleteDialogState extends State<SeasonDeleteDialog> {
  final _confirmation=TextEditingController();
  String get _expected=>widget.names.length==1?widget.names.single:'ELIMINAR';
  @override void dispose(){
    _confirmation.dispose();
    super.dispose();
  }
  @override Widget build(BuildContext context){
    final colors=Theme.of(context).colorScheme;
    return AlertDialog(title:Text('Eliminar ${widget.names.length} temporada(s)'),
    scrollable:true,
    content:SizedBox(width:480,
    child:Column(mainAxisSize:MainAxisSize.min,
    crossAxisAlignment:CrossAxisAlignment.start,
    children:[
      const Text('Se eliminarán las temporadas seleccionadas, sus partidos, jornadas y disponibilidad semanal propia. La competición, equipos y campos se conservarán.'),
      const SizedBox(height:12),
      ...widget.names.map((name)=>Text('• $name')),
      const SizedBox(height:12),
      const Text('Esta acción no se puede deshacer.'),
      const SizedBox(height:12),
      TextField(controller:_confirmation,
      onChanged:(_)=>setState((){
      }),
      decoration:InputDecoration(labelText:'Escribe "$_expected" para confirmar',
      border:const OutlineInputBorder())),
    ])),
    actions:[
      TextButton(onPressed:()=>Navigator.pop(context,
      false),
      child:const Text('Cancelar')),
      FilledButton(style:FilledButton.styleFrom(backgroundColor:colors.error,
      foregroundColor:colors.onError),
      onPressed:_confirmation.text.trim()==_expected?()=>Navigator.pop(context,
      true):null,
      child:const Text('Eliminar definitivamente')),
    ]);
  }
}
