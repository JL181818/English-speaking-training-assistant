import json
import os
import requests


def read_json_file(file_path):
    with open(file_path, 'r', encoding='utf-8') as file:
        data = json.load(file)
    return data


def clean_string(s):
    return s.replace('\t', '').replace('\n', '').replace('\r', '')


def send_https_request(url, word, trans, example, example_trans):
    headers = {'Content-Type': 'application/json'}
    payload = {
        'word': word,
        'trans': trans,
        'example': example,
        'exampleTrans': example_trans
    }
    response = requests.post(url, headers=headers, json=payload)
    return response


def process_tran(tran_list):
    first = True
    result = ''
    for item in tran_list:
        key = next(iter(item))
        value = item[key].strip().replace('\t', '').replace('\n', '').replace('\r', '')
        key = key.replace('\t', '').replace('\n', '').replace('\r', '')
        if len(key) > 0 and len(value) > 0:
            if first:
                first = False
            else:
                result += '\n'

            result += key + ': ' + value

    return result


def process_json_files(file_paths, url):
    for file_path in file_paths:
        data = read_json_file(file_path)
        for obj in data:
            word = clean_string(obj.get('word', ''))
            tran_list = obj.get('tran', [])
            trans = process_tran(tran_list)
            example_list = obj.get('example', [{}])
            if len(example_list) > 0:
                example_obj = obj.get('example', [{}])[0]
                example = clean_string(example_obj.get('sen', '')).replace('<div>', '').replace('</div>', '').replace(
                    '<em>', '').replace('</em>', '').replace('&#13;', '').replace('#13;', '')
                example_trans = clean_string(example_obj.get('sen_tran', ''))
            else:
                example = '暂无'
                example_trans = '暂无'

            response = send_https_request(url, word, trans, example, example_trans)
            print(f"文件: {file_path}, word: {word}")
            print("响应状态码:", response.status_code)
            print("响应内容:", response.text)


def main():
    json_files = ['D:\\Design\\牛津词典 爬取\\words\\1.json',
                  'D:\\Design\\牛津词典 爬取\\words\\2.json',
                  'D:\\Design\\牛津词典 爬取\\words\\3.json',
                  'D:\\Design\\牛津词典 爬取\\words\\4.json',
                  'D:\\Design\\牛津词典 爬取\\words\\5.json',
                  'D:\\Design\\牛津词典 爬取\\words\\6.json',
                  'D:\\Design\\牛津词典 爬取\\words\\7.json',
                  'D:\\Design\\牛津词典 爬取\\words\\8.json',
                  'D:\\Design\\牛津词典 爬取\\words\\9.json',
                  'D:\\Design\\牛津词典 爬取\\words\\10.json',
                  'D:\\Design\\牛津词典 爬取\\words\\11.json']  # 替换为你的JSON文件路径列表
    url = 'https://oralenglish.clankalliance.cn/api/dictionary/saveword'  # 替换为你的HTTPS端点URL

    process_json_files(json_files, url)


if __name__ == '__main__':
    main()
