#!/bin/bash

echo 接下来演示循环打印列表

list="Alabama Alaska Arizona Arkansas California Colorado"
list=$list" 新加的"

for test in $list; do
  echo The next state is $test
done
