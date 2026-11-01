import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import '../../../lib/features/planning/presentation/configure_plan_dialog.dart';
import '../../../lib/features/planning/presentation/create_calendar_wizard.dart';
import 'capacity_test_support.dart';
void main(){
  testWidgets('modal receives season explicitly without needing any GoRouter context',(tester) async {
    tester.view.physicalSize=const Size(1100,1000);tester.view.devicePixelRatio=1;
    addTearDown(tester.view.resetPhysicalSize);addTearDown(tester.view.resetDevicePixelRatio);
    final api=FakeCalendarCapacityApi();
    await tester.pumpWidget(MaterialApp(home:Scaffold(body:CreateCalendarWizard(plan:capacityPlan(),capacityApi:api))));
    await tester.pumpAndSettle();
    final modal=tester.widget<ConfigurePlanDialog>(find.byType(ConfigurePlanDialog));
    expect(modal.seasonId,'season-1');expect(api.checks,1);
    expect(find.textContaining('no ha recibido una temporada'),findsNothing);
    expect(find.text('Comprobando fechas…'),findsNothing);
    expect(tester.takeException(),isNull);
  });
  testWidgets('invalid input cannot leave loading active without a request',(tester) async {
    final api=FakeCalendarCapacityApi();
    await tester.pumpWidget(MaterialApp(home:Scaffold(body:ConfigurePlanDialog(seasonId:'',teams:capacityTeams(),capacityApi:api))));
    await tester.pumpAndSettle();
    expect(api.checks,0);expect(find.text('Comprobando fechas…'),findsNothing);
    expect(find.textContaining('no ha recibido una temporada'),findsOneWidget);
    expect(tester.takeException(),isNull);
  });
}
