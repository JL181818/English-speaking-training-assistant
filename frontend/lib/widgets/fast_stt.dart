import 'dart:async';
import 'dart:io';
import 'dart:convert';

import 'package:dio/io.dart';
import 'package:flutter/material.dart';
import 'package:untitled/entities/speech2text.dart';
import 'package:untitled/services/web/request.dart';
import '../services/web/url.dart';
import '../services/web/ws_format.dart';
import 'package:web_socket_client/web_socket_client.dart';
import 'package:speech_to_text/speech_to_text.dart' as stt;
import 'package:record/record.dart';
import 'package:dio/dio.dart' as dio;
import 'package:flutter/foundation.dart';
import 'package:http/http.dart' as http;

import '../utils/voice2text/platform/audio_recorder_io.dart';

class FastSTT extends StatefulWidget{
  final WebSocket? socket;
  final Function(String) show;
  final Function(String ,double, List) addition;
  final Function(bool) setSpeakingState;
  final Function(String) setRealTimeText;
  const FastSTT({
    super.key, this.socket,
    required this.show,
    required this.addition,
    required this.setRealTimeText,
    required this.setSpeakingState
  });
  @override
  State<StatefulWidget> createState() {
    return SSTState();
  }

}
class SSTState extends State<FastSTT> with AudioRecorderMixin {
  late final AudioRecorder _audioRecorder;
  late stt.SpeechToText _speech;
  bool _isListening = false;
  String _text = '';

  @override
  void initState() {
    super.initState();
    _audioRecorder = AudioRecorder();
    _speech = stt.SpeechToText();
  }
  _toServer(res, cor, points){
    var requestMsg= WsFormat.getRequestMsg(res, cor, points);
    widget.socket?.send(requestMsg);
    bool p = false;
    Timer.periodic(const Duration(seconds: 5), (timer) {
      if(p){
        timer.cancel();
      }
      widget.socket?.send(requestMsg);
    });
    widget.socket?.messages.listen((msg) {
      var rawMsg = msg as String;
      if(rawMsg.startsWith('ack')){
        if(WsFormat.checkResponse(rawMsg)){
          p = true;
        }
      }
    });
    print('res: $res');
    print('cor: $cor');
    print('points: $points');
    // widget.show(res);
  }
  Future<bool> _isEncoderSupported(AudioEncoder encoder) async {
    final isSupported = await _audioRecorder.isEncoderSupported(
      encoder,
    );

    if (!isSupported) {
      debugPrint('${encoder.name} is not supported on this platform.');
      debugPrint('Supported encoders are:');

      for (final e in AudioEncoder.values) {
        if (await _audioRecorder.isEncoderSupported(e)) {
          debugPrint('- ${encoder.name}');
        }
      }
    }

    return isSupported;
  }
  Future<void> _startRecord() async {
    try {
      if (await _audioRecorder.hasPermission()) {
        const encoder = AudioEncoder.wav;

        if (!await _isEncoderSupported(encoder)) {
          return;
        }

        final devs = await _audioRecorder.listInputDevices();
        debugPrint(devs.toString());

        const config = RecordConfig(encoder: encoder, numChannels: 1);

        // Record to file
        await recordFile(_audioRecorder, config);
      }
    } catch (e) {
      if (kDebugMode) {
        print(e);
      }
    }
  }
  Future<void> _listen() async {
    if (!_isListening) {
      // 录音
      _startRecord();
      setState(() => _isListening = true);
      // bool available = await _speech.initialize(
      //   onStatus: (val) => print('onStatus: $val'),
      // );
      // if (available) {
      //   setState(() => _isListening = true);
      //   widget.setSpeakingState(_isListening);
      //
      //   // 转文字
      //   // _speech.listen(
      //   //   onResult: (val) {
      //   //     print(_text);
      //   //     _text = val.recognizedWords;
      //   //     widget.setRealTimeText(_text);
      //   //   },
      //   //   cancelOnError: false, // 不停止监听出错
      //   //   partialResults: true, // 允许部分结果
      //   //   localeId: 'en_US',
      //   // );
      //
      //   Future.delayed(const Duration(minutes: 10), _stopListening);
      // } else {
      //   setState(() => _isListening = false);
      // }
    }
  }
  Future<String> azureSpeechRecognition(File file) async {
    var url = 'eastus.stt.speech.microsoft.com';
    var unencodedPath = 'speech/recognition/conversation/cognitiveservices/v1';
    var queryParameters = {'language':'en-US'};
    var response = await http.post(
        Uri.https(url, unencodedPath, queryParameters),
        headers: {
          'Ocp-Apim-Subscription-Key':'c85d9bf805634270a809c0618689c677',
          'Content-Type':'audio/wav'
        },
        body: file.readAsBytesSync()
    );
    var json = jsonDecode(response.body);
    var res = SpeechResult.fromJson(json);
    return res.displayText ?? 'no content';
  }

  Future<Speech2TextEntity> getPointsCorrection(Map<String, dynamic> request)
  async {
    dio.Dio d = dio.Dio();
    (d.httpClientAdapter as DefaultHttpClientAdapter).onHttpClientCreate = (client) {
      client.badCertificateCallback = (cert, host, port) {
        return true;	// 返回true强制通过
      };
    };
    dio.FormData formData = dio.FormData.fromMap(request);
    String url = UrlRouter.speech2Text;
    var response = await d.post(url, data: formData);
    Map<String, dynamic> res = response.data;
    return Speech2TextEntity(res);
  }

  Future<void> _stopListening() async {
    if (_isListening) {
      // 即时显示语音文本
      // await _speech.stop();
      // azureSpeechRecognition(file)


      setState(() => _isListening = false);
      widget.setSpeakingState(_isListening);
      // 获取修正和分数
      final path = await _audioRecorder.stop();
      if(path != null){
        // 获取文本
        var file = File(path);
        _text = await azureSpeechRecognition(file);
        widget.show(_text);
        // 获取矫正和得分
        Map<String, dynamic> request = {};
        request['file'] = await dio.MultipartFile.fromFile(path);
        var data = await getPointsCorrection(request);
        var cor = data.correction;
        var points = data.score;
        var words = data.wrongWords;
        _toServer(_text, cor, points);
        widget.addition(cor, points, words);
      }
    }
  }
  @override
  Widget build(BuildContext context) {
    return Center(
      child: IconButton(
        onPressed: _isListening ? _stopListening : _listen,
        icon: Icon(_isListening ? Icons.stop : Icons.mic),
      ),
    );
  }

}
