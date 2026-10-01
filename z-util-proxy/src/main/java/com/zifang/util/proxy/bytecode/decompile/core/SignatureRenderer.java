package com.zifang.util.proxy.bytecode.decompile.core;

import java.util.ArrayList;
import java.util.List;

/**
 * 把 .class 的 Signature 字符串按 JVM 规范（4.7.9）渲染为 Java 源码形态的泛型类型名。
 * <p>
 * 当前覆盖：L 类型带或不带泛型参数、数组、嵌套泛型。
 */
public final class SignatureRenderer {

    private SignatureRenderer() {
    }

    /**
     * 字段类型的签名渲染。
     *
     * @param signature            例如 "Ljava/util/Map<Ljava/lang/String;Ljava/lang/Integer;>;"
     * @param descriptorSimpleName 例如从 descriptor 得到的简单名 "Map"
     * @return 例如 "Map<String, Integer>"
     */
    public static String renderFieldType(String signature, String descriptorSimpleName) {
        if (signature == null) {
            return descriptorSimpleName;
        }
        int lt = signature.indexOf('<');
        if (lt < 0) {
            return descriptorSimpleName;
        }
        int gt = matchingGt(signature, lt);
        String inner = signature.substring(lt + 1, gt);
        return descriptorSimpleName + "<" + splitAndRender(inner) + ">";
    }

    /**
     * 把方法返回类型签名渲染：{@code descriptor ("[Ljava/util/List<Ljava/lang/String;>;")}
     * → {@code List<String>[]}（数组 + 泛型）。
     */
    public static String renderReturnType(String descriptor, String signature) {
        int dims = 0;
        String d = descriptor;
        while (d.startsWith("[")) {
            dims++;
            d = d.substring(1);
        }
        if (signature == null) {
            return descriptor;
        }
        int sigDims = 0;
        String s = signature;
        while (s.startsWith("[")) {
            sigDims++;
            s = s.substring(1);
        }
        String base = renderSingleType(s);
        if (base == null) {
            return descriptor;
        }
        StringBuilder sb = new StringBuilder(base);
        for (int i = 0; i < Math.max(dims, sigDims); i++) {
            sb.append("[]");
        }
        return sb.toString();
    }

    private static String renderSingleType(String s) {
        if (s == null || s.isEmpty()) {
            return null;
        }
        char c = s.charAt(0);
        switch (c) {
            case 'B': return "byte";
            case 'C': return "char";
            case 'D': return "double";
            case 'F': return "float";
            case 'I': return "int";
            case 'J': return "long";
            case 'S': return "short";
            case 'Z': return "boolean";
            case 'V': return "void";
            case 'L':
                int semi = s.indexOf(';');
                if (semi < 0) {
                    return null;
                }
                String internal = s.substring(1, semi);
                String simple = internal.substring(internal.lastIndexOf('/') + 1);
                String rest = s.substring(semi + 1);
                if (rest.startsWith("<")) {
                    int g = matchingGt(rest, 0);
                    return simple + "<" + splitAndRender(rest.substring(1, g)) + ">";
                }
                return simple;
            default:
                return null;
        }
    }

    private static String splitAndRender(String inner) {
        List<String> args = new ArrayList<>();
        int depth = 0;
        int start = 0;
        for (int i = 0; i < inner.length(); i++) {
            char c = inner.charAt(i);
            if (c == '<') {
                depth++;
            } else if (c == '>') {
                depth--;
            } else if (c == ';' && depth == 0) {
                // 顶层 ; 把内部分成多个完整描述符（每个必以 ; 收尾），不能再用 ',' 切
                // —— '<>' 内没有逗号而只有 ; 收尾
                args.add(renderSingleType(inner.substring(start, i + 1)));
                start = i + 1;
            }
        }
        return String.join(", ", args);
    }

    private static int matchingGt(String s, int openIdx) {
        int depth = 0;
        for (int i = openIdx; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '<') {
                depth++;
            } else if (c == '>') {
                depth--;
                if (depth == 0) {
                    return i;
                }
            }
        }
        return s.length() - 1;
    }
}