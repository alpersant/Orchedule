import 'season.dart';
abstract interface class SeasonRepository {
  Future<SeasonPage> search({
    required int page,
    String? competitionId,
    String? search,
    SeasonStatus? status
  });
  Future<Season> create(SeasonInput input);
  Future<Season> update(String id,
  SeasonInput input);
  Future<Season> activate(String id);
  Future<Season> close(String id);
  Future<void> delete(String id);
  Future<DeleteSeasonsResult> deleteMany(List<String> ids);
  Future<CompetitionOptionsPage> competitions({
    int page = 0,
    String search = ''
  });
}
