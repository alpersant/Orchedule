import 'playing_field.dart';

abstract class FieldsRepository {
  Future<List<PlayingField>> list(String venueId);
  Future<void> create(String venueId, String name);
  Future<void> rename(String id, String name);
  Future<void> setActive(String id, bool active);
}
