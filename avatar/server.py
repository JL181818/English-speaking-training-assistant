from lmdeploy import pipeline, TurbomindEngineConfig
from flask import Flask, request, jsonify, send_from_directory, make_response, send_file
import random
from models import tts
from models import SadTalker
import azure.cognitiveservices.speech as speechsdk
import os
import time

app = Flask(__name__)
backend_config = TurbomindEngineConfig(cache_max_entry_count=0.5)
pipe = pipeline('/root/assis/quant-4bit', backend_config=backend_config, temperature=0.8, model_name='internlm2-chat-7b')
prompt = '''
You are an English speaking practice assistant named SpeakSpark. Your task is to engage in natural, conversational English and correct any non-native expressions. Here are your guidelines:

1. When a user asks a question or makes a statement, respond with a natural and conversational reply.
2. If the user's expression needs improvement, give a natural reply first. Then, provide three specific suggestions to enhance their expression, explaining why each change makes the expression more natural.

### Example1:
User: How can I improve my English speaking skills?
Assistant: One great way to improve your English speaking skills is to practice with native speakers as much as possible. You can also try watching English movies or TV shows and repeating the lines to practice pronunciation.

### Example2:
User: I got so black after my vacation.
Assistant: It sounds like you had a lot of sun on your vacation! You must have spent a lot of time outdoors.
    Here are some suggestions to improve your expression:
    1. Use "tanned" instead of "black" to describe skin darkening from sun exposure. "Tanned" is the appropriate term in English.
    2. Replace "so" with "really" or "quite" to make the sentence sound more natural: "I got really tanned after my vacation."
    3. Specify "vacation" to make the sentence clear. You could also say "holiday" if you prefer British English: "I got really tanned after my holiday."

### User Input:
{query}
### Assistant:
'''
# testQuery = 'She is good in playing piano.'
# response = pipe(prompt.format(query=testQuery))
# print(response)
blink_every = True
size_of_image = 256
preprocess_type = 'crop'
facerender = 'facevid2vid'
enhancer = False
is_still_mode = False
pic_path = './inputs/{role}.png'
crop_pic_path = './inputs/first_frame_dir_{role}/{role}.png'
first_coeff_path = './inputs/first_frame_dir_{role}/{role}.mat'
crop_info = ((403, 403), (19, 30, 502, 513), [40.05956541381802, 40.17324339233366, 443.7892505041507, 443.9029284826663])

exp_weight = 1
batch_size = 40
pose_style = random.randint(0, 45)
use_ref_video = False
ref_video = None
ref_info = 'pose'
use_idle_mode = False
length_of_audio = 5
audio = './files/audio.wav'
talker = SadTalker()

@app.route('/chat', methods=['POST'])
def chat():
    data = request.get_json()
    query = data.get('query')
    print('query',query)
    response = pipe(prompt.format(query=query))
    print(response.text)
    return jsonify({'text': response.text})

@app.route('/full')
def full():
    data = request.get_json()
    query = data.get('query', 'Hello, I am SpeakSpark! Nice to meet you!')
    role = data.get('role', 'girl')
    voicename = 'en-US-AvaMultilingualNeural' if role == 'girl' else 'en-US-BrianMultilingualNeural'
    response = pipe(prompt.format(query=query))
    tts(voicename=voicename, path_to_save='./files/audio.wav', text=response.text)
    video = talker.test(
            pic_path=pic_path.format(role=role),
            crop_pic_path=crop_pic_path.format(role=role),
            first_coeff_path=first_coeff_path.format(role=role), 
            crop_info=crop_info, 
            driven_audio=audio,
            preprocess=preprocess_type, 
            still_mode=is_still_mode,
            use_enhancer=enhancer,
            batch_size=batch_size,
            size=size_of_image, 
            pose_style = pose_style, 
            facerender=facerender,
            exp_scale=exp_weight, 
            use_idle_mode = use_idle_mode,
            length_of_audio = length_of_audio,
            use_blink=blink_every, 
            fps=20
    )
    print(response.text)
    return send_from_directory(directory='./files', path=f'{role}_audio.mp4')

@app.route('/video')
def video():
    return send_from_directory(directory='./files', path=f'girl_audio.mp4')

@app.route('/texttovideo')
def texttovideo():
    data = request.args
    sentence = data.get('sentence', 'Hello, I am SpeakSpark! Nice to meet you!')
    role = data.get('role','girl')
    use_enhancer = True if data.get('enhancer') == "1" else False
    print('use_enhancer',use_enhancer)
    voicename = 'en-US-AvaMultilingualNeural' if role == 'girl' else 'en-US-BrianMultilingualNeural'
    tts(voicename=voicename, path_to_save='./files/audio.wav', text=sentence)
    video = talker.test(
            pic_path=pic_path.format(role=role),
            crop_pic_path=crop_pic_path.format(role=role),
            first_coeff_path=first_coeff_path.format(role=role), 
            crop_info=crop_info, 
            driven_audio=audio,
            preprocess=preprocess_type, 
            still_mode=is_still_mode,
            use_enhancer=use_enhancer,
            batch_size=batch_size,
            size=size_of_image, 
            pose_style = pose_style, 
            facerender=facerender,
            exp_scale=exp_weight, 
            use_idle_mode = use_idle_mode,
            length_of_audio = length_of_audio,
            use_blink=blink_every, 
            fps=20
    )
    print(sentence)
    return send_from_directory(directory='./files', path=(f'{role}_audio_enhanced.mp4') if use_enhancer else (f'{role}_audio.mp4'))

@app.route('/speechtotext', methods=['POST'])
def upload_file():
    startTime = time.time()
    if 'file' not in request.files:
        return 'No file part'
    file = request.files['file']
    if file.filename == '':
        return 'No selected file'
    if file:
        filename = "uploaded.wav"
        file.save(filename)
        result = pronunciation_assessment_with_content_assessment(filename)
        endTime = time.time()
        print(f"运行时间: {endTime - startTime}秒 ")
        # os.remove(filename)  # remove the file after processing
        return result
'''
{
    "AccuracyScore": 81.0,
    "FluencyScore": 84.0,
    "PronunciationAssessmentResult": 74.0,
    "ProsodyScore": 68.4,
    "Words": [
        {
            "AccuracyScore": 35.0,
            "Errortype": "Mispronunciation",
            "Word": "what's"
        },
        {
            "AccuracyScore": 92.0,
            "Errortype": "None",
            "Word": "wrong"
        },
        {
            "AccuracyScore": 84.0,
            "Errortype": "None",
            "Word": "with"
        },
        {
            "AccuracyScore": 100.0,
            "Errortype": "None",
            "Word": "you"
        },
        {
            "AccuracyScore": 96.0,
            "Errortype": "None",
            "Word": "man"
        }
    ]
}
'''
def pronunciation_assessment_with_content_assessment(filename):
    speech_config = speechsdk.SpeechConfig(subscription="c85d9bf805634270a809c0618689c677", region="eastus")
    audio_config = speechsdk.audio.AudioConfig(filename=filename)

    pronunciation_config = speechsdk.PronunciationAssessmentConfig(
        grading_system=speechsdk.PronunciationAssessmentGradingSystem.HundredMark,
        granularity=speechsdk.PronunciationAssessmentGranularity.Phoneme)
    pronunciation_config.enable_prosody_assessment()

    language = 'en-US'
    speech_recognizer = speechsdk.SpeechRecognizer(
        speech_config=speech_config, language=language, audio_config=audio_config)
    pronunciation_config.apply_to(speech_recognizer)
    speech_recognition_result = speech_recognizer.recognize_once()
    pronunciation_assessment_result = speechsdk.PronunciationAssessmentResult(speech_recognition_result)

    result = {
        "AccuracyScore": pronunciation_assessment_result.accuracy_score,
        "FluencyScore": pronunciation_assessment_result.fluency_score,
        "ProsodyScore": pronunciation_assessment_result.prosody_score,
        "PronunciationAssessmentResult": pronunciation_assessment_result.pronunciation_score,
        "Words": [
            {
                "Word": word.word,
                "AccuracyScore": word.accuracy_score,
                "Errortype": word.error_type
            } for word in pronunciation_assessment_result.words
        ]
    }

    return result


if __name__ == '__main__':
    app.run()


# User: I very like go to the beach.
# Assistant: I really enjoy going to the beach too! It's such a great place to relax and enjoy the sun.
#     improved: I really enjoy going to the beach. It's so relaxing! 
#     Here are some suggestions to improve your expression:
#     1. Use "really enjoy" instead of "very like."
#     2. Say "going to the beach" instead of "go to the beach."
#     3. Adding "It's so relaxing!" makes the sentence more conversational.
# the improved version of their statement and 

'''
Today was a beautiful day. We had a great time taking a long walk outside in the morning.
The countryside was in full bloom, yet the air was crisp and cold. 
'''