import 'dart:convert';
import 'package:dio/dio.dart';
import '../../entities/normal_result.dart';
import '../auth/auth_services.dart';



/// 请求拦截相关的处理
class NetInterceptor extends Interceptor {
  NetInterceptor();

  @override
  void onRequest(RequestOptions options, RequestInterceptorHandler handler) {
    if (!options.headers.containsKey("token")) {
      final token = AuthService.token;
      if (token != null && token.isNotEmpty) {
        options.headers['token'] = token;
      }
    }
    handler.next(options);
  }

  @override
  void onResponse(Response response, ResponseInterceptorHandler handler) {
    int? statusCode = response.statusCode;
    if (statusCode == 200) {
      Map dataMap;
      if (response.data != null) {
        if (response.data is Map<String, dynamic>) {
          dataMap = response.data as Map;
        } else if (response.data is String) {
          dataMap = jsonDecode(response.data) as Map;
        } else {
          dataMap = {'code': 200, 'data': response.data, 'message': 'success'};
        }
        final result = wrapResult(dataMap as Map<String, dynamic>);
      }
    }

    handler.next(response);
  }

  @override
  void onError(DioException err, ErrorInterceptorHandler handler) {
    return handler.next(err);
  }
}

// 首先，你需要定义一个函数来根据数据类型将解析后的数据包装为 Result 对象：
Result<T> wrapResult<T>(Map<String, dynamic> dataMap) {
  return Result<T>(
    success: dataMap['success'],
    message: dataMap['message'],
    loginValid: dataMap['loginValid'],

    // data: dataMap['data'], // Adjust this based on your data structure
  );
}