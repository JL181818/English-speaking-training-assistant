import 'dart:async';

import 'package:flutter/material.dart';
import 'package:dio/dio.dart' as dio;
import 'package:shared_preferences/shared_preferences.dart';
import '../services/web/ws_format.dart';
import '../services/web/url.dart';
import '../utils/voice2text/audio_recorder.dart';
import 'package:web_socket_client/web_socket_client.dart';

class MyRecorder extends StatelessWidget{
  final WebSocket? socket;
  final Function(String) show;
  const MyRecorder({super.key, this.socket, required this.show});
  Future<String> postRequestFunction(Map<String, dynamic> request) async {
    dio.Dio d = dio.Dio();
    dio.FormData formData = dio.FormData.fromMap(request);
    String url = UrlRouter.speechText;
    var response = await d.post(url, data: formData);
    Map<String, dynamic> res = response.data;
    bool success = res['success'];
    var result = res['content'];
    if(success) {
      return result;
    } else{
      return('error');
    }
  }

  Future<void> sendVoice(String path) async {
    // path = '/data/user/0/com.example.untitled/app_flutter/audio_1715567081203.m4a';
    Map<String, dynamic> request = {};
    request['file'] = await dio.MultipartFile.fromFile(path);
    Future<SharedPreferences> prefs0 = SharedPreferences.getInstance();
    SharedPreferences prefs = await prefs0;
    request['id'] = prefs.getString('phone');
    String res = await postRequestFunction(request);

    // var res = 'Can you tell me something about Trinity?';
    var cor = 'correction example';
    var points = 2;

    var requestMsg= WsFormat.getRequestMsg(res, cor, points);
    socket?.send(requestMsg);
    bool p = false;
    Timer.periodic(const Duration(seconds: 5), (timer) {
      if(p){
        timer.cancel();
      }
      socket?.send(requestMsg);
    });
    socket?.messages.listen((msg) {
      var rawMsg = msg as String;
      if(rawMsg.startsWith('ack')){
        if(WsFormat.checkResponse(rawMsg)){
          p = true;
        }
      }
    });
    print(res);
    show(res);
  }

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Recorder(
        onStop: (path){
          print(path);
          sendVoice(path);
        },
      ),
    );
  }

}