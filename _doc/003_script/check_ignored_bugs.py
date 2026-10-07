#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
守卫：断言测试源码中不存在「用 @Ignore 掩盖已知缺陷」的写法。

背景 —— 本守卫不是凭空加的。z-util 曾在 5 处 @Ignore 的 reason 里直接写下
实现缺陷（作者原话），然后靠跳过让构建永远绿：

    Num.sum/multiply 实现 bug，结果与预期不符
    DockerCommandResult 实现 bug，getData() 与 getStdout() 返回不同值
    findPooledObject 是 stub，返回 null
    KeyedObjectPool.getNumIdle 逻辑问题，需详细调试
    与 testCopyFileNoOverwrite 行为矛盾

由于这 5 处都不写进 CI 配置，构建永远 BUILD SUCCESS，欠账也就永远看不见。
本守卫的作用是把它们挂到明面上：谁再写一个「已知有 bug」的 @Ignore，CI 当场红。

判定口径（刻意收窄，避免误伤）：
  - 只扫测试源码下的 @Ignore / @org.junit.Ignore 注解；
  - 只在 **reason 文本** 命中禁用词时判违规；
  - 裸 @Ignore（无 reason）**不在本守卫范围** —— 那是另一类欠账
    「不知道为什么跳过」，需要单独治理，不与本守卫混为一谈；
  - reason 写「环境不具备」这类正当理由（需要数据库、需要外部磁盘等）
    不会命中，因为不含禁用词。

自指处置：
  本脚本的关键词以 \\uXXXX 转义书写，源码里**不出现连续字面量**，
  因此即使有人把扫描范围扩大到全仓，它也不会把自己判为违规。
  同时下面还显式跳过了本文件，双保险。

用法：
    python3 _doc/003_script/check_ignored_bugs.py
退出码：0 = 通过；1 = 发现用 @Ignore 掩盖的已知缺陷。
"""

import os
import re
import sys

# ---------------------------------------------------------------------------
# 禁用词：表达「已知缺陷尚未修复」的语义。
# 一律用 unicode 转义书写，保证本文件源码里不含这些连续字面量。
# ---------------------------------------------------------------------------
FORBIDDEN = [
    "\u5b9e\u73b0 bug",                     # 实现 bug
    "\u662f stub",                          # 是 stub
    "\u903b\u8f91\u95ee\u9898",             # 逻辑问题
    "\u884c\u4e3a\u77db\u76fe",             # 行为矛盾
    "\u9700\u8be6\u7ec6\u8c03\u8bd5",       # 需详细调试
    "\u5f85\u4fee\u590d",                   # 待修复
    "\u672a\u4fee\u590d",                   # 未修复
    "known bug",
]

# @Ignore / @org.junit.Ignore，后面可跟一个带引号的 reason
IGNORE_RE = re.compile(
    r'@(?:org\.junit\.)?Ignore\s*\(\s*"([^"]*)"\s*\)'
)

SELF = os.path.abspath(__file__)
SKIP_DIR_PARTS = (os.sep + "target" + os.sep,)


def iter_java_files(root):
    for dirpath, dirnames, filenames in os.walk(root):
        # 跳过编译产物与构建目录
        dirnames[:] = [
            d for d in dirnames
            if d not in ("target", ".git", "node_modules")
        ]
        if any(p in dirpath + os.sep for p in SKIP_DIR_PARTS):
            continue
        for fn in filenames:
            if fn.endswith(".java"):
                yield os.path.join(dirpath, fn)


def main():
    root = os.path.abspath(os.path.join(os.path.dirname(SELF), "..", ".."))
    if not os.path.isdir(root):
        print("找不到仓库根目录: %s" % root)
        return 2

    violations = []
    scanned = 0

    for path in iter_java_files(root):
        if os.path.abspath(path) == SELF:
            continue
        # 只看测试源码
        norm = path.replace(os.sep, "/")
        if "/src/test/" not in norm:
            continue
        scanned += 1
        try:
            with open(path, "r", encoding="utf-8", errors="replace") as f:
                text = f.read()
        except OSError:
            continue

        for lineno, line in enumerate(text.split("\n"), start=1):
            m = IGNORE_RE.search(line)
            if not m:
                continue
            reason = m.group(1)
            low = reason.lower()
            hit = [w for w in FORBIDDEN if w.lower() in low]
            if hit:
                violations.append((path, lineno, reason, hit))

    rel_root = root + os.sep
    print("扫描测试源码 %d 个 .java 文件" % scanned)

    if not violations:
        print("PASS: 未发现用 @Ignore 掩盖的已知缺陷")
        return 0

    print("")
    print("FAIL: 发现 %d 处 @Ignore 把已知缺陷藏了起来 ——" % len(violations))
    print("      这些用例永远跳过，构建永远绿，欠账也就永远看不见。")
    print("")
    for path, lineno, reason, hit in violations:
        shown = path[len(rel_root):] if path.startswith(rel_root) else path
        print("  %s:%d" % (shown, lineno))
        print("      reason: %s" % reason)
        print("      命中  : %s" % ", ".join(hit))
        print("      处置  : 修好并去掉 @Ignore；若确属正当跳过，")
        print("              请把 reason 改写成不含上述禁用词的说明。")
    return 1


if __name__ == "__main__":
    sys.exit(main())