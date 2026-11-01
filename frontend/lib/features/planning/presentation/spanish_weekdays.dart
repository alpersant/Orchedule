const _weekdays={
  'MONDAY':'Lunes','TUESDAY':'Martes','WEDNESDAY':'Miércoles','THURSDAY':'Jueves',
  'FRIDAY':'Viernes','SATURDAY':'Sábado','SUNDAY':'Domingo',
};
String weekdayEs(String raw){
  final key=raw.trim().toUpperCase().split('.').last;
  final translated=_weekdays[key];
  if(translated!=null)return translated;
  for(final value in _weekdays.values){if(value.toUpperCase()==key)return value;}
  return raw;
}
/// Localizes view-only copies. Never changes enums used to save field opening hours.
Map<String,dynamic> localizePlanningWeekdays(Map<String,dynamic> input){
  final result=Map<String,dynamic>.from(input);
  final assignments=input['legacyAssignments'];
  if(assignments is List){
    result['legacyAssignments']=assignments.map((item){
      final row=Map<String,dynamic>.from(item as Map);
      if(row['day'] is String)row['day']=weekdayEs(row['day'] as String);
      return row;
    }).toList();
  }
  final fairness=input['fairness'];
  if(fairness is List){
    result['fairness']=fairness.map((item){
      final row=Map<String,dynamic>.from(item as Map),days=row['days'];
      if(days is Map){
        final localized=<String,dynamic>{};
        for(final entry in days.entries)localized[weekdayEs(entry.key.toString())]=entry.value;
        final ordered=<String,dynamic>{};
        for(final name in _weekdays.values){if(localized.containsKey(name))ordered[name]=localized[name];}
        for(final entry in localized.entries)ordered.putIfAbsent(entry.key,()=>entry.value);
        row['days']=ordered;
      }
      return row;
    }).toList();
  }
  return result;
}
