import json
from datasets import load_from_disk

# 加载数据集
dataset = load_from_disk("./daily_dialog")

# 获取所有对话数据
all_dialogs = dataset['train']['dialog'] + dataset['test']['dialog'] + dataset['validation']['dialog']

# 将数据转换为需要的JSON格式
formatted_data = []
for dialog in all_dialogs:
    if len(dialog) % 2 != 0:
        # 如果对话数目为奇数，删除最后一个对话
        dialog = dialog[:-1]
    for i in range(0, len(dialog), 2):
        formatted_data.append({
            "conversation": [
            {   
                # 有时候会出现字符’，需要替换
                "input": dialog[i].replace("’", "\'"),
                "output": dialog[i+1].replace("’", "\'")
            }
            ]
        })

# 将格式化后的数据保存为JSON文件
with open('formatted_data.json', 'w', encoding='utf-8') as f:
    json.dump(formatted_data, f, ensure_ascii=False, indent=4)

