import 'package:flutter/material.dart';
import 'package:fluttertoast/fluttertoast.dart';
import 'package:get/get.dart';
import 'package:group_list_view/group_list_view.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:intl/date_symbol_data_local.dart';

import '../services/web/request.dart';
import '../services/web/url.dart';
import '../pages/chat.dart';
import 'login.dart';

// void main() => runApp(ListViewPage());
int pageNum = 0;
int pageSize = 10;

class ItemData{
  ItemData({required this.title, required this.id});
  String title;
  String id;
}

class DialogItem{
  DialogItem({
    required this.time,
    required this.content,
    required this.isUser,
    required this.correction,
    required this.score
  });
  int time;
  String content;
  bool isUser;
  String correction;
  double score;
}

bool loadSwitch = true;
class ListViewPage extends StatefulWidget{
  const ListViewPage({super.key});

  @override
  State<StatefulWidget> createState() {
    return _ListViewState();
  }

}
class _ListViewState extends State<ListViewPage> {
  final ScrollController _scrollController = ScrollController();
  final List<List> _elements = [];
  final List<String> _times = [];
  final List<DialogItem> _dialog = [];

  getListData() async {
    // pageNum++;
    Map<String, dynamic> request = {};
    request['pagenum'] = pageNum;
    request['pagesize'] = pageSize;
    Future<SharedPreferences> prefs0 = SharedPreferences.getInstance();
    SharedPreferences prefs = await prefs0;
    var token = prefs.getString('token');
    request['token'] = token;
    await ApiClient().postRequest(
      url: UrlRouter.getList,
      request: request,
      reqeustSuccess: (result) {
        if(result.success){
          List list = result.content;
          for(var item in list){
            String id = item['id'];
            String timeUtc = item['time'];
            String time = DateTime.parse(timeUtc).toLocal().toString();
            time = time.substring(0, 10);
            List dialogs = item['dialogs'];
            String title;
            if(dialogs.length < 2){
              title = "Hello, I'm...";
            }else{
              title = dialogs[1]['content'];
              if(title.length > 10){
                title = '${title.substring(0, 10)}...';
              }
            }
            int index = _times.indexOf(time);
            if(index == -1){
              _times.add(time);
              _elements.add([ItemData(title: title, id: id)]);
            }else{
              _elements[index].add(ItemData(title: title, id: id));
            }
          }
          setState(() { });
        }else{
          Fluttertoast.showToast(
            msg: 'err: ${result.message}',
            toastLength: Toast.LENGTH_SHORT,
            backgroundColor: Colors.red,
          );
        }
      },
      requestFail: (e) {
        Fluttertoast.showToast(
          msg: e.errorMsg,
          toastLength: Toast.LENGTH_SHORT,
          backgroundColor: Colors.red,
        );
      },
    );
  }
  getDetails(String id) async {
    _dialog.clear();
    Map<String, dynamic> request = {};
    request['id'] = id;
    Future<SharedPreferences> prefs0 = SharedPreferences.getInstance();
    SharedPreferences prefs = await prefs0;
    var token = prefs.getString('token');
    request['token'] = token;
    await ApiClient().postRequest(
      url: UrlRouter.getDetail,
      request: request,
      reqeustSuccess: (result){
        if(result.success){
          List list = result.content['dialogs'];
          for(var item in list){
            var timeUtc = item['time'];
            var time = DateTime.parse(timeUtc).millisecondsSinceEpoch;
            String content = item['content'];
            if(content.startsWith('say#')){
              content = content.substring(content.lastIndexOf('#')+1);
            }
            var sender = item['sender']['phone'];
            bool isUser = sender==prefs.getString('phone');
            double score = item['score'];
            String correction = item['correction'];
            _dialog.add(
                DialogItem(
                    time: time, content: content, isUser: isUser,
                    score: score, correction: correction
                )
            );
          }
        }else{
          Fluttertoast.showToast(
            msg: 'err: ${result.message}',
            toastLength: Toast.LENGTH_SHORT,
            backgroundColor: Colors.red,
          );
        }
      },
      requestFail: (e){
        Fluttertoast.showToast(
          msg: e.errorMsg,
          toastLength: Toast.LENGTH_SHORT,
          backgroundColor: Colors.red,
        );
      }
    );
  }
  _loadMore() async {
    pageNum++;
    getListData();
    await Future.delayed(const Duration(seconds: 1), (){
      loadSwitch = true;
    });
  }
  _refresh() async {
    pageNum = 0;
    _elements.clear();
    _times.clear();
    await getListData();
  }
  @override
  void initState() {
    super.initState();
    getListData();
  }
  @override
  Widget build(BuildContext context) {
    _scrollController.addListener(() {
      if (_scrollController.position.pixels ==
          _scrollController.position.maxScrollExtent) {
        /// 滑动到了底部
        if(loadSwitch){
          _loadMore();
          loadSwitch = false;
        }
      }
    });
    return Scaffold(
      appBar: AppBar(
        title: const Text('我的训练'),
        centerTitle: true,
        backgroundColor: Colors.blue[700],
        actions: [
          IconButton(
            onPressed: () async {
              SharedPreferences preferences = await SharedPreferences.getInstance();
              await preferences.clear();
              Get.offAll(const LoginPage());
            },
            icon: const Icon(Icons.logout),
            color: Colors.white,
          )
        ],
      ),
      body: RefreshIndicator(
        onRefresh: () async {
          await _refresh();
        },
        child: GroupListView(
          physics: const AlwaysScrollableScrollPhysics(),
          controller: _scrollController,
          sectionsCount: _times.length,
          countOfItemInSection: (int section) {
            return _elements[section].length;
          },
          itemBuilder: _itemBuilder,
          groupHeaderBuilder: (BuildContext context, int section) {
            return Padding(
              padding: const EdgeInsets.symmetric(horizontal: 15, vertical: 8),
              child: Text(
                _times[section],
                style: const TextStyle(fontSize: 18, fontWeight: FontWeight.w600),
              ),
            );
          },
          separatorBuilder: (context, index) => const SizedBox(height: 10),
          sectionSeparatorBuilder: (context, section) => const SizedBox(height: 10),
        ),
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: () async {
          Future<SharedPreferences> prefs0 = SharedPreferences.getInstance();
          SharedPreferences prefs = await prefs0;
          var token = prefs.getString('token');
          if(token!=null){
            initializeDateFormatting().then(
                    (_)=>Get.to( MyChatPage(token: token,))
            );
          }
        },
        child: const Icon(Icons.add),
      ),
    );
  }

  Widget _itemBuilder(BuildContext context, IndexPath index) {
    ItemData itemData = _elements[index.section][index.index];
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 8.0),
      child: Card(
        elevation: 8,
        child: ListTile(
          contentPadding:
          const EdgeInsets.symmetric(horizontal: 18, vertical: 10.0),
          leading: CircleAvatar(
            backgroundColor: _getAvatarColor(itemData.title),
            child: Text(
              '${index.index + 1}',
              style: const TextStyle(color: Colors.white, fontSize: 18),
            ),
          ),
          title: Text(
            itemData.title,
            style: const TextStyle(fontSize: 16, fontWeight: FontWeight.w400),
          ),
          trailing: const Icon(Icons.arrow_forward_ios),
          onTap: () async {
            Future<SharedPreferences> prefs0 = SharedPreferences.getInstance();
            SharedPreferences prefs = await prefs0;
            var token = prefs.getString('token');

            if(token!=null){
              await getDetails(itemData.id);
              initializeDateFormatting().then(
                (_)=>Get.to(MyChatPage(dialog: _dialog, token: token,))
              );
            }

          },
        ),
      ),
    );
  }

  Color _getAvatarColor(String user) {
    return Colors.pink;
  }
}