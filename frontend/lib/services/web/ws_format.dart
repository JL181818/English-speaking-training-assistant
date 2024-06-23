class WsFormat{
  static int seq = 0;
  static String getAISeq(String response){
    var start = response.indexOf('#');
    var end = response.indexOf('#', start+1);
    return response.substring(start+1, end);
  }

  static String getAIContent(String response){
    print(response);
    var start = response.lastIndexOf('#');
    var raw = response.substring(start+1);
    var res = raw.replaceAll('\\n', '\n');
    print(res);
    res = res.replaceAll('\\"', '"');
    print(res);
    return res;
  }

  static String getAIAckSeq(String response){
    var start = response.lastIndexOf('#');
    return response.substring(start+1);
  }
  static bool checkResponse(String response){
    if(getAIAckSeq(response) == '$seq'){
      return true;
    }
    return false;
  }
  static String getRequestMsg(msg, correction, rank){
    seq++;
    return 'say#$seq#$msg#$correction#$rank';
  }
  // static String getTokenSeq(String response){
  //   seq++;
  //   return 'say#$seq#$msg#$correction#$rank';
  // }
}