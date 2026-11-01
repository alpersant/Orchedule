import 'package:flutter_test/flutter_test.dart';
import '../../../lib/features/planning/presentation/spanish_weekdays.dart';
void main(){
  test('weekday enums have Spanish labels',(){
    expect(['MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY','SUNDAY'].map(weekdayEs).toList(),
      ['Lunes','Martes','Miércoles','Jueves','Viernes','Sábado','Domingo']);
  });
  test('localizes view data without changing original values or counts',(){
    final data=<String,dynamic>{
      'legacyAssignments':[{'day':'FRIDAY','teamId':'a'}],
      'fairness':[{'days':{'FRIDAY':2,'MONDAY':3}}],
      'fields':[{'days':['MONDAY','FRIDAY']}],
    };
    final result=localizePlanningWeekdays(data);
    expect((result['legacyAssignments'] as List).single['day'],'Viernes');
    expect((result['fairness'] as List).single['days'],{'Lunes':3,'Viernes':2});
    expect((data['legacyAssignments'] as List).single['day'],'FRIDAY');
    expect((result['fields'] as List).single['days'],['MONDAY','FRIDAY']);
  });
}
