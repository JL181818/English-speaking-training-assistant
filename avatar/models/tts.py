import azure.cognitiveservices.speech as speechsdk
def tts(voicename="en-US-AvaMultilingualNeural", path_to_save='../files/audio.wav', text='Hello, I am SpeakSpark! Nice to meet you!'):
    # 配置语音服务的密钥和区域
    speech_key, service_region = "", ""
    
    # 语音服务的具体配置
    speech_config = speechsdk.SpeechConfig(subscription=speech_key, region=service_region)
    # 设置语音服务的语言
    speech_config.speech_synthesis_voice_name = voicename
    audio_config = speechsdk.audio.AudioOutputConfig(filename=path_to_save)
    speech_synthesizer = speechsdk.SpeechSynthesizer(speech_config=speech_config, audio_config=audio_config)
    speech_synthesizer.speak_text_async(text).get() # 生成音频文件

def test_tts():
    speech_key, service_region = "", ""
    speech_config = speechsdk.SpeechConfig(subscription=speech_key, region=service_region)
    speech_config.speech_synthesis_language = "en-US"
    # Set the voice name, refer to https://aka.ms/speech/voices/neural for full list.
    speech_config.speech_synthesis_voice_name = "en-US-AvaMultilingualNeural"
    
    # 使用默认扬声器作为音频输出创建语音合成器
    speech_synthesizer = speechsdk.SpeechSynthesizer(speech_config=speech_config)
    
    # 接收来自控制台输入的文本
    print("Type some text that you want to speak...")
    text = input()
    
    # 将接收到的文本合成为语音
    # 在执行该行的情况下，期望在扬声器上听到合成语音。
    result = speech_synthesizer.speak_text_async(text).get()
    print(result)
    # Checks result.
    if result.reason == speechsdk.ResultReason.SynthesizingAudioCompleted:
        print("Speech synthesized to speaker for text [{}]".format(text))
    elif result.reason == speechsdk.ResultReason.Canceled:
        cancellation_details = result.cancellation_details
        print("Speech synthesis canceled: {}".format(cancellation_details.reason))
        if cancellation_details.reason == speechsdk.CancellationReason.Error:
            if cancellation_details.error_details:
                print("Error details: {}".format(cancellation_details.error_details))
        print("Did you update the subscription info?")


if __name__ == '__main__':
    tts()