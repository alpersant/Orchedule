import 'dart:async';
import 'dart:typed_data';
import 'package:dio/dio.dart';
import 'package:flutter_test/flutter_test.dart';
import '../../../lib/features/planning/data/calendar_capacity_api.dart';
class WaitingAdapter implements HttpClientAdapter {
  int requests=0;
  @override Future<ResponseBody> fetch(RequestOptions options,Stream<Uint8List>? stream,Future<void>? cancelFuture){
    requests++;final pending=Completer<ResponseBody>();
    cancelFuture?.then((_) {if(!pending.isCompleted)pending.completeError(DioException(requestOptions:options,type:DioExceptionType.cancel));});
    return pending.future;
  }
  @override void close({bool force=false}){}
}
void main(){
  test('whole preflight request has a deadline, even if response never arrives',() async {
    final dio=Dio(BaseOptions(baseUrl:'http://localhost/api'));final adapter=WaitingAdapter();dio.httpClientAdapter=adapter;
    final api=CalendarCapacityApi(dio,deadline:const Duration(milliseconds:30));
    final watch=Stopwatch()..start();
    await expectLater(api.check('season',mode:'SINGLE',teamIds:['a','b']),throwsA(isA<CalendarCapacityTimeout>()));
    expect(watch.elapsed,lessThan(const Duration(seconds:1)));expect(adapter.requests,1);
    dio.close(force:true);
  });
  test('superseded check is actually cancelled',() async {
    final dio=Dio(BaseOptions(baseUrl:'http://localhost/api'));dio.httpClientAdapter=WaitingAdapter();
    final api=CalendarCapacityApi(dio);final pending=api.check('season',mode:'SINGLE',teamIds:['a','b']);
    final assertion=expectLater(pending,throwsA(predicate<Object>(CalendarCapacityApi.isCancellation)));
    await Future<void>.delayed(Duration.zero);api.cancelCheck('changed selection');await assertion;
    dio.close(force:true);
  });
}
