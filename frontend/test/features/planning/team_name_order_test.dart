import 'package:flutter_test/flutter_test.dart';
import '../../../lib/features/planning/domain/team_name_order.dart';
void main(){
  test('numeric suffixes are naturally ordered',(){
    final values=['Equipo Test 24','Equipo Test 10','Equipo Test 2','Equipo Test 1']..sort(compareTeamNames);
    expect(values,['Equipo Test 1','Equipo Test 2','Equipo Test 10','Equipo Test 24']);
  });
  test('Spanish accents and case do not alter the primary alphabetical order',(){
    final values=['Zeta','Ñandú','Nube','álamo','Olivo','beta']..sort(compareTeamNames);
    expect(values,['álamo','beta','Nube','Ñandú','Olivo','Zeta']);
  });
  test('duplicates are deduplicated by id without mutating input',(){
    final input=[{'id':'b','name':'Equipo 10'},{'id':'a','name':'Equipo 2'},{'id':'a','name':'Equipo 2'}];
    final result=orderedTeams(input);
    expect(result.map((t)=>t['id']),['a','b']);expect(input,hasLength(3));
  });
}
