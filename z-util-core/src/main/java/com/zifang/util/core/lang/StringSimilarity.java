package com.zifang.util.core.lang;

/**
 * 字符串相似度计算工具类，提供两种算法：
 * <ul>
 *   <li>最长公共子串（LCS）相似度</li>
 *   <li>Levenshtein 编辑距离相似度</li>
 * </ul>
 */
public class StringSimilarity {

    /**
     * 计算两个字符串的相似度（基于最长公共子串）
     *
     * @param strA 字符串A
     * @param strB 字符串B
     * @return 相似度比例 [0, 1]，1 表示完全相同
     */
    public static double lcsSimilarity(String strA, String strB) {
        if (strA == null || strB == null) {
            return 0.0;
        }
        if (strA.isEmpty() && strB.isEmpty()) {
            return 1.0;
        }
        // 较长的放前面以提高效率
        if (strA.length() < strB.length()) {
            String tmp = strA;
            strA = strB;
            strB = tmp;
        }
        String lcs = longestCommonSubstring(strA, strB);
        return (double) lcs.length() / strA.length();
    }

    /**
     * 计算两个字符串的相似度（基于 Levenshtein 编辑距离）
     *
     * @param strA 字符串A
     * @param strB 字符串B
     * @return 相似度比例 [0, 1]，1 表示完全相同
     */
    public static double levenshteinSimilarity(String strA, String strB) {
        if (strA == null || strB == null) {
            return 0.0;
        }
        if (strA.isEmpty() && strB.isEmpty()) {
            return 1.0;
        }
        int distance = levenshteinDistance(strA, strB);
        int maxLen = Math.max(strA.length(), strB.length());
        return 1.0 - (double) distance / maxLen;
    }

    /**
     * 求最长公共子串
     */
    private static String longestCommonSubstring(String strA, String strB) {
        char[] a = removeNonCJK(strA).toCharArray();
        char[] b = removeNonCJK(strB).toCharArray();
        int[][] dp = new int[a.length + 1][b.length + 1];
        int maxLen = 0;
        int endIndex = 0;

        for (int i = 1; i <= a.length; i++) {
            for (int j = 1; j <= b.length; j++) {
                if (a[i - 1] == b[j - 1]) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                    if (dp[i][j] > maxLen) {
                        maxLen = dp[i][j];
                        endIndex = i;
                    }
                }
            }
        }
        return new String(a, endIndex - maxLen, maxLen);
    }

    /**
     * 移除非中日韩字符，仅保留汉字和字母数字
     */
    private static String removeNonCJK(String str) {
        StringBuilder sb = new StringBuilder();
        for (char c : str.toCharArray()) {
            if (isCjkOrAlnum(c)) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static boolean isCjkOrAlnum(char c) {
        return (c >= 0x4E00 && c <= 0x9FA5)   // CJK 汉字
                || (c >= 'a' && c <= 'z')
                || (c >= 'A' && c <= 'Z')
                || (c >= '0' && c <= '9');
    }

    /**
     * Levenshtein 编辑距离
     */
    private static int levenshteinDistance(String strA, String strB) {
        char[] a = strA.toCharArray();
        char[] b = strB.toCharArray();
        int[][] dp = new int[a.length + 1][b.length + 1];

        for (int i = 0; i <= a.length; i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= b.length; j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= a.length; i++) {
            for (int j = 1; j <= b.length; j++) {
                if (a[i - 1] == b[j - 1]) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(dp[i - 1][j - 1],
                            Math.min(dp[i - 1][j], dp[i][j - 1]));
                }
            }
        }
        return dp[a.length][b.length];
    }
}
