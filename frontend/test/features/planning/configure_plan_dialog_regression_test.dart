import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import '../../../lib/features/planning/presentation/configure_plan_dialog.dart';
import 'capacity_test_support.dart';
void main(){
  testWidgets('24 selected teams fit, stay ordered and require no timezone',(tester) async {
    tester.view.physicalSize=const Size(1100,1000);tester.view.devicePixelRatio=1;
    addTearDown(tester.view.resetPhysicalSize);addTearDown(tester.view.resetDevicePixelRatio);
    final api=FakeCalendarCapacityApi(weeks:40);Map<String,dynamic>? result;
    await tester.pumpWidget(MaterialApp(home:Builder(builder:(context)=>Scaffold(body:TextButton(onPressed:() async {
      result=await showDialog<Map<String,dynamic>>(context:context,builder:(_)=>ConfigurePlanDialog(teams:capacityTeams(),seasonId:'season-1',capacityApi:api));
    },child:const Text('Abrir'))))));
    await tester.tap(find.text('Abrir'));await tester.pumpAndSettle();
    expect(find.textContaining('Zona horaria'),findsNothing);
    await tester.tap(find.text('Elegir equipos'));await tester.pumpAndSettle();
    final boxes=tester.widgetList<CheckboxListTile>(find.byType(CheckboxListTile)).toList();
    expect(boxes,hasLength(24));expect(boxes.every((c)=>c.value==true),isTrue);
    expect(boxes.map((c)=>(c.title! as Text).data).toList(),List.generate(24,(i)=>'Equipo Test ${i+1}'));
    await tester.tap(find.text('Continuar'));await tester.pumpAndSettle();
    await tester.tap(find.text('Crear calendario'));await tester.pumpAndSettle();
    expect(result!['teamIds'],hasLength(24));expect(result!.containsKey('zoneId'),isFalse);
    expect(api.checks,greaterThanOrEqualTo(3));expect(tester.takeException(),isNull);
  });
}
