import json
from datasets import load_from_disk

# 加载数据集
dataset = load_from_disk("./daily_dialog")

# 获取所有对话数据
all_dialogs = dataset['train']['dialog'] + dataset['test']['dialog'] + dataset['validation']['dialog']

# 将数据转换为需要的JSONL格式
with open('formatted_data2.jsonl', 'w', encoding='utf-8') as f:
    for dialog in all_dialogs:
        conversations = []
        for i in range(len(dialog) - 1):
            conversations.append({
                # 有时候会出现字符’，需要替换
                "input": dialog[i].replace("’", "\'"),
                "output": dialog[i+1].replace("’", "\'") if i != len(dialog) - 1 else ""
            })
        formatted_data = {"conversation": conversations}
        json.dump(formatted_data, f, ensure_ascii=False)
        f.write("\n")

print("数据已成功转换并保存为formatted_data.jsonl文件")
