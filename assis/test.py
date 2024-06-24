import torch
from transformers import AutoTokenizer, AutoModelForCausalLM


model_name_or_path = "/root/assis/internlm2-chat-7b-4bit"

tokenizer = AutoTokenizer.from_pretrained(model_name_or_path, trust_remote_code=True)
model = AutoModelForCausalLM.from_pretrained(model_name_or_path, trust_remote_code=True, torch_dtype=torch.bfloat16, device_map='auto')
model = model.eval()

system_prompt = """You are an English speaking practice assistant whose name is SpeakSpark. Speak in natural, conversational English and correct any non-native expressions.
"""

messages = [(system_prompt, '')]

print("=============Welcome=============")

while True:
    input_text = input("User  >>> ")
    input_text = input_text.replace(' ', '')
    if input_text == "exit":
        break
    response, history = model.chat(tokenizer, input_text, history=messages)
    messages.append((input_text, response))
    print(f"robot >>> {response}")