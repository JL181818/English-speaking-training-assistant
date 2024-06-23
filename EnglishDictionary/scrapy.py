#!/usr/bin/env python3
import requests
import json
import os
import uuid
from lxml import etree
from multiprocessing import Process
import random


user_agent = [
    "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_8; en-us) AppleWebKit/534.50 (KHTML, like Gecko) Version/5.1 Safari/534.50",
    "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-us) AppleWebKit/534.50 (KHTML, like Gecko) Version/5.1 Safari/534.50",
    "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:38.0) Gecko/20100101 Firefox/38.0",
    "Mozilla/5.0 (Windows NT 10.0; WOW64; Trident/7.0; .NET4.0C; .NET4.0E; .NET CLR 2.0.50727; .NET CLR 3.0.30729; .NET CLR 3.5.30729; InfoPath.3; rv:11.0) like Gecko",
    "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Trident/5.0)",
    "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.0; Trident/4.0)",
    "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.0)",
    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1)",
    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.6; rv:2.0.1) Gecko/20100101 Firefox/4.0.1",
    "Mozilla/5.0 (Windows NT 6.1; rv:2.0.1) Gecko/20100101 Firefox/4.0.1",
    "Opera/9.80 (Macintosh; Intel Mac OS X 10.6.8; U; en) Presto/2.8.131 Version/11.11",
    "Opera/9.80 (Windows NT 6.1; U; en) Presto/2.8.131 Version/11.11",
    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_0) AppleWebKit/535.11 (KHTML, like Gecko) Chrome/17.0.963.56 Safari/535.11",
    "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; Maxthon 2.0)",
    "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; TencentTraveler 4.0)",
    "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1)",
    "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; The World)",
    "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; Trident/4.0; SE 2.X MetaSr 1.0; SE 2.X MetaSr 1.0; .NET CLR 2.0.50727; SE 2.X MetaSr 1.0)",
    "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; 360SE)",
    "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; Avant Browser)",
    "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1)",
    "Mozilla/5.0 (iPhone; U; CPU iPhone OS 4_3_3 like Mac OS X; en-us) AppleWebKit/533.17.9 (KHTML, like Gecko) Version/5.0.2 Mobile/8J2 Safari/6533.18.5",
    "Mozilla/5.0 (iPod; U; CPU iPhone OS 4_3_3 like Mac OS X; en-us) AppleWebKit/533.17.9 (KHTML, like Gecko) Version/5.0.2 Mobile/8J2 Safari/6533.18.5",
    "Mozilla/5.0 (iPad; U; CPU OS 4_3_3 like Mac OS X; en-us) AppleWebKit/533.17.9 (KHTML, like Gecko) Version/5.0.2 Mobile/8J2 Safari/6533.18.5",
    "Mozilla/5.0 (Linux; U; Android 2.3.7; en-us; Nexus One Build/FRF91) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1",
    "MQQBrowser/26 Mozilla/5.0 (Linux; U; Android 2.3.7; zh-cn; MB200 Build/GRJ22; CyanogenMod-7) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1",
    "Opera/9.80 (Android 2.3.4; Linux; Opera Mobi/build-1107180945; U; en-GB) Presto/2.8.149 Version/11.10",
    "Mozilla/5.0 (Linux; U; Android 3.0; en-us; Xoom Build/HRI39) AppleWebKit/534.13 (KHTML, like Gecko) Version/4.0 Safari/534.13",
    "Mozilla/5.0 (BlackBerry; U; BlackBerry 9800; en) AppleWebKit/534.1+ (KHTML, like Gecko) Version/6.0.0.337 Mobile Safari/534.1+",
    "Mozilla/5.0 (hp-tablet; Linux; hpwOS/3.0.0; U; en-US) AppleWebKit/534.6 (KHTML, like Gecko) wOSBrowser/233.70 Safari/534.6 TouchPad/1.0",
    "Mozilla/5.0 (SymbianOS/9.4; Series60/5.0 NokiaN97-1/20.0.019; Profile/MIDP-2.1 Configuration/CLDC-1.1) AppleWebKit/525 (KHTML, like Gecko) BrowserNG/7.1.18124",
    "Mozilla/5.0 (compatible; MSIE 9.0; Windows Phone OS 7.5; Trident/5.0; IEMobile/9.0; HTC; Titan)",
    "UCWEB7.0.2.37/28/999",
    "NOKIA5700/ UCWEB7.0.2.37/28/999",
    "Openwave/ UCWEB7.0.2.37/28/999",
    "Mozilla/4.0 (compatible; MSIE 6.0; ) Opera/UCWEB7.0.2.37/28/999",
    # iPhone 6：
	"Mozilla/6.0 (iPhone; CPU iPhone OS 8_0 like Mac OS X) AppleWebKit/536.26 (KHTML, like Gecko) Version/8.0 Mobile/10A5376e Safari/8536.25",

]


def get_user_agent():
    return random.choice(user_agent)

class ScrapyProcess(Process):

    def __init__(self, file_name):
        super(ScrapyProcess, self).__init__()
        self.file_name = file_name

    def read_file(self):
        with open(self.file_name + '.txt', 'r', encoding='utf-8') as f:
            for line in f:
                yield line[:-1]

    def download_file(self, url, path):
        res = requests.get(url)
        with open(path, 'wb') as f:
            f.write(res.content)

    def connect_file(self, file_name1, file_name2, file_name3):
        file1 = open(file_name1, 'rb')
        file2 = open(file_name2, 'rb')
        file3 = open(file_name3, 'wb')
        file3.write(file1.read())
        file3.write(file2.read())
        file1.close()
        file2.close()
        file3.flush()
        file3.close()
        os.remove(file_name1)
        os.remove(file_name2)

    def is_in(self, key, dict_list):
        for item in dict_list:
            if key in item.keys():
                return True
        return False


    def scrapy(self, word):

        headers = {
        
        "User-Agent": get_user_agent(),
        
        }

        word_info = {}
        url = 'http://www.91dict.com/words?w=' + word
        res = requests.get(url,headers=headers)
        res.encoding = 'utf-8'
        data = etree.HTML(res.text)
        if data.xpath('/html/body/div[2]/section[2]/div/div/div/div[1]/div[1]/p/text()'):

            # 单词

            word_info['word'] = data.xpath('/html/body/div[2]/section[2]/div/div/div/div[1]/div[1]/p/text()')[0]

            try:
                pronounce_element = data.xpath('/html/body/div[2]/section[2]/div/div/div/div[1]/div[1]/div[1]/span/audio')[0]
                pron_url = pronounce_element.get('src')  # 获取音频的 URL
                if pron_url:
                    file_path = './audio/%s.mp3' % (word_info['word'])
                    self.download_file(pron_url,file_path)
                else:
                    print("没有对应的url", word_info['word'])
            except IndexError:
                print("没有对应的元素", word_info['word'])

            train = []
            for item in filter(lambda x: x != '', map(lambda x: x.replace('\n', ''),
                                                      data.xpath("//*[@class='listBox']/text()"))):
                if len(item.split('. ')) == 1:
                    train.append({'': item.split('. ')[0]})
                elif len(item.split('. ')) == 2 and not item.startswith('=') and not self.is_in(item.split('. ')[0], train):
                    train.append({item.split('. ')[0]: item.split('. ')[1]})
            word_info['tran'] = train

            # 例子
            example = []
            example_len = len(data.xpath(
                "//*[@class='flexslider flexslider_2']/ul/li/div[@class='imgMainbox']"))
            # 例句
            sens = data.xpath("//*[@class='mBottom']")
            # 例句范意思
            sen_trains = data.xpath("//*[@class='mFoot']/text()")
            origins = list(filter(lambda x: x != '\n', data.xpath(
                "//*[@class='mTop']/text()")))
            # 下文内容及翻译
            next_sens = data.xpath(
                "//*[@class='mTextend']/div[2]/div[2]/p[1]/text()")
            next_sen_trains = data.xpath(
                "//*[@class='mTextend']/div[2]/div[2]/p[2]/text()")
            next_pron_urls = data.xpath("//*[@class='viewdetail']/@href")
            for i in range(example_len):
                sen = etree.tostring(
                    sens[i], encoding='utf-8')[22:-7].decode('utf-8')
                sen_train = sen_trains[i][1:]
                example.append({
                    'origin': origins[i][1:-1],
                    "sen": sen,
                    'sen_tran': sen_train,
                })
            word_info['example'] = example
            return word_info

    def main(self):
        for word in self.read_file():
            print(word)
            self.save(self.scrapy(word))

    def save(self, word_info):
        with open(self.file_name + '.json', 'a', encoding='utf-8') as f:
            if word_info:
                json.dump(word_info, fp=f, indent=4, ensure_ascii=False)
                f.write(',\n')

    def run(self):
        self.main()


if __name__ == "__main__":
    for i in range(1, 12):
        p = ScrapyProcess('./words/' + str(i))
        # 启动子进程
        p.start()