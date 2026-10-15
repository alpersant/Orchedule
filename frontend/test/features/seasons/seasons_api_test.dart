import 'package:flutter_test/flutter_test.dart';
// Replace this relative import only if you move the test file.
import '../../../lib/features/seasons/domain/season.dart';
void main() {
  test('serializes dates without timezone conversion',
  () {
    expect(dateOnly(DateTime(2026,
    1,
    2,
    23,
    59)),
    '2026-01-02');
  });
  test('status labels match supported lifecycle',
  () {
    expect(seasonStatusLabel(SeasonStatus.closed),
    'Cerrada');
  });
}
