package com.zifang.util.codegen.core.emit;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 生成源码的文本构造器：负责包名、import 排序与缩进。
 *
 * <p>只做"排版"，不含任何语法判断——调用方自己保证写出来的是合法 Java。</p>
 *
 * @author zifang
 */
public final class JavaSource {

    private final String packageName;
    private final String simpleTypeName;
    private final Set<String> imports = new LinkedHashSet<>();
    private final List<String> lines = new ArrayList<>();
    private int indent;

    private JavaSource(String packageName, String simpleTypeName) {
        this.packageName = packageName;
        this.simpleTypeName = simpleTypeName;
    }

    public static JavaSource of(String qualifiedName) {
        int idx = qualifiedName.lastIndexOf('.');
        return idx < 0
                ? new JavaSource("", qualifiedName)
                : new JavaSource(qualifiedName.substring(0, idx), qualifiedName.substring(idx + 1));
    }

    public JavaSource importType(String qualifiedName) {
        imports.add(qualifiedName);
        return this;
    }

    /** 追加一行（自动缩进）。 */
    public JavaSource line(String text) {
        if (!text.isEmpty()) {
            lines.add("    ".repeat(indent) + text);
        } else {
            lines.add("");
        }
        return this;
    }

    public JavaSource blank() {
        lines.add("");
        return this;
    }

    public JavaSource indent() {
        indent++;
        return this;
    }

    public JavaSource dedent() {
        if (indent > 0) {
            indent--;
        }
        return this;
    }

    public String packageName() {
        return packageName;
    }

    public String simpleTypeName() {
        return simpleTypeName;
    }

    /** 渲染成完整源文件文本。 */
    public String render() {
        StringBuilder sb = new StringBuilder();
        if (!packageName.isEmpty()) {
            sb.append("package ").append(packageName).append(";\n\n");
        }
        if (!imports.isEmpty()) {
            List<String> sorted = new ArrayList<>(imports);
            sorted.sort(String::compareTo);
            for (String i : sorted) {
                sb.append("import ").append(i).append(";\n");
            }
            sb.append('\n');
        }
        for (String l : lines) {
            sb.append(l).append('\n');
        }
        return sb.toString();
    }
}
