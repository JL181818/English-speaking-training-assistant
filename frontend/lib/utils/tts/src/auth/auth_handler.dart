import 'dart:async';

import '../../flutter_azure_tts.dart';
import '../auth/auth_client.dart';
import '../auth/auth_response_mapper.dart';
import '../common/constants.dart';

///Handles authorisation token requests
class AuthHandler {
  AuthHandler({required this.authClient, required this.mapper});

  final AuthClient authClient;
  final AuthResponseMapper mapper;

  ///Request an authorisation token.
  ///
  /// Returns [AuthResponse]
  ///
  /// [TokenSuccess] : request succeeded
  ///
  /// [TokenFailure] : request failed
  ///
  Future<AuthResponse> getAuthToken() async {
    final response = await authClient.post(Uri.parse(Endpoints.issueToken));
    return mapper.map(response) as AuthResponse;
  }
}
