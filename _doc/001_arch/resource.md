# resourceHandler的所有的参数的解释

1. 读取本地文件
2. 写出到本地文件
3. 读取mysql数据
4. 写出数据到mysql
5. 读取集群数据
6. 写出到集群
7. 本地文件上传到HDFS
8. 本地文件直接映射到表结构

## 1. 读取本地文件

```json
{
      "serviceUnit": "engine.service.resourceHandler",
      "invokeDynamic": "handleLocalInput",
      "invokeParameter":{
        "localFile":"/Users/zifang/workplace/idea_workplace/components/util-workflow/src/test/resources/data/input1.csv"
      },
      "cache":{
        "flushFlag": true,
        "cacheTempNameAlias":"input_local"
      }
}
```

localFile 指向本地路径

flushFlag 标明是否缓存这个node的中间结果

cacheTempNameAlias 表明为这个缓存保留的名字是什么


