#!/bin/bash

echo 接下来进行if判断语句
echo 'if 中对命令返回值进行判断，如果返回值是0则为true'

if pwdf; then
  echo 'command if is true'
elif pwd; then
  echo 'command else if is true'
else
  echo 'command if is false'
fi

echo 测试test命令的用法

a='a'
#b=''

if test $b; then
  echo "command$b||||"
elif test $a; then
  echo 'test $a is oke'
else
  echo 'command if is false'
fi

echo \n提供第三种条件测试

if [ pwd ]; then
  echo 'command if is true'
fi
