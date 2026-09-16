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

import java.util.HashMap;
import java.util.Map;

/**
 * _076类。
 */
public class _076 {
    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        new _076().minWindow("ADOBECODEBANC", "ABC");
    }

    // 判断是否是子序列

    /**
     * getCharCntMap方法。
     * * @param cs char[]类型参数
     *
     * @return HashMap<Character, Integer>类型返回值
     */
    public HashMap<Character, Integer> getCharCntMap(char[] cs) {
        HashMap<Character, Integer> m = new HashMap<>();
        for (int i = 0; i < cs.length; i++) {
            if (m.containsKey(cs[i])) {
                m.put(cs[i], m.get(cs[i]) + 1);
            } else {
                m.put(cs[i], 1);
            }
        }
        return m;
    }

    /**
     * isSub方法。
     * * @param scm HashMapCharacter,Integer类型参数
     *
     * @param cs HashMapCharacter,Integer类型参数
     * @return boolean类型返回值
     */
    public boolean isSub(HashMap<Character, Integer> scm, HashMap<Character, Integer> cs) {
        for (Map.Entry<Character, Integer> entry : cs.entrySet()) {
            if (!scm.containsKey(entry.getKey())) {
                return false;
            } else {
                if (scm.get(entry.getKey()) < entry.getValue()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * minWindow方法。
     * * @param s String类型参数
     *
     * @param t String类型参数
     * @return String类型返回值
     */
    public String minWindow(String s, String t) {
        // 先使用最大长度进行判断，然后逐渐缩小其左右

        // 单独判定, 如果总长度小于目标，则返回空值
        if (s.length() < t.length()) {
            return "";
        }

        // 获得数组
        char[] sc = s.toCharArray();
        char[] tc = t.toCharArray();

        // 转化为char-次数的映射
        HashMap<Character, Integer> scm = getCharCntMap(sc);
        HashMap<Character, Integer> tcm = getCharCntMap(tc);

        // 先整体判断是否是子串
        if (!isSub(scm, tcm)) {
            return "";
        }

        // s的滑动窗口指针
        int start = 0;
        int end = s.length() - 1;

        // 开始循环判断
        while (true) {

            // 左边开始缩

            // 如果左边字符不在目标序列中
            if (!tcm.containsKey(sc[start])) {
                start = start + 1;
            } else {
                // 针对char, scm内的数字如果大于 tcm内的数量,可以被删掉
                if (scm.get(sc[start]) > tcm.get(sc[start])) {
                    scm.put(sc[start], scm.get(sc[start]) - 1);
                    start = start + 1;
                } else {
                    break;
                }
            }
        }

        // 开始循环判断
        while (true) {

            // 右边开始缩

            // 如果左边字符不在目标序列中
            if (!tcm.containsKey(sc[end])) {
                end = end - 1;
            } else {
                // 针对char, scm内的数字如果大于 tcm内的数量,可以被删掉
                if (scm.get(sc[end]) > tcm.get(sc[end])) {
                    scm.put(sc[end], scm.get(sc[end]) - 1);
                    end = end - 1;
                } else {
                    break;
                }
            }
        }

        return s.substring(start, end + 1);
    }
}
