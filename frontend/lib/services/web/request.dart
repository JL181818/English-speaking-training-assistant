import 'dart:convert';

import 'package:dio/dio.dart';
import 'package:dio/io.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'error_handler.dart';
import 'interceptor.dart';

import '../../entities/normal_result.dart';

class YzmError {
  // late int errorCode;
  late String errorMsg;
}

class ApiClient {
  late Dio _dio;
  // String baseUrl = BaseConstants.baseUrl;
  // 私有构造函数
  ApiClient._internal() {
    _dio = Dio(BaseOptions(
        sendTimeout: const Duration(seconds: 10),
        connectTimeout: const Duration(seconds: 10),
        receiveTimeout: const Duration(seconds: 10)))
      ..interceptors.addAll([
        LogInterceptor(
          requestBody: true,
          responseBody: true,
        ),
        NetInterceptor(),
      ]); // 创建Dio实例
    (_dio.httpClientAdapter as DefaultHttpClientAdapter).onHttpClientCreate = (client) {
      client.badCertificateCallback = (cert, host, port) {
        return true;	// 返回true强制通过
      };
    };
  }

  static final ApiClient _instance = ApiClient._internal();

  // 工厂方法返回单例实例
  factory ApiClient() {
    return _instance;
  }

  /// POST请求
  Future<void> postRequest(
      {required String url,
        required Map<String, dynamic> request,
        required Function(Result) reqeustSuccess,
        required Function(YzmError) requestFail,
        Map<String, dynamic> headers = const <String, dynamic>{}}) async {
    const extra = <String, dynamic>{};
    final queryParameters = <String, dynamic>{};
    final data = <String, dynamic>{};
    data.addAll(request);
    Result value = Result(success: false, message: "message",
        loginValid: false
        // data: Map()
    );
    try {
      final result = await _dio
          .fetch<Map<String, dynamic>>(_setStreamType<Result<String>>(Options(
        method: 'POST',
        headers: headers,
        extra: extra,
      ).compose(
        _dio.options,
        url,
        queryParameters: queryParameters,
        data: json.encode(data),
      )
      ));
      _requestInfo(value, result, reqeustSuccess, requestFail);
    } catch (e) {
      // e= 网络错误，解析数据异常,其它
      YzmError error = YzmError();
      // error.errorCode = 0;
      error.errorMsg = formatError(e);
      requestFail(error);
      return;
    }
  }

  /// dio的参数解析
  RequestOptions _setStreamType<T>(RequestOptions requestOptions) {
    if (T != dynamic &&
        !(requestOptions.responseType == ResponseType.bytes ||
            requestOptions.responseType == ResponseType.stream)) {
      if (T == String) {
        requestOptions.responseType = ResponseType.plain;
      } else {
        requestOptions.responseType = ResponseType.json;
      }
    }
    return requestOptions;
  }

  YzmError _requestError(Result value) {
    YzmError error = YzmError();
    // error.errorCode = value.code;
    error.errorMsg = value.message ?? "";
    return error;
  }

  // 网络请求返回信息通用
  _requestInfo(
      value,
      result,
      Function(Result) reqeustSuccess,
      Function(YzmError) requestFail,
      ) async {
    // 网络请求错误相关异常
    if (result.statusCode != 200) {
      YzmError error = YzmError();
      // error.errorCode = 0;
      DioException dioException =
      DioException(requestOptions: result.requestOptions, response: result);
      error.errorMsg = formatError(dioException);
      requestFail(error);
      return;
    }
    // 解析数据
    value = Result.fromJson(result.data!);
    // 返回的业务异常
    if (!value.success) {
      requestFail(_requestError(value));
      return;
    }
    // 返回成功
    Future<SharedPreferences> prefs0 = SharedPreferences.getInstance();
    SharedPreferences prefs = await prefs0;
    if(value.token != null){
      // prefs.setString("token", '114514');
      prefs.setString("token", value.token);
    }

    reqeustSuccess(value);
  }
}