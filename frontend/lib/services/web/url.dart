class UrlRouter{
  // static String baseUrl = "http://114.115.210.247:8083";
  static String baseUrl = "https://oralenglish.clankalliance.cn";
  static String baseUrlModel = "https://englishmodel.clankalliance.cn";
  static String sendCode = "$baseUrl/api/user/phonecode";
  static String login = "$baseUrl/api/user/phonelogin";
  static String myInfo = "$baseUrl/api/user/myinfo";
  static String tokenCheck = "$baseUrl/api/user/tokencheck";
  static String updateToken = "$baseUrl/api/user/myinfo";
  static String getWord = "$baseUrl/api/dictionary/getword";
  static String getAudio = "$baseUrl/static/audio";

  static String speech2Text = "$baseUrlModel/speechtotext";
  static String text2Video = "$baseUrlModel/texttovideo";

  static String getList = "$baseUrl/api/trainingdata/getlist";
  static String getDetail = "$baseUrl/api/trainingdata/getdetail";

  static String speechText = "http://114.115.210.247:8085/getText";

  static String websocket = "ws://101.200.128.229:5174/websocket/";
}