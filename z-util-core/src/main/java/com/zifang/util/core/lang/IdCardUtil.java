package com.zifang.util.core.lang;

import com.zifang.util.core.time.LocalDateUtil;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

/**
 * 身份证号码工具类
 * 提供居民身份证号码的格式校验、校验位计算与 15 位升 18 位转换，
 * 以及出生日期、区域码、性别、年龄等基础信息提取
 */
public class IdCardUtil {

    /**
     * 18 位身份证号码格式（出生日期与末位校验码格式约束）
     */
    private static final Pattern PATTERN_EIGHTEEN = Pattern.compile(
            "^[1-9]\\d{5}(18|19|20)\\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$");

    /**
     * 15 位身份证号码格式
     */
    private static final Pattern PATTERN_FIFTEEN = Pattern.compile(
            "^[1-9]\\d{5}\\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\\d{3}$");

    /**
     * 前 17 位加权因子
     */
    private static final int[] WEIGHTS = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};

    /**
     * 模 11 校验码表
     */
    private static final char[] CHECK_CODES = {'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'};

    /**
     * 校验身份证号码是否有效（格式 + 18 位校验码）。
     * 15 位号码无校验码，格式合法即视为有效；
     * 18 位号码在格式合法的基础上还需校验码匹配（末位 x 不区分大小写）。
     *
     * @param cardNo 身份证号码
     * @return true-有效，false-无效
     */
    public static boolean isValid(String cardNo) {
        if (!isValidFormat(cardNo)) {
            return false;
        }
        if (cardNo.length() == 15) {
            return true;
        }
        return Character.toUpperCase(cardNo.charAt(17)) == getCheckCode(cardNo);
    }

    /**
     * 校验身份证号码格式（不校验校验码）。
     *
     * @param cardNo 身份证号码，支持 15 位与 18 位
     * @return true-格式合法，false-格式非法
     */
    public static boolean isValidFormat(String cardNo) {
        if (cardNo == null) {
            return false;
        }
        if (cardNo.length() == 18) {
            return PATTERN_EIGHTEEN.matcher(cardNo).matches();
        }
        if (cardNo.length() == 15) {
            return PATTERN_FIFTEEN.matcher(cardNo).matches();
        }
        return false;
    }

    /**
     * 将 15 位身份证号码升级为 18 位。
     * 在第 6 位后补出生世纪 "19"，并按前 17 位计算校验码追加为第 18 位。
     *
     * @param fifteenCardNo 15 位身份证号码
     * @return 18 位身份证号码；入参为 null 或非合法 15 位号码时返回 null
     */
    public static String toEighteen(String fifteenCardNo) {
        if (!isValidFormat(fifteenCardNo) || fifteenCardNo.length() != 15) {
            return null;
        }
        String seventeen = fifteenCardNo.substring(0, 6) + "19" + fifteenCardNo.substring(6);
        return seventeen + getCheckCode(seventeen);
    }

    /**
     * 计算身份证号码的校验码（第 18 位）。
     * 前 17 位按位乘以加权因子求和，对 11 取模后查校验码表。
     *
     * @param cardNo 17 位（不含校验码）或 18 位（含校验码，仅取前 17 位参与计算）号码
     * @return 校验码字符（可能为数字或 X）
     * @throws IllegalArgumentException 号码长度不为 17/18 或前 17 位含非数字时抛出
     */
    public static char getCheckCode(String cardNo) {
        if (cardNo == null || (cardNo.length() != 17 && cardNo.length() != 18)) {
            throw new IllegalArgumentException("cardNo length must be 17 or 18: " + cardNo);
        }
        int sum = 0;
        for (int i = 0; i < 17; i++) {
            char c = cardNo.charAt(i);
            if (c < '0' || c > '9') {
                throw new IllegalArgumentException("cardNo first 17 chars must be digits: " + cardNo);
            }
            sum += (c - '0') * WEIGHTS[i];
        }
        return CHECK_CODES[sum % 11];
    }

    /**
     * 从身份证号码提取出生日期字符串。
     * 15 位号码在第 6 位后补世纪 "19"。
     *
     * @param cardNo 身份证号码，支持 15 位与 18 位
     * @return 出生日期字符串（yyyy-MM-dd）；号码为 null 或长度非法时返回 null；
     * 仅做字符位截取，不校验日期合法性
     */
    public static String getBirthday(String cardNo) {
        if (cardNo == null) {
            return null;
        }
        if (cardNo.length() == 18) {
            return cardNo.substring(6, 10) + "-" + cardNo.substring(10, 12) + "-" + cardNo.substring(12, 14);
        }
        if (cardNo.length() == 15) {
            return "19" + cardNo.substring(6, 8) + "-" + cardNo.substring(8, 10) + "-" + cardNo.substring(10, 12);
        }
        return null;
    }

    /**
     * 从身份证号码提取出生日期。
     *
     * @param cardNo 身份证号码，支持 15 位与 18 位
     * @return 出生日期；号码为 null、长度非法或出生日期不存在时返回 null
     */
    public static LocalDate getBirthDate(String cardNo) {
        String birthday = getBirthday(cardNo);
        if (birthday == null) {
            return null;
        }
        try {
            return LocalDate.parse(birthday);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * 从身份证号码提取行政区划码（前 6 位）。
     *
     * @param cardNo 身份证号码，支持 15 位与 18 位
     * @return 行政区划码；号码为 null 或长度非法时返回 null
     */
    public static String getAreaCode(String cardNo) {
        if (cardNo == null || (cardNo.length() != 15 && cardNo.length() != 18)) {
            return null;
        }
        return cardNo.substring(0, 6);
    }

    /**
     * 从身份证号码提取性别。
     * 18 位号码取第 17 位顺序码末位，15 位号码取第 15 位，奇数为男、偶数为女。
     *
     * @param cardNo 身份证号码，支持 15 位与 18 位
     * @return 1-男，0-女；号码为 null、长度非法或顺序码位非数字时返回 -1
     */
    public static int getGenderCode(String cardNo) {
        if (cardNo == null) {
            return -1;
        }
        int index;
        if (cardNo.length() == 18) {
            index = 16;
        } else if (cardNo.length() == 15) {
            index = 14;
        } else {
            return -1;
        }
        char c = cardNo.charAt(index);
        if (c < '0' || c > '9') {
            return -1;
        }
        return (c - '0') % 2;
    }

    /**
     * 从身份证号码提取周岁年龄（相对当前日期）。
     *
     * @param cardNo 身份证号码，支持 15 位与 18 位
     * @return 周岁年龄；号码为 null、长度非法或出生日期不存在时返回 -1
     */
    public static int getAge(String cardNo) {
        LocalDate birthDate = getBirthDate(cardNo);
        if (birthDate == null) {
            return -1;
        }
        return LocalDateUtil.age(birthDate);
    }
}
