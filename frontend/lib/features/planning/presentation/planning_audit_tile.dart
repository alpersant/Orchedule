import 'package:flutter/material.dart';

String planningAuditAction(String action) {
  const configurations={
    'CONFIGURE_SINGLE':'Se creó el calendario a una vuelta',
    'CONFIGURE_DOUBLE':'Se creó el calendario de ida y vuelta',
    'CONFIGURE_MANUAL':'Se creó el calendario con enfrentamientos manuales',
  };
  if(configurations.containsKey(action))return configurations[action]!;
  final match=RegExp(r'^(SCHEDULE|RESOLVE|REGENERATE|CONFIRM|CANCEL)_(\d+)$').firstMatch(action);
  if(match==null)return 'Operación registrada ($action)';
  const labels={
    'SCHEDULE':'Se prepararon los horarios de la jornada',
    'RESOLVE':'Se revisaron los pendientes de la jornada',
    'REGENERATE':'Se recalcularon los horarios de la jornada',
    'CONFIRM':'Se confirmó la jornada',
    'CANCEL':'Se canceló la jornada',
  };
  return '${labels[match.group(1)]} ${match.group(2)}';
}

String planningAuditDate(Object? raw) {
  final value=DateTime.tryParse(raw?.toString()??'')?.toLocal();
  if(value==null)return 'Fecha no disponible';
  String two(int n)=>n.toString().padLeft(2,'0');
  return '${two(value.day)}/${two(value.month)}/${value.year} · ${two(value.hour)}:${two(value.minute)}';
}

class PlanningAuditTile extends StatelessWidget {
  final Map<String,dynamic> entry;
  const PlanningAuditTile({super.key,required this.entry});
  @override Widget build(BuildContext context) {
    final operation=entry['operation']?.toString();
    final detail=entry['detail']?.toString();
    final result=entry['result']?.toString();
    final name=entry['actor_name']?.toString()??'Usuario no disponible';
    return ListTile(
      leading:Icon(result=='ERROR'||result=='REJECTED'?Icons.warning_amber:Icons.history),
      title:Text(operation==null||operation.isEmpty?planningAuditAction(entry['action'].toString()):operation),
      subtitle:Text([
        if(detail!=null&&detail.isNotEmpty)detail,
        if(result!=null)const {'SUCCESS':'Operación completada','REJECTED':'Operación rechazada','ERROR':'Error'}[result]??result,
        '${planningAuditDate(entry['created_at'])} · $name',
      ].join('\n')),
    );
  }
}
