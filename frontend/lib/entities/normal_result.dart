import 'package:json_annotation/json_annotation.dart';

@JsonSerializable()
class Result<T> {
  @JsonKey(name: 'success')
  bool success;
  @JsonKey(name: 'message')
  String? message;
  @JsonKey(name: 'token')
  String? token;
  @JsonKey(name: 'loginValid')
  bool? loginValid;
  @JsonKey(name: 'content')
  dynamic content;

  Result({
    required this.success,
    required this.message,
    this.token,
    required this.loginValid,
    this.content
  });

  factory Result.fromJson(Map<String, dynamic> json) {
    return Result(
      success: json['success'] as bool,
      message: json['message'] == null ? '' : json['message'] as String,
      token: json["token"] == null ? '' : json["token"] as String,
      loginValid: json["loginValid"] == null ? false : json["loginValid"] as bool,
      content: json['content'] as dynamic,
      // data: json['data'] as dynamic,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'success': success,
      'msg': message,
      // 'data': data,
    };
  }
}