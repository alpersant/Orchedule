enum SeasonStatus {
  draft,
  active,
  closed
}
class Season {
  const Season({
    required this.id,
    required this.competitionId,
    required this.name,
    required this.startDate,
    required this.endDate,
    required this.status,
    required this.active
  });
  final String id;
  final String? competitionId;
  final String name;
  final DateTime startDate;
  final DateTime endDate;
  final SeasonStatus status;
  final bool active;
}
class SeasonPage {
  const SeasonPage(this.items,
  this.page,
  this.totalPages,
  this.totalElements);
  final List<Season> items;
  final int page;
  final int totalPages;
  final int totalElements;
}
class CompetitionOption {
  const CompetitionOption(this.id,
  this.name);
  final String id;
  final String name;
}
class CompetitionOptionsPage {
  const CompetitionOptionsPage(this.items,
  this.page,
  this.last);
  final List<CompetitionOption> items;
  final int page;
  final bool last;
}
class DeleteSeasonsResult {
  const DeleteSeasonsResult(this.deletedIds,
  this.failures);
  final List<String> deletedIds;
  final List<SeasonDeleteFailure> failures;
}
class SeasonDeleteFailure {
  const SeasonDeleteFailure(this.seasonId,
  this.reason);
  final String seasonId;
  final String reason;
}
class SeasonInput {
  const SeasonInput({
    required this.name,
    required this.startDate,
    required this.endDate,
    this.competitionId,
    this.active = false
  });
  final String name;
  final DateTime startDate;
  final DateTime endDate;
  final String? competitionId;
  final bool active;
}
String dateOnly(DateTime value) => '${value.year.toString().padLeft(4, '0')}-'
'${value.month.toString().padLeft(2, '0')}-${value.day.toString().padLeft(2, '0')}';
String seasonStatusLabel(SeasonStatus status) => switch (status) {
  SeasonStatus.draft => 'Borrador',
  SeasonStatus.active => 'Activa',
  SeasonStatus.closed => 'Cerrada',
};
