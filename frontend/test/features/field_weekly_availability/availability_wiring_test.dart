import 'dart:convert';
import 'dart:typed_data';
import 'package:dio/dio.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:orchedule_app/app/providers.dart';
import 'package:orchedule_app/features/field_weekly_availability/di/field_weekly_availability_providers.dart';
import 'package:orchedule_app/features/field_weekly_availability/presentation/controllers/field_weekly_availability_controller.dart';

class _Adapter implements HttpClientAdapter {
  final List<RequestOptions> requests = [];
  int status = 200;
  @override
  Future<ResponseBody> fetch(RequestOptions options,
      Stream<Uint8List>? requestStream, Future<void>? cancelFuture) async {
    requests.add(options);
    return ResponseBody.fromString(jsonEncode(status == 200 ? [] : {'error': 'denied'}),
      status, headers: {Headers.contentTypeHeader: [Headers.jsonContentType]});
  }
  @override
  void close({bool force = false}) {}
}

void main() {
  late Dio dio;
  late _Adapter adapter;
  late ProviderContainer container;

  setUp(() {
    adapter = _Adapter();
    dio = Dio(BaseOptions(baseUrl: 'http://localhost:8080/api'));
    dio.httpClientAdapter = adapter;
    container = ProviderContainer(overrides: [dioProvider.overrideWithValue(dio)]);
  });
  tearDown(() { container.dispose(); dio.close(force: true); });

  test('availability reuses the shared client, not an unimplemented provider', () {
    expect(container.read(availabilityDioProvider), same(dio));
  });

  test('an empty week is valid and calls the correct URL', () async {
    final result = await container.read(getFieldWeeklyAvailabilityProvider)(
      seasonId: 'season', weekNumber: 1);
    expect(result, isEmpty);
    expect(adapter.requests.single.uri.path, '/api/v1/fields/availability');
    expect(adapter.requests.single.queryParameters['weekNumber'], 1);
  });

  test('a new week can select a field and save without prior records', () async {
    final provider = fieldWeeklyAvailabilityControllerProvider('season');
    final subscription = container.listen(provider, (_, __) {});
    final controller = container.read(provider.notifier);
    await controller.loadWeek(1);
    expect(container.read(provider).errorMessage, isNull);
    controller.toggleField('field');
    expect(container.read(provider).selectedFieldIds, contains('field'));
    expect(await controller.saveWeek(1), isTrue);
    expect(adapter.requests.last.data['enabledFieldIds'], ['field']);
    subscription.close();
  });

  test('authorization errors remain visible and do not enable editing', () async {
    adapter.status = 403;
    final provider = fieldWeeklyAvailabilityControllerProvider('season');
    final subscription = container.listen(provider, (_, __) {});
    final controller = container.read(provider.notifier);
    await controller.loadWeek(1);
    expect(container.read(provider).errorMessage, contains('permisos'));
    controller.toggleField('field');
    expect(container.read(provider).selectedFieldIds, isEmpty);
    subscription.close();
  });
}
