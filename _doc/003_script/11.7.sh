# !/bin/bash
### 执行数学计算方法

echo "第一种计算方式 使用expr"
echo "执行expr 1 + 5命令有$(expr 1 + 5)"

echo "\n第二种计算方式 使用[]"
echo "执行[1*5]命令有:"
echo $((1 * 5))

echo "\n使用内建bc指令计算浮点值"
var1=$(echo "scale=4;3.44/5" | bc)
echo var1 is $var1
