import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'core/network/dio_client.dart';
import 'core/router/app_router.dart';
import 'features/auth/data/auth_api.dart';
import 'features/auth/domain/auth_state.dart';

void main() {
  runApp(
    ProviderScope(
      overrides: [
        authApiProvider.overrideWith((ref) => AuthApi(ref.watch(rawDioProvider))),
      ],
      child: const OrcheduleApp(),
    ),
  );
}

class OrcheduleApp extends ConsumerWidget {
  const OrcheduleApp({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final router = ref.watch(goRouterProvider);

    return MaterialApp.router(
      title: 'Orchedule',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        colorSchemeSeed: Colors.indigo,
        useMaterial3: true,
      ),
      routerConfig: router,
    );
  }
}
