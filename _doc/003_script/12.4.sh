#!/bin/bash

echo 接下来进行数值比较

value1=10
value2=11
#
if [ $value1 -gt 5 ]; then
  echo "The test value $value1 is greater than 5"
fi
#
if [ $value1 -eq $value2 ]; then
  echo "The values are equal"
else
  echo "The values are different"
fi

a=1
b=1
c=2
d=3

if [ $a -eq $b ]; then
  echo "a=b监测成功"
fi
