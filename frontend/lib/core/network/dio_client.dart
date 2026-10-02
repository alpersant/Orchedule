import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'auth_interceptor.dart';

const String kApiBaseUrl = 'http://localhost:8080';

final rawDioProvider = Provider<Dio>((ref) {
  return Dio(BaseOptions(
    baseUrl: kApiBaseUrl,
    connectTimeout: const Duration(seconds: 10),
    receiveTimeout: const Duration(seconds: 10),
  ));
});

final dioProvider = Provider<Dio>((ref) {
  final rawDio = ref.watch(rawDioProvider);

  final dio = Dio(BaseOptions(
    baseUrl: kApiBaseUrl,
    connectTimeout: const Duration(seconds: 10),
    receiveTimeout: const Duration(seconds: 10),
  ));

  dio.interceptors.add(AuthInterceptor(rawDio, ref));

  return dio;
});
