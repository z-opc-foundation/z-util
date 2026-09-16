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

import java.util.ArrayList;
import java.util.List;

/**
 * _068类。
 */
public class _068 {
    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        new _068().fullJustify(new String[]{"What ", "must ", "be   ", "shall ", "be.  "}, 5);
    }

    /**
     * fullJustify方法。
     * * @param words String[]类型参数
     *
     * @param maxWidth int类型参数
     * @return List<String>类型返回值
     */
    public List<String> fullJustify(String[] words, int maxWidth) {
        List<String> ans = new ArrayList<>();

        String temp = "";
        for (int i = 0; i < words.length; i++) {
            if (temp.length() == 0) {
                if (words[i].length() == maxWidth) {
                    ans.add(words[i]);
                } else {
                    temp = temp + words[i] + " ";
                    if (i == words.length - 1) {
                        ans.add(temp);
                    }
                }
            } else {
                if (temp.length() + words[i].length() > maxWidth) {
                    ans.add(temp);
                    temp = words[i] + " ";
                    if (i == words.length - 1) {
                        ans.add(temp);
                    }
                } else {
                    temp = temp + words[i] + " ";
                    if (i == words.length - 1) {
                        ans.add(temp);
                    }
                }
            }
        }

        for (int j = 0; j < ans.size(); j++) {

            String str = ans.get(j);
            String trimed = str.trim();

            if (j == ans.size() - 1) {
                ans.set(j, trimed + repeat(" ", maxWidth - trimed.length()));
                continue;
            }

            String[] strs = trimed.trim().split("[ ]");
            int blankCnt = maxWidth + (strs.length - 1) - trimed.length();

            if (strs.length - 1 == 0) {
                ans.set(j, trimed + repeat(" ", maxWidth - trimed.length()));
                continue;
            }

            // 平均分开字符串间应该有多少
            int pj = blankCnt / (strs.length - 1);
            // 还剩下的空格
            int mode = blankCnt - pj * (strs.length - 1);

            str = "";
            for (int k = 0; k < strs.length; k++) {
                str = str + strs[k] + repeat(" ", pj);
                if (mode > 0) {
                    str = str + " ";
                    mode = mode - 1;
                }
            }
            ans.set(j, str.trim());
        }


        return ans;
    }

    /**
     * repeat方法。
     * * @param str String类型参数
     *
     * @param times int类型参数
     * @return String类型返回值
     */
    public String repeat(String str, int times) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < times; i++) {
            sb.append(str);
        }
        return sb.toString();
    }
}
