package com.zifang.util.ch;

/**
 * 中文数字念法工具类
 * <p>
 * 提供整数的逐位中文念法转换，适用于编号、单号等逐位报数场景，
 * 例如 123 转换为「一二三」，与金额大写（带十百千万等单位，见 {@link MoneyUtil}）不同。
 * <ul>
 *   <li>{@link #digitToChinese(long)}：逐位小写中文（一二三…）</li>
 *   <li>{@link #digitToChineseUpper(long)}：逐位大写中文（壹贰叁…）</li>
 * </ul>
 * 负号转换为「负」前缀；0 转换为「零」。
 *
 * @author zifang
 */
public class NumberChineseUtil {

    /**
     * 小写中文数字，索引对应数字0~9
     */
    private static final char[] DIGITS_LOWER = {'零', '一', '二', '三', '四', '五', '六', '七', '八', '九'};

    /**
     * 大写中文数字，索引对应数字0~9
     */
    private static final char[] DIGITS_UPPER = {'零', '壹', '贰', '叁', '肆', '伍', '陆', '柒', '捌', '玖'};

    private NumberChineseUtil() {
    }

    /**
     * 将整数逐位转换为小写中文（一二三四五六七八九）
     * <p>
     * 例如 123 得到「一二三」，10050 得到「一零零五零」，-25 得到「负二五」。
     *
     * @param number 待转换整数
     * @return 逐位小写中文字符串
     */
    public static String digitToChinese(long number) {
        return mapDigits(Long.toString(number), DIGITS_LOWER);
    }

    /**
     * 将整数逐位转换为大写中文（壹贰叁肆伍陆柒捌玖）
     * <p>
     * 例如 123 得到「壹贰叁」，-25 得到「负贰伍」。
     *
     * @param number 待转换整数
     * @return 逐位大写中文字符串
     */
    public static String digitToChineseUpper(long number) {
        return mapDigits(Long.toString(number), DIGITS_UPPER);
    }

    private static String mapDigits(String number, char[] mapping) {
        StringBuilder sb = new StringBuilder(number.length());
        for (int i = 0; i < number.length(); i++) {
            char c = number.charAt(i);
            if (c >= '0' && c <= '9') {
                sb.append(mapping[c - '0']);
            } else if (c == '-') {
                sb.append('负');
            }
        }
        return sb.toString();
    }
}
