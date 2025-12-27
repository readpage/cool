import os
import requests
from fake_useragent import UserAgent
from lxml import etree

def saveFile(file, mode, data):
  file_dir = os.path.split(file)[0]
  if not os.path.exists(file_dir):  # os模块判断并创建
    os.makedirs(file_dir)
  with open(file, mode) as f:
    for i in data:
      f.write(i + "\n")

def save(data):
  saveFile(file, "a", data)

# 去除文件内容重复
def deDuplication(file):
  strs = []
  with open(file, 'r') as f:
    for line in f.readlines():
      if line not in strs:
        strs.append(line)
  
  with open(file, 'w') as f:
    for str in strs:
      f.write(str)

def getData(): 
  strs = []
  response = requests.get(url=url, headers=headers)
  response.encoding = 'utf-8'
  tree = etree.HTML(response.text)
  list = tree.xpath('//*[@id="article"]/div[6]/div[1]/div/div[1]/div[2]/div/span')
  del list[0]
  for i in list:
    try:
      strs.append(i.text.split('、')[1])
    except:
      pass
  return strs

def main():
  list = getData()
  save(list)

if __name__ == "__main__":
  global file
  file = "./assets/username.txt"
  headers = { "User-Agent": UserAgent().random}
  url = "http://www.nejiaoyu.com/wangming/8510.html"
  main()
  # deDuplication(file)