import 'package:flutter_test/flutter_test.dart';
import '../../../lib/features/planning/presentation/planning_audit_tile.dart';

void main(){
  test('translates technical actions without inventing historical outcomes',(){
    expect(planningAuditAction('REGENERATE_1'),'Se recalcularon los horarios de la jornada 1');
    expect(planningAuditAction('SCHEDULE_12'),'Se prepararon los horarios de la jornada 12');
    expect(planningAuditAction('CONFIGURE_SINGLE'),'Se creó el calendario a una vuelta');
    expect(planningAuditAction('OTHER'),'Operación registrada (OTHER)');
  });
  test('handles a missing timestamp',(){
    expect(planningAuditDate(null),'Fecha no disponible');
  });
}
