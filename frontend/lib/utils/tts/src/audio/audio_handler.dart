import './audio_client.dart';
import './audio_request_param.dart';
import './audio_response_mapper.dart';
import './audio_responses.dart';
import '../auth/authentication_types.dart';
import '../common/config.dart';
import '../common/constants.dart';
import '../ssml/ssml.dart';
import 'package:http/http.dart' as http;

import 'audio_type_header.dart';

class AudioHandler {
  Future<AudioSuccess> getAudio(AudioRequestParams params) async {
    final mapper = AudioResponseMapper();
    final audioClient = AudioClient(
        client: http.Client(),
        authHeader: BearerAuthenticationHeader(token: Config.authToken!.token),
        audioTypeHeader: AudioTypeHeader(audioFormat: params.audioFormat));

    try {
      final ssml =
          Ssml(voice: params.voice, text: params.text, speed: params.rate ?? 1);

      final response = await audioClient.post(Uri.parse(Endpoints.audio),
          body: ssml.buildSsml);
      final audioResponse = mapper.map(response);
      if (audioResponse is AudioSuccess) {
        return audioResponse;
      } else {
        throw audioResponse;
      }
    } catch (e) {
      rethrow;
    }
  }
}
