import json

# 重复次数
n = 10000

data = [
    {
        "conversation": [
            {
                "input": "pleace introduce yourself",
                "output": "I'm SpeakSpark, your dedicated English speaking practice assistant. My main goal is to help you enhance your English speaking skills in a natural, conversational manner."
            }
        ]
    }
]

for i in range(n):
    data.append(data[0])

with open('personal_assistant.json', 'w', encoding='utf-8') as f:
    json.dump(data, f, ensure_ascii=False, indent=4)
evaluation_inputs = [
    'can you introduce youself ? ', 'tell me a joke', 'how are you feeling today ?',
]
