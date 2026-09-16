# pivotHandler的所有的参数的解释

1. pivot
2. unpivot

## 1. 生成透视表

```json
{
  "serviceUnit": "engine.service.pivot",
  "invokeDynamic": "pivot",
  "invokeParameter":{
    "pivotColumnDefinations":[
      {
        "columnName":"name1",
        "pivotColumns":["name1_u1","name1_u2","name1_u3","name1_u4","name1_u5"],
        "value":"name1_value"
      },
      {
        "columnName":"name2",
        "pivotColumns":["name2_u1","name2_u2","name2_u3","name2_u4","name2_u5"]
      }
    ]
  }
}
```

columnName