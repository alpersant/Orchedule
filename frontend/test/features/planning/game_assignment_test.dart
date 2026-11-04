import 'package:flutter_test/flutter_test.dart';
import 'package:flutter/material.dart';
import '../../../lib/features/planning/presentation/game_assignment_dialog.dart';
import '../../../lib/features/planning/domain/planning_view.dart';

void main(){
  test('sends local season time without converting to device UTC',(){
    expect(assignmentLocal(DateTime(2026,12,8),const TimeOfDay(hour:19,minute:5)),'2026-12-08T19:05:00');
  });
  test('reads provisional manual and confirmation flags',(){
    final game=PlanningGame.fromJson({
      'id':'g','home_team_id':'h','away_team_id':'a','home_name':'Local','away_name':'Visitante',
      'round_number':1,'local_start':'2026-12-08T19:00:00','manual_assignment':true,
      'confirmation_status':'PROVISIONAL','field_id':null,
    });
    expect(game.manualAssignment,isTrue);expect(game.confirmed,isFalse);
    expect(game.scheduled,isFalse);expect(game.localStart,isNotNull);
  });
}
