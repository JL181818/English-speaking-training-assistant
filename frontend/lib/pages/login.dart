import 'package:flutter/material.dart';
import 'package:fluttertoast/fluttertoast.dart';
import 'package:get/get.dart';
import 'package:intl/date_symbol_data_local.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:untitled/pages/history_list.dart';
import '../services/web/request.dart';
import '../services/web/url.dart';
import 'chat.dart';
import 'main_page.dart';

Widget getAvatar(){
  return Center(
    child: Container(
      child: CircleAvatar(
          radius: 100,
          child: Image.asset("assets/images/arona.png"),
      ),
      // ),
    ),
  );
}
class LoginPage extends StatelessWidget{
  const LoginPage({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Column(
        children: [
          Expanded(flex: 1, child: Container(),),
          Expanded(flex: 6,
            child: Hero(
              tag: "login",
              child: getAvatar(),
            ),
          ),
          Expanded(
            flex: 1,
            child: Center(
              child: ElevatedButton(
                onPressed: ()=> Get.to(PhoneEnter()),
                child: const Text("登录"),
              ),
            ),
          ),
          Expanded(flex: 2, child: Container(),)
        ],
      ),
    );
  }
  
}
Widget getNum(controller, hint, max){
  return Container(
    margin: const EdgeInsets.all(50),
    child: TextField(
      controller: controller,
      style: const TextStyle(
        fontSize: 30
      ),
      decoration:  InputDecoration(
        hintText: hint,
        counter: const Text('')
      ),
      keyboardType: TextInputType.number,
      cursorColor: Colors.black,
      textAlign: TextAlign.center,
      maxLength: max,
      maxLines: 1,
    ),
  );
}
class PhonenumController extends GetxController{
  var phone = "".obs;
  setPhone(newPhone) => phone.value = newPhone;
}
class PhoneEnter extends StatelessWidget{
  final textController = TextEditingController();
  final pc = Get.put(PhonenumController());
  sendCode(String phone) async {
    Map<String, dynamic> request = {};
    request['phone'] = phone;
    await ApiClient().postRequest(
      url: UrlRouter.sendCode,
      request: request,
      reqeustSuccess: (result) async {
        pc.setPhone(phone);
        Fluttertoast.showToast(
            msg: "已成功向$phone发送验证码",
            toastLength: Toast.LENGTH_SHORT,
            backgroundColor: Colors.green,
        );
        Get.to(Verification());
      },
      requestFail: (e) {
        // pc.setPhone(phone);
        Fluttertoast.showToast(
          msg: e.errorMsg,
          toastLength: Toast.LENGTH_SHORT,
          backgroundColor: Colors.red,
        );
        // Get.to(Verification());
      },
    );
  }
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      resizeToAvoidBottomInset: false,
      body: Column(
        children: [
          Expanded(
              flex: 2,
              child: Hero(
                tag: "login",
                child: getAvatar(),
          )),
          Expanded(
            flex: 2,
            child: getNum(textController, '电话号码', 11),
          ),
          Expanded(
            flex: 1,
            child: Center(
              child: IconButton(
                color: Colors.blue,
                icon: const Icon(Icons.arrow_forward, size: 40,),
                onPressed: (){
                  sendCode(textController.text);
                },
              ),
            )
          )
        ],
      ),
    );
  }
}

class Verification extends StatelessWidget{
  final textController = TextEditingController();
  final PhonenumController pc = Get.find();
  login(code) async {
    Map<String, dynamic> request = {};
    request['phone'] = pc.phone.value;
    // request['phone'] = '13829271769';
    request['code'] = code;
    await ApiClient().postRequest(
      url: UrlRouter.login,
      request: request,
      reqeustSuccess: (result) async {
        if(result.success){
          if(result.loginValid != null){
            Fluttertoast.showToast(
              msg: "登录成功",
              backgroundColor: Colors.green,
            );
            Future<SharedPreferences> prefs0 = SharedPreferences.getInstance();
            SharedPreferences prefs = await prefs0;
            // prefs.setString("phone", "13829271769");
            prefs.setString("phone", pc.phone.value);
            prefs.setString("code", code);
            /// 跳转
            Get.offAll(ListViewPage());
            // Get.offAll(MainPage());
            // initializeDateFormatting().then((_)=>Get.off(const MyChatPage()));
          }else{
            Fluttertoast.showToast(
              msg: "身份验证失败",
              backgroundColor: Colors.red,
            );
          }
        }else{
          Fluttertoast.showToast(
            msg: "登录失败",
            backgroundColor: Colors.red,
          );
        }
      },
      requestFail: (e) {
        Fluttertoast.showToast(
          msg: e.errorMsg,
          backgroundColor: Colors.red,
        );
      },
    );
  }
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      resizeToAvoidBottomInset: false,
      body: Column(
        children: [
          Expanded(
            flex: 2,
              child: Hero(
                tag: "login",
                child: getAvatar(),
              )
          ),
          Expanded(
            flex: 2,
            child: getNum(textController, '验证码', 5),
          ),
          Expanded(
              flex: 1,
              child: Center(
                child: IconButton(
                  color: Colors.blue,
                  icon: const Icon(Icons.check, size: 40,),
                  onPressed: (){
                    login(textController.text);
                    // sendCode(textController.text);
                  },
                ),
              )
          )
          // Expanded(
          //     child: Center(
          //       child: Obx(()=> Text('${pc.phone}')),
          //     )
          // )
        ],
      ),
    );
  }

}
