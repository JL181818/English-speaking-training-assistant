# English-speaking-training-assistant
## 特征

**对话练习**：与助理进行模拟对话，以提高你的口语能力

**发音评估**：接收发音反馈，给出精确评分，以确定需要改进的地方。

**词汇构建**：探索适合您水平的新词汇单词和短语。

**口型学习**：利用数字人模型帮助学会英语发音口型

**进度跟踪**：随着时间的推移监控你的进度，以保持动力并跟踪你的进步，数据库持久化存储

## Authors
排名不分先后

- [Panyu]() - 前端
- [Jielirong](https://github.com/JL181818) - 模型
- [Guan Yunpeng](https://github.com/SKgtsa) - 后端
- [stroll](https://github.com/Idonotkownwho) - 后端

## 模型环境配置和模型文件下载：
```bash
conda create -n speakspark python=3.10

conda activate speakspark

pip install torch==2.2.0+cu121 torchaudio==2.2.0+cu121 torchvision==0.17.0+cu121

conda install ffmpeg

cd avatar

pip install -r requirements.txt

bash scripts/download_models.sh
```

## 模型部署

```bash
python server.py
```
