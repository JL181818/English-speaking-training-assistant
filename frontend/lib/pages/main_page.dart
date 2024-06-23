import 'package:flutter/material.dart';
import 'package:fluttertoast/fluttertoast.dart';
import 'package:get/get.dart';
import 'package:intl/date_symbol_data_local.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../pages/chat.dart';
import '../pages/login.dart';
import '../services/web/url.dart';
import '../services/web/request.dart';

class ItemData{
  ItemData({required this.title, required this.time, required this.id});
  String title;
  String time;
  String id;
}
class ListController extends GetxController{
  var listData = [].obs;
  int getLength(){
    return listData.length;
  }
  addData(List<ItemData> data){
    listData.addAll(data);
  }
}
int pageNum = 0;
int pageSize = 10;
class MainPage extends StatelessWidget{
  final mc = Get.put(ListController());
  final ScrollController _scrollController = ScrollController();
  getListData() async {
    pageNum++;
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
          List<ItemData> data = [];
          for(var item in list){
            String id = item['id'];
            String timeUtc = item['time'];
            String time = DateTime.parse(timeUtc).toLocal().toString();
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
            data.add(ItemData(title: title, time: time, id: id));
          }
          mc.addData(data);
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
  Widget getItem(int index){
    ItemData? data = mc.listData[index];
    return ListTile(
      leading: CircleAvatar(
        backgroundColor: const Color(0xff764abc),
        foregroundColor: Colors.white70,
        child: Text('$index'),
      ),
      title: Text('${data?.title}'),
      subtitle: Text('${data?.time}'),
      trailing: const Icon(Icons.keyboard_arrow_right_outlined),
      selectedColor: Colors.red,
      onTap: () {
        print('$index');
      },
    );
  }
  @override
  Widget build(BuildContext context) {
    _scrollController.addListener(() {
      if (_scrollController.position.pixels ==
          _scrollController.position.maxScrollExtent) {
        print('滑动到了最底部');
        // getListData();
      }
    });
    getListData();
    return Scaffold(
      appBar: AppBar(
        title: const Text('我的训练'),
        backgroundColor: Colors.blue,
        centerTitle: true,
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
      body: Obx(()=>ListView(
        controller: _scrollController,
        children: ListTile.divideTiles(
            context: context,
            tiles: List.generate(mc.getLength(), (index){
              return getItem(index);
            })
        ).toList(),
      )),
      floatingActionButton: FloatingActionButton(
        onPressed: () async {
          Future<SharedPreferences> prefs0 = SharedPreferences.getInstance();
          SharedPreferences prefs = await prefs0;
          var token = prefs.getString('token');
          if(token!=null){
            initializeDateFormatting().then(
                    (_)=>Get.to(MyChatPage(token: token,))
            );
          }
        },
        child: const Icon(Icons.add),
      ),
    );
  }

}