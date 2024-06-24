import 'package:json_annotation/json_annotation.dart';

class Speech2TextEntity{
  late double score;
  late String correction;
  List wrongWords = [];
  Speech2TextEntity(Map<String, dynamic> response){
    score = response['PronunciationAssessmentResult'] ?? 0;
    correction = compose(response['Words']);
    getWrongWords(response['Words']);
  }
  String compose(words){
    String res = "";
    if(words==null || words[0]==null){
      res = "";
    }else{
      for(var word in words){
        var s = word['Word'] ?? "";
        res = '$res $s';
      }
    }
    return res;
  }
  void getWrongWords(List words){
    for(var word in words){
      if(word['Errortype']!='None'){
        wrongWords.add({
          'word': word['Word'],
          'score': word['AccuracyScore']
        });
      }
    }
  }
}

@JsonSerializable()
class SpeechResult {
  @JsonKey(name: 'RecognitionStatus')
  String? recognitionStatus;
  @JsonKey(name: 'Offset')
  int? offset;
  @JsonKey(name: 'Duration')
  int? duration;
  @JsonKey(name: 'DisplayText')
  String? displayText;


  SpeechResult({
    this.recognitionStatus,
    this.offset,
    this.duration,
    this.displayText,
  });

  factory SpeechResult.fromJson(Map<String, dynamic> json) {
    return SpeechResult(
      recognitionStatus: json['RecognitionStatus'] as String,
      offset: json['Offset'] as int,
      duration: json['Duration'] as int,
      displayText: json['DisplayText'] as String,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'RecognitionStatus': recognitionStatus,
      'Offset': offset,
      'Duration': duration,
      'DisplayText': displayText,
      // 'data': data,
    };
  }
}