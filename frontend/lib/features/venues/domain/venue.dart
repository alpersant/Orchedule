class Venue {
  const Venue({
    this.id,
    required this.name,
    required this.city,
    this.address,
    this.capacity,
    this.status,
    required this.active,
  });

  final String? id;
  final String name;
  final String city;
  final String? address;
  final int? capacity;
  final String? status;
  final bool active;

  factory Venue.fromJson(Map<String, dynamic> json) {
    return Venue(
      id: json['id']?.toString(),
      name: json['name'] as String? ?? '',
      city: json['city'] as String? ?? '',
      address: json['address'] as String?,
      capacity: (json['capacity'] as num?)?.toInt(),
      status: json['status']?.toString(),
      active: json['active'] as bool? ?? false,
    );
  }
}
