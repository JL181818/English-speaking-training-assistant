import 'dart:io';

import 'package:dio/io.dart';
import 'package:flutter/material.dart';
import 'package:path_provider/path_provider.dart';
import 'package:untitled/services/web/url.dart';
import 'package:video_player/video_player.dart';
import 'package:dio/dio.dart' as dio;

class DigitalHuman extends StatefulWidget{
  final String text;
  final String? role;
  const DigitalHuman({super.key, required this.text, this.role});
  @override
  State<StatefulWidget> createState() {
    return DigitalHumanState();
  }

}
class DigitalHumanState extends State<DigitalHuman>{
  VideoPlayerController? _controller;

  @override
  void initState() {
    super.initState();
    _download();
  }
  String _getUrl(){
    var text = widget.text;
    return '${UrlRouter.text2Video}?sentence=$text&role=${widget.role ?? 'boy'}';
  }
  _download() async {
    dio.Dio d = dio.Dio();
    (d.httpClientAdapter as DefaultHttpClientAdapter).onHttpClientCreate = (client) {
      client.badCertificateCallback = (cert, host, port) {
        return true;	// 返回true强制通过
      };
    };
    Directory tempDir = await getTemporaryDirectory();
    String tempPath = tempDir.path;
    var path = '$tempPath/${DateTime.now().second}.mp4';
    var res = await d.download(_getUrl(), path);
    _controller = VideoPlayerController.file(File(path))
      ..initialize().then((_) {
        setState(() {});
    });
  }
  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        Center(
            child: _controller!=null ? SizedBox(
              height: 150,
              child: _controller!.value.isInitialized
                  ? AspectRatio(
                aspectRatio: _controller!.value.aspectRatio,
                child: VideoPlayer(_controller!),
              )
                  : Container(),
            ):
            Container(
              child: const Text('视频加载中...', style: TextStyle(
                color: Colors.white
              ),),
            )
        ),
        Row(
          children: [
            Expanded(
              flex: 1,
              child: IconButton(
                onPressed: (){
                  _controller?.play();
                },
                icon: const Icon(Icons.play_arrow, color: Colors.white,)
              )
            ),
            Expanded(
              child: IconButton(
                  onPressed: (){
                    _controller?.pause();
                  },
                  icon: const Icon(Icons.pause, color: Colors.white,)
              )
            )
          ],
        )
      ],
    );
  }

}