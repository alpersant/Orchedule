import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../app/providers.dart';

Future<void> handleSessionExpired(Ref ref) async {
  ref.read(authProvider.notifier).forceLogout();
}