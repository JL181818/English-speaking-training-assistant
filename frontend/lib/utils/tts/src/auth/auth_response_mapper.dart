import '../auth/auth.dart';
import '../common/base_response.dart';
import '../common/base_response_mapper.dart';
import 'package:http/http.dart' as http;

class AuthResponseMapper extends BaseResponseMapper {
  @override
  BaseResponse map(http.Response response) {
    switch (response.statusCode) {
      case 200:
        return TokenSuccess(token: response.body);
      default:
        return TokenFailure(
            code: response.statusCode,
            reason: response.reasonPhrase ?? response.body);
    }
  }
}
