#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
守卫：断言生产代码中不存在「跨模块的同包同名类」。

背景 —— z-util 曾在 z-util-core 与 z-util-pattern/* 两处各放一份
com.zifang.util.core.pattern.* 的实现，共 73 个类同包同名。
Maven 对此不报错（它们是不同 jar 里的不同 class），
但**运行时到底加载哪一个取决于 classpath 顺序**，
于是同一份代码在不同模块里可能表现出不同行为。

实测该重复里有 3 个类**实现并不相同**，不是无害冗余：

  Event               core 版做防御性拷贝并返回不可变视图；pattern 版是引用透传
  AnnotationContext   pattern 版多一个 register(Class,T)；core 版没有该方法
                      （运行时若加载 core 版，调用它会 NoSuchMethodError）
  LeafWrapper          core 版继承 Triplet；pattern 版自己持有字段

因此它不是「代码洁癖」，是**真实的行为不确定性**。本次已把 core 侧整体删除
（全仓现在 0 组重复），本守卫负责防止它再长回来。

只扫 src/main —— 测试代码允许同名（测试不参与发布物的 classpath），
但生产代码不允许。

用法：
    python3 _doc/003_script/check_duplicate_classes.py
退出码：0 = 通过；1 = 存在跨模块同名类。
"""

import os
import re
import sys

PKG_RE = re.compile(r'^\s*package\s+([\w.]+)\s*;', re.M)
SKIP_DIRS = {"target", ".git", "node_modules", ".idea"}


def iter_main_sources(root):
    for dirpath, dirnames, filenames in os.walk(root):
        dirnames[:] = [d for d in dirnames if d not in SKIP_DIRS]
        if os.sep + "src" + os.sep + "main" + os.sep + "java" + os.sep not in dirpath + os.sep:
            continue
        for fn in filenames:
            if fn.endswith(".java") and fn not in ("package-info.java", "module-info.java"):
                yield os.path.join(dirpath, fn)


def module_of(path, root):
    """取从 repo 根到 src/main 之间的路径作为模块名。"""
    rel = os.path.relpath(path, root)
    parts = rel.split(os.sep)
    if "src" in parts:
        return os.sep.join(parts[: parts.index("src")])
    return parts[0]


def main():
    root = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
    if not os.path.isdir(root):
        print("找不到仓库根目录: %s" % root)
        return 2

    occ = {}
    scanned = 0
    for path in iter_main_sources(root):
        try:
            with open(path, "r", encoding="utf-8", errors="replace") as f:
                text = f.read()
        except OSError:
            continue
        m = PKG_RE.search(text)
        if not m:
            continue
        scanned += 1
        fqcn = "%s.%s" % (m.group(1), os.path.splitext(os.path.basename(path))[0])
        occ.setdefault(fqcn, set()).add(module_of(path, root))

    dupes = {k: v for k, v in occ.items() if len(v) > 1}

    print("扫描生产源码 %d 个 .java 文件（src/main）" % scanned)

    if not dupes:
        print("PASS: 不存在跨模块的同包同名类")
        return 0

    print("")
    print("FAIL: 发现 %d 组「跨模块同包同名类」——" % len(dupes))
    print("      这类重复不报编译错，但运行时加载到哪一个取决于 classpath 顺序，")
    print("      同一份代码在不同模块下可能表现出不同行为。")
    print("")
    for fqcn, mods in sorted(dupes.items()):
        print("  %s" % fqcn)
        for m in sorted(mods):
            print("      - %s" % m)
        print("      处置: 保留唯一一份（通常在语义归属的模块里），")
        print("            其余模块改为依赖它；不要两份都留着。")
    return 1


if __name__ == "__main__":
    sys.exit(main())