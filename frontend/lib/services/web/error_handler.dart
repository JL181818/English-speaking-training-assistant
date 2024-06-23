import 'package:dio/dio.dart';
// import 'package:yzm_flutter_driver/common/utils/app_log.dart';

/// 统一处理错误信息
String formatError(e) {
  if (e is Response) {
    // logger.d(e.toString());
    return e.statusMessage ?? '请求错误';
  }
  if (e is DioException) {
    String message = "";
    if (e.response != null && e.response!.statusCode != null) {
      switch (e.response!.statusCode) {
        case 400:
          message = "请求参数有误"; // signature Attestation of failure
          break;
        case 403:
          message = "请求被拒绝";
          break;
        case 404:
          message = "资源未找到";
          break;
        case 405:
          message = "请求方式不被允许";
          break;
        case 408:
          message = "请检查网络是否可用，再行尝试"; // 请求超时
          break;
        case 422:
          message = "请求语义错误";
          break;
        case 500:
          message = "服务器逻辑错误";
          break;
        case 502:
          message = "服务器网关错误";
          break;
        case 504:
          message = "服务器网关超时";
          break;
        default:
          message = "请求异常，请稍后再试";
      }
      return message;
    } else {
      switch (e.type) {
        case DioExceptionType.connectionTimeout:
          message = "连接超时";
          break;
        case DioExceptionType.sendTimeout:
          message = "请求超时";
          break;
        case DioExceptionType.receiveTimeout:
          message = "响应超时";
          break;
        case DioExceptionType.connectionError:
          message = "当前网络不可用，请检查你的网络...";
          break;
        default:
          if (e.message != null && e.message!.isNotEmpty) {
            message = e.message!;
          } else {
            message = "请求异常";
          }
          break;
      }
      return message;
    }
  } else if (e is TypeError) {
    return "数据解析类型转换异常";
  }
  return "未知错误";
}