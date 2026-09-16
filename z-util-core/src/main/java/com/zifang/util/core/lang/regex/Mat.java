package com.zifang.util.core.lang.regex;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 正则表达式匹配工具类。
 *
 * @author zifang
 */
public class Mat {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final Pattern IP_PATTERN = Pattern.compile(
            "^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$");

    private static final Pattern MOBILE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    private static final Pattern ID_CARD_PATTERN = Pattern.compile(
            "^[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[\\dXx]$");

    private static final Pattern PASSPORT_PATTERN = Pattern.compile("^[a-zA-Z0-9]{5,17}$");

    private Mat() {
    }

    /**
     * 判断字符串是否匹配指定正则。
     *
     * @param input 待检测字符串
     * @param regex 正则表达式
     * @return 是否匹配
     */
    public static boolean isMatch(String input, String regex) {
        if (input == null || regex == null) {
            return false;
        }
        return Pattern.matches(regex, input);
    }

    /**
     * 判断字符串是否匹配指定模式。
     *
     * @param input   待检测字符串
     * @param pattern 正则模式
     * @return 是否匹配
     */
    public static boolean isMatch(String input, Pattern pattern) {
        if (input == null || pattern == null) {
            return false;
        }
        return pattern.matcher(input).matches();
    }

    /**
     * 从字符串中提取第一个匹配的子串。
     *
     * @param input 待提取字符串
     * @param regex 正则表达式
     * @return 匹配的子串，未匹配返回 null
     */
    public static String getFirst(String input, String regex) {
        if (input == null || regex == null) {
            return null;
        }
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    /**
     * 从字符串中提取所有匹配的子串。
     *
     * @param input 待提取字符串
     * @param regex 正则表达式
     * @return 匹配的子串数组
     */
    public static String[] getAll(String input, String regex) {
        if (input == null || regex == null) {
            return new String[0];
        }
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        java.util.List<String> results = new java.util.ArrayList<>();
        while (matcher.find()) {
            results.add(matcher.group());
        }
        return results.toArray(new String[0]);
    }

    /**
     * 验证邮箱格式。
     */
    public static boolean isEmail(String input) {
        return isMatch(input, EMAIL_PATTERN);
    }

    /**
     * 验证IP地址格式。
     */
    public static boolean isIp(String input) {
        return isMatch(input, IP_PATTERN);
    }

    /**
     * 验证手机号格式。
     */
    public static boolean isMobile(String input) {
        return isMatch(input, MOBILE_PATTERN);
    }

    /**
     * 验证身份证号格式。
     */
    public static boolean isIdCard(String input) {
        return isMatch(input, ID_CARD_PATTERN);
    }

    /**
     * 验证护照号格式（5-17位字母或数字）。
     */
    public static boolean isPassport(String input) {
        return isMatch(input, PASSPORT_PATTERN);
    }

    /**
     * 替换所有匹配的子串。
     *
     * @param input       原字符串
     * @param regex       正则表达式
     * @param replacement 替换内容
     * @return 替换后的字符串
     */
    public static String replaceAll(String input, String regex, String replacement) {
        if (input == null || regex == null) {
            return input;
        }
        return input.replaceAll(regex, replacement);
    }
}