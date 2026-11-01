import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import '../../../lib/features/planning/presentation/create_calendar_wizard.dart';
import 'capacity_test_support.dart';
void main(){
  testWidgets('insufficient season is shown before review and creation is blocked',(tester) async {
    tester.view.physicalSize=const Size(1100,1000);tester.view.devicePixelRatio=1;
    addTearDown(tester.view.resetPhysicalSize);addTearDown(tester.view.resetDevicePixelRatio);
    final api=FakeCalendarCapacityApi(weeks:18);Map<String,dynamic>? result;
    await tester.pumpWidget(MaterialApp(home:Builder(builder:(context)=>Scaffold(body:TextButton(onPressed:() async {
      result=await showDialog<Map<String,dynamic>>(context:context,builder:(_)=>CreateCalendarWizard(plan:capacityPlan(),capacityApi:api));
    },child:const Text('Abrir'))))));
    await tester.tap(find.text('Abrir'));await tester.pumpAndSettle();
    expect(find.textContaining('Necesitas 23 jornadas'),findsOneWidget);
    expect(find.textContaining('Faltan 5 semanas'),findsOneWidget);
    expect(find.text('Ampliar fecha final'),findsOneWidget);
    await tester.tap(find.text('Elegir equipos'));await tester.pumpAndSettle();
    expect(tester.widget<FilledButton>(find.widgetWithText(FilledButton,'Continuar')).onPressed,isNull);
    expect(result,isNull);expect(tester.takeException(),isNull);
  });
}
