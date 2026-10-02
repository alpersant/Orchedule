import 'package:dio/dio.dart';
import 'auth_interceptor.dart';
import '../storage/secure_storage.dart';

class ApiClient {
  final Dio dio;

  ApiClient(String baseUrl, SecureStorage storage)
      : dio = Dio(BaseOptions(
    baseUrl: baseUrl,
    connectTimeout: const Duration(seconds: 10),
    receiveTimeout: const Duration(seconds: 10),
    headers: {'Content-Type': 'application/json'},
  )) {
    final refreshDio = Dio(BaseOptions(baseUrl: baseUrl));
    dio.interceptors.add(AuthInterceptor(storage, refreshDio));
  }
}