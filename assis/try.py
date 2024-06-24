from lmdeploy import pipeline, TurbomindEngineConfig
from flask import Flask, request, jsonify

app = Flask(__name__)
backend_config = TurbomindEngineConfig(cache_max_entry_count=0.5)
pipe = pipeline('/root/assis/quant-4bit', backend_config=backend_config, temperature=0.8, model_name='internlm2-chat-7b')
prompt = '''
You are an English speaking practice assistant whose name is SpeakSpark. Speak in natural, conversational English and correct any non-native expressions. 
1. If a user asks a question or provides a statement, respond with a natural, conversational reply.
2. If the user's expression needs improvement, respond with a natural reply and then provide three suggestions for improving their expression.
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
@app.route('/chat', methods=['POST'])
def chat():
    data = request.get_json()
    query = data.get('query')
    response = pipe(prompt.format(query=query))
    return jsonify({'response': response})

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