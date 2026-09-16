package com.zifang.util.zex.leetcode;

/**
 * LeetCode算法练习类。
 * <p>
 * 此类包含LeetCode题目的解决方案。
 * 练习算法和数据结构知识。
 *
 * @author zifang
 * @version 1.0
 */
public class _125 {
    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        new _125().isPalindrome(" ");
    }

    /**
     * isNumberOrChar方法。
     * * @param c char类型参数
     *
     * @return boolean类型返回值
     */
    public boolean isNumberOrChar(char c) {
        if (c >= '0' && c <= '9') {
            return true;
        }

        if (c >= 'a' && c <= 'z') {
            return true;
        }

        if (c >= 'A' && c <= 'Z') {
            return true;
        }

        return false;
    }

    /**
     * equa方法。
     * * @param c1 char类型参数
     *
     * @param c2 char类型参数
     * @return boolean类型返回值
     */
    public boolean equa(char c1, char c2) {
        return String.valueOf(c1).compareToIgnoreCase(String.valueOf(c2)) == 0;
    }

    /**
     * isPalindrome方法。
     * * @param s String类型参数
     *
     * @return boolean类型返回值
     */
    public boolean isPalindrome(String s) {

        if (s.equals("")) {
            return true;
        }

        int i = 0;
        int j = s.length() - 1;

        while (true) {
            while (true) {
                if (!isNumberOrChar(s.charAt(i))) {
                    if (i != s.length() - 1) {
                        i++;
                    }
                } else {
                    break;
                }
            }

            while (true) {
                if (!isNumberOrChar(s.charAt(j))) {
                    if (j > 0) {
                        j--;
                    }
                } else {
                    break;
                }
            }

            if (!equa(s.charAt(i), s.charAt(j))) {
                return false;
            }

            if (i == j) {
                return true;
            }
            if (i > j) {
                return true;
            }
            i++;
            j--;
        }
    }
}
