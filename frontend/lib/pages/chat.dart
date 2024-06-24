import 'dart:convert';
import 'dart:io';


import 'package:audioplayers/audioplayers.dart';
import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter_chat_types/flutter_chat_types.dart' as types;
import 'package:flutter_chat_ui/flutter_chat_ui.dart';
import 'package:fluttertoast/fluttertoast.dart';
import 'package:get/get.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:untitled/widgets/digital_human.dart';
import 'package:video_player/video_player.dart';
import '../services/web/request.dart';
import '../widgets/fast_stt.dart';
import '../pages/history_list.dart';
import '../services/web/url.dart';
import '../services/web/ws_format.dart';
import 'package:uuid/uuid.dart';
import 'package:web_socket_client/web_socket_client.dart';


class MyChatPage extends StatefulWidget {
  final List<DialogItem>? dialog;
  final String token;
  const MyChatPage({super.key, this.dialog, required this.token});
  @override
  State<StatefulWidget> createState() {
    return _MyChatState();
  }

}
class _MyChatState extends State<MyChatPage>{
  @override
  Widget build(BuildContext context) => MaterialApp(
    debugShowCheckedModeBanner: false,
    home: Directionality(
      textDirection: TextDirection.ltr,
      child: widget.dialog==null ?
      ChatPage(token: widget.token,) :
      ChatPage(
        dialog: widget.dialog,
        token: widget.token,
      ),
    ),
  );
}

class ChatPage extends StatefulWidget {
  final String token;
  final List<DialogItem>? dialog;
  const ChatPage({super.key, this.dialog, required this.token});

  @override
  State<ChatPage> createState() => _ChatPageState();
}

class _ChatPageState extends State<ChatPage> {
  List<types.CustomMessage> _messages = [];
  Set<String> expandedId = {};
  // late VideoPlayerController _controller;
  bool isPlayingVideo = false;
  String videoText = "";
  String playingMsgId = "";

  final AudioPlayer _audioPlayer = AudioPlayer();
  final _user = const types.User(
    firstName: 'Matthew',
    id: '82091008-a484-4a89-ae75-a22bf8d6f3ac',
    lastName: 'White'
  );
  final _ai = const types.User(
      firstName: 'Arona',
      id: '4c2307ba-3d40-442f-b1ff-b271f63904ca',
  );
  late WebSocket socket;
  bool _isSpeaking = false;
  String _text = '';

  @override
  void initState() {
    super.initState();

    if(widget.dialog==null){
      setSocket();
      // _loadMessagesFromJson();
    }else{
      _loadMessagesFromHistory();
    }

  }
  _updateText(text){
    _text = text;
    setState(() { });
  }
  _setSpeakingState(p){
    _isSpeaking = p;
    setState(() { });
  }

  void showBottomSheet(String word, String trans, String example,
      String exampleTrans) {
    //用于在底部打开弹框的效果
    showModalBottomSheet(builder: (BuildContext context) {
      //构建弹框中的内容
      return buildBottomSheetWidget(context, word, trans,
          example, exampleTrans);
    }, context: context);
  }
  Widget buildBottomSheetWidget(BuildContext context, String word,
      String trans, String example, String exampleTrans) {
    return Container(
      height: 300,
      width: MediaQuery.of(context).size.width,
      margin: const EdgeInsets.all(20),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Text(
                word,
                style: const TextStyle(fontSize: 20),
              ),
              IconButton(
                onPressed: () async {
                  await _audioPlayer.play(UrlSource('${UrlRouter.getAudio}/$word.mp3'));
                },
                icon: const Icon(Icons.record_voice_over_rounded))
            ],
          ),
          Text(
            '【翻译】：$trans',
            style: const TextStyle(fontSize: 15, ),
          ),
          Text(
            '【例句】：$example',
            style: const TextStyle(fontSize: 15, ),
          ),
          Text(
            '【例句翻译】：$exampleTrans',
            style: const TextStyle(fontSize: 15, ),
          ),
        ],
      )
    );
  }

  List<TextSpan> textSpanBuilder(types.CustomMessage cm){
    List<TextSpan> res = [];
    String text = cm.metadata?['text'];
    List<String> words = text.split(' ');
    List? wrongWords = cm.metadata?['wrongWords'];
    Set<String> wwords = {};
    if(wrongWords!=null){
      for(var wrongWord in wrongWords){
        wwords.add(wrongWord['word']);
      }
    }

    for(var word in words){
      res.add(
          TextSpan(
              style: TextStyle(
                fontSize: 15,
                color: wwords.contains(word) ? Colors.red : Colors.black
              ),
              text: '$word ',
              recognizer: TapGestureRecognizer()..onTap = () async {
                print(word);
                Future<SharedPreferences> prefs0 = SharedPreferences.getInstance();
                SharedPreferences prefs = await prefs0;
                var token = prefs.getString('token');
                await ApiClient().postRequest(
                    url: UrlRouter.getWord,
                    request: {
                      'token': token,
                      'word': word
                    },
                    reqeustSuccess: (res) {
                      if(res.success){
                        var content = res.content;
                        if(content != null){
                          String word = content['word'];
                          String trans = content['trans'];
                          String example = content['example'];
                          String exampleTrans = content['exampleTrans'];
                          showBottomSheet(word, trans, example, exampleTrans);
                        }
                      }else{
                        Fluttertoast.showToast(
                          msg: 'err: ${res.message}',
                          backgroundColor: Colors.red,
                        );
                      }
                    },
                    requestFail: (e) {
                      Fluttertoast.showToast(
                        msg: '请检查网络',
                        backgroundColor: Colors.red,
                      );
                    }
                );
              }
          )
      );
    }
    return res;
  }
  Widget myMessageBuilder(types.CustomMessage cm, {required int messageWidth}){
    String text = cm.metadata?['text'];

    print(messageWidth);
    return Column(
      children: [
        SizedBox(
          height: 30,
          child: IconButton(
              onPressed: (){
                videoText = text;
                isPlayingVideo = !isPlayingVideo;
                playingMsgId = cm.id;
                setState(() { });
              },
              icon: Icon(
                  isPlayingVideo && playingMsgId==cm.id ?
                  Icons.cancel: Icons.play_circle
              )
          ),
        ),
        Container(
          width: 320,
          alignment: Alignment.center,
          margin: EdgeInsets.only(left: 20, right: 20),
          child: Text.rich(
            TextSpan(
                children: textSpanBuilder(cm)
            ),
          ),
        ),
        const Divider(height: 1.0,indent: 6.0,color: Colors.black12,),
        cm.author==_user ? Expansion(cm: cm): Container(),

      ],
    );

  }

  setSocket() {

    var token = widget.token;
    socket = WebSocket(Uri.parse('${UrlRouter.websocket}/$token'));
    socket.messages.listen((rawMsg) async {
      // print(rawMsg as String);
      if(rawMsg.startsWith('say')){
        /// 回应ack
        var seq = WsFormat.getAISeq(rawMsg);
        socket.send('ack#say#$seq');
        /// 显示内容
        var msg = WsFormat.getAIContent(rawMsg);
        final textMessage = types.CustomMessage(
          author: _ai,
          createdAt: DateTime.now().millisecondsSinceEpoch,
          id: const Uuid().v4(),
          metadata: {'text': msg}
        );
        _addMessage(textMessage);
      }else if(rawMsg.startsWith('token')){
        var seq = WsFormat.getAISeq(rawMsg);
        var token = WsFormat.getAIContent(rawMsg);
        /// 更新token
        Future<SharedPreferences> prefs0 = SharedPreferences.getInstance();
        SharedPreferences prefs = await prefs0;
        prefs.setString('token', token);
        /// 回应ack
        socket.send('ack#say#$seq');
      }
    });
  }



  void _addMessage(types.CustomMessage message) {
    setState(() {
      _messages.insert(0, message);
    });
  }

  void _handleAttachmentPressed() {
    print('AttachmentPressed:');
    socket.close();
  }

  // void _handleFileSelection() async {
  //   // print('111');
  // }
  //
  // void _handleImageSelection() async {
  //   final result = await ImagePicker().pickImage(
  //     imageQuality: 70,
  //     maxWidth: 1440,
  //     source: ImageSource.gallery,
  //   );
  //
  //   if (result != null) {
  //     final bytes = await result.readAsBytes();
  //     final image = await decodeImageFromList(bytes);
  //
  //     final message = types.ImageMessage(
  //       author: _user,
  //       createdAt: DateTime.now().millisecondsSinceEpoch,
  //       height: image.height.toDouble(),
  //       id: const Uuid().v4(),
  //       name: result.name,
  //       size: bytes.length,
  //       uri: result.path,
  //       width: image.width.toDouble(),
  //     );
  //
  //     _addMessage(message);
  //   }
  // }

  // void _handleMessageTap(BuildContext _, types.Message message) async {
  //   if (message is types.FileMessage) {
  //     var localPath = message.uri;
  //
  //     if (message.uri.startsWith('http')) {
  //       try {
  //         final index =
  //         _messages.indexWhere((element) => element.id == message.id);
  //         final updatedMessage =
  //         (_messages[index] as types.FileMessage).copyWith(
  //           isLoading: true,
  //         );
  //
  //         setState(() {
  //           _messages[index] = updatedMessage;
  //         });
  //
  //         final client = http.Client();
  //         final request = await client.get(Uri.parse(message.uri));
  //         final bytes = request.bodyBytes;
  //         final documentsDir = (await getApplicationDocumentsDirectory()).path;
  //         localPath = '$documentsDir/${message.name}';
  //
  //         if (!File(localPath).existsSync()) {
  //           final file = File(localPath);
  //           await file.writeAsBytes(bytes);
  //         }
  //       } finally {
  //         final index =
  //         _messages.indexWhere((element) => element.id == message.id);
  //         final updatedMessage =
  //         (_messages[index] as types.FileMessage).copyWith(
  //           isLoading: null,
  //         );
  //
  //         setState(() {
  //           _messages[index] = updatedMessage;
  //         });
  //       }
  //     }
  //
  //     await OpenFilex.open(localPath);
  //   }
  // }

  // void _handlePreviewDataFetched(
  //     types.TextMessage message,
  //     types.PreviewData previewData,
  //     ) {
  //   final index = _messages.indexWhere((element) => element.id == message.id);
  //   final updatedMessage = (_messages[index] as types.TextMessage).copyWith(
  //     previewData: previewData,
  //   );
  //
  //   setState(() {
  //     _messages[index] = updatedMessage;
  //   });
  // }

  void _handleSendPressed(String message) {
    final textMessage = types.CustomMessage(
      author: _user,
      createdAt: DateTime.now().millisecondsSinceEpoch,
      id: const Uuid().v4(),
      metadata: {'text': message}
    );
    // socket.send(message);
    _addMessage(textMessage);
  }

  void _addCorrectionScore(String corr, double score, List wrongWords){
    _messages[0].metadata?['correction'] = corr;
    _messages[0].metadata?['score'] = score;
    _messages[0].metadata?['wrongWords'] = wrongWords;
    setState(() { });
  }

  // void _loadMessagesFromJson() async {
  //   final response = await rootBundle.loadString('assets/json/simple.json');
  //   final messages = (jsonDecode(response) as List)
  //       .map((e) => types.Message.fromJson(e as Map<String, dynamic>))
  //       .toList();
  //
  //   setState(() {
  //     _messages = messages;
  //   });
  // }
  void _loadMessagesFromHistory(){
    for(var item in widget.dialog!){
      var textMessage = types.CustomMessage(
        author: item.isUser ? _user : _ai,
        createdAt: item.time,
        id: const Uuid().v4(),
        metadata: {
          'text': item.content,
          'score': item.score,
          'correction': item.correction
        }
      );
      _messages.insert(0, textMessage);
    }
    setState(() { });
  }
  Widget getBottomWidget(){
    if(widget.dialog != null){
      return const Text('历史记录');
    }else{
      // return MyRecorder(socket: socket, show: _handleSendPressed,);
      return FastSTT(
        socket: socket,
        show: _handleSendPressed,
        addition: _addCorrectionScore,
        setRealTimeText: _updateText,
        setSpeakingState: _setSpeakingState,
      );
    }
  }
  @override
  Widget build(BuildContext context) => Scaffold(
    appBar: AppBar(
      leading: IconButton(
        onPressed: () {
          widget.dialog ?? socket.close();
          // Get.back(result: true);
          Get.offAll(ListViewPage());
        },
        icon: const Icon(Icons.arrow_back_ios),

      ),
      title: widget.dialog==null ? const Text('新对话') : const Text('历史对话'),
    ),
    body: Stack(
      alignment: Alignment.center,
      children: [
        Chat(
          customMessageBuilder: myMessageBuilder,
          messages: _messages,
          // onAttachmentPressed: _handleAttachmentPressed,
          // onMessageTap: _handleMessageTap,
          // onPreviewDataFetched: _handlePreviewDataFetched,
          // onSendPressed: _handleSendPressed,
          onSendPressed: (e)=>{},
          showUserAvatars: true,
          showUserNames: true,
          user: _user,
          customBottomWidget: Padding(
            padding: const EdgeInsets.all(20),
            child: getBottomWidget(),
          ),
        ),
        isPlayingVideo ? Positioned(
          top: 20,
          child: Container(
            padding: EdgeInsets.only(top: 10),
            alignment: Alignment.center,
            // margin: EdgeInsets.only(top: 10),
            decoration: const BoxDecoration(
              color: Colors.black54,
              borderRadius: BorderRadius.all(Radius.circular(18.0)),
            ),
            width: 200,
            height: 300,
            child: DigitalHuman(text: videoText,),
            // color: Colors.red,
          )
        ) : Container()
        // _isSpeaking ? Container(
        //   height: 150,
        //   width: 250,
        //   alignment: Alignment.center,
        //   decoration: const BoxDecoration(
        //     //背景颜色
        //     color: Colors.black54,
        //     //圆角半径
        //     borderRadius: BorderRadius.all(Radius.circular(18.0)),
        //   ),
        //   child: Text(
        //       _text,
        //       style: const TextStyle(
        //         fontSize: 20,
        //         color: Colors.white
        //       )
        //   ),
        // ) : Container()
      ],
    ),
  );
}
class Expansion extends StatefulWidget{
  final types.CustomMessage cm;
  const Expansion({super.key, required this.cm});
  @override
  State<StatefulWidget> createState() {
    return ExpansionState();
  }

}
class ExpansionState extends State<Expansion>{
  bool isOpen = false;
  String _getCorrection(){
    String text = widget.cm.metadata?['text'];
    text = text.replaceAll(',', '').replaceAll('.', '').replaceAll(';', '')
        .replaceAll(':', '').replaceAll('"', '').replaceAll("'", '');
    text = text.toLowerCase();
    String? corr = widget.cm.metadata?['correction'];
    if(corr == null || text.compareTo(corr.toLowerCase())==0){
      return '无';
    }else{
      return corr;
    }
  }
  Widget _getWrongWords(){
    List<Widget> children = [];
    var words = widget.cm.metadata?['wrongWords'];
    if(words!=null){
      for(var word in words){
        children.add(Text('${word['word']}: ${word['score']}'));
      }
    }
    return Column(children: children,);
  }
  @override
  Widget build(BuildContext context) {
    return Container(
      color: Color(0xBFB170FF),
      width: 320,
      // color: Colors.red,
      // margin: EdgeInsets.only(left: 20, right: 20),
      child: isOpen ?
      Column(

        // crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text('得分：${widget.cm.metadata?['score'] ?? '暂无'}'),
          Text('纠正：${_getCorrection()}'),
          _getWrongWords(),
          SizedBox(
            height: 30,
            child: IconButton(
                color: Colors.white,
                onPressed: (){
                  isOpen = false;
                  setState(() { });
                },
                icon: const Icon(Icons.arrow_drop_up)
            ),
          )
        ],
      ) :
      SizedBox(
        height: 30,
        child: IconButton(
          color: Colors.white,
            onPressed: (){
              isOpen = true;
              setState(() { });
            },
            icon: const Icon(Icons.arrow_drop_down)
        ),
      )
    );
  }

}