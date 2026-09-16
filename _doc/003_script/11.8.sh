# !/bin/bash

### 退出脚本

echo 接下来打印退出状态码
echo $?

echo "\n"

### 退出脚本,指定状态码
echo 当退出的命令大于255的情况下，进行取模数
exit 5
