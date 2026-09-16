package com.zifang.util.core.lang.primitive;

/**
 * 针对int相关的所有帮助方法合集
 *
 * @author zifang
 */
public class Ints {
    /**
     * 表达是否为字母
     */
    public static boolean isAlpha(int ch) {
        return ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z';
    }

    /**
     * 表达是否为数字
     */
    public static boolean isDigit(int ch) {
        return ch >= '0' && ch <= '9';
    }

    /**
     * 表达是否为空白字符 -> ' ' , '\t' , '\n' 都算
     */
    public static boolean isBlank(int ch) {
        return ch == ' ' || ch == '\t' || ch == '\n';
    }
}
