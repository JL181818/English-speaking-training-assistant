import 'package:flutter/material.dart';
import 'package:fluttertoast/fluttertoast.dart';
import 'package:get/get.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:untitled/pages/history_list.dart';
import 'package:untitled/utils/theme.dart';
import './pages/main_page.dart';
import './services/web/url.dart';
import './pages/login.dart';
import './services/web/request.dart';
import 'dart:async';


void main() {
  runApp(
     GetMaterialApp(
      themeMode: ThemeMode.light,
      theme: GlobalThemData.lightThemeData,
      debugShowCheckedModeBanner: false,
      home: LoginPage(),
    )
  );
  Future.delayed(Duration.zero,() async {
    Future<SharedPreferences> prefs0 = SharedPreferences.getInstance();
    SharedPreferences prefs = await prefs0;
    if(prefs.get("token")==null){
      print('phone num is null');
    }else{
      // 根据历史记录登录
      Map<String, dynamic> request = {};
      // prefs.setString("token", '114514');
      request['token'] = prefs.get("token");
      await ApiClient().postRequest(
        url: UrlRouter.tokenCheck,
        request: request,
        reqeustSuccess: (result) {
          if(result.success){
            Fluttertoast.showToast(
              msg: "登录成功",
              backgroundColor: Colors.green,
            );
            /// 跳转到主页，否则自动留在登录页
            Get.offAll(ListViewPage());
          }else{
            Fluttertoast.showToast(
              msg: "请重新登录",
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
  });
}
