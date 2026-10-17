class PlayingField {
  final String id;
  final String venueId;
  final String name;
  final String status;

  const PlayingField({required this.id, required this.venueId,
    required this.name, required this.status});

  bool get active => status == 'ACTIVE';

  factory PlayingField.fromJson(Map<String, dynamic> json) => PlayingField(
    id: json['id'] as String,
    venueId: json['venueId'] as String,
    name: json['name'] as String,
    status: json['status'] as String,
  );
}
