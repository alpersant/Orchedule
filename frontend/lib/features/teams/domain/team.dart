class Team {
  const Team({
    required this.id,
    required this.name,
    required this.active,
  });

  final String id;
  final String name;
  final bool active;

  factory Team.fromJson(Map<String, dynamic> json) {
    return Team(
      id: json['id'].toString(),
      name: json['name'] as String? ?? '',
      active: json['active'] as bool? ?? true,
    );
  }
}
