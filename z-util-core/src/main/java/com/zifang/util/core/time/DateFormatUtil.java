package com.zifang.util.core.time;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 日期格式字符串转换工具类
 * <p>
 * 仅处理字符串格式之间的转换，不涉及Date对象。
 * Date对象的格式化请使用 DateUtil
 */
public class DateFormatUtil {

    public static final String DATE_YEAR = "yyyy";
    public static final String DATE_MONTH = "MM";
    public static final String DATE_DAY = "dd";
    public static final String DATE_HOUR = "HH";
    public static final String DATE_MINUTE = "mm";
    public static final String DATE_SECONDS = "ss";

    public static final String DATE_FORMAT_STANDARD = "yyyy-MM-dd";
    public static final String DATE_FORMAT_FULL = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_FORMAT_COMPACT = "yyyyMMdd";
    public static final String TIME_FORMAT_COMPACT = "yyyyMMddHHmmss";

    /**
     * 格式字符串互转
     * <ul>
     *   <li>14位 -> yyyy-MM-dd HH:mm:ss</li>
     *   <li>19位 -> yyyyMMddHHmmss</li>
     *   <li>10位 -> yyyyMMdd</li>
     *   <li>8位  -> yyyy-MM-dd</li>
     * </ul>
     */
    public static String formatString(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        switch (value.length()) {
            case 14:
                return value.substring(0, 4) + "-" + value.substring(4, 6) + "-" + value.substring(6, 8) + " "
                        + value.substring(8, 10) + ":" + value.substring(10, 12) + ":" + value.substring(12, 14);
            case 19:
                return value.substring(0, 4) + value.substring(5, 7) + value.substring(8, 10)
                        + value.substring(11, 13) + value.substring(14, 16) + value.substring(17, 19);
            case 10:
                return value.substring(0, 4) + value.substring(5, 7) + value.substring(8, 10);
            case 8:
                return value.substring(0, 4) + "-" + value.substring(4, 6) + "-" + value.substring(6, 8);
            default:
                return value;
        }
    }

    /**
     * 解析日期字符串为Date（自动识别长度）
     * <ul>
     *   <li>14位: yyyyMMddHHmmss</li>
     *   <li>12位: yyyyMMddHHmm</li>
     *   <li>10位: yyyyMMddHH</li>
     *   <li>8位:  yyyyMMdd</li>
     *   <li>6位:  yyyyMM</li>
     * </ul>
     */
    public static Date parse(String dateStr) {
        return parse(dateStr, null);
    }

    /**
     * parse方法。
     * * @param dateStr String类型参数
     *
     * @param outPattern String类型参数
     * @return static Date类型返回值
     */
    public static Date parse(String dateStr, String outPattern) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        String normalized = dateStr.replace("-", "").replace(":", "");
        if (normalized.chars().allMatch(Character::isDigit) && normalized.equals("0")) {
            return null;
        }

        ParsePosition pos = new ParsePosition(0);
        SimpleDateFormat inFmt;
        switch (normalized.trim().length()) {
            case 14:
                inFmt = new SimpleDateFormat("yyyyMMddHHmmss");
                break;
            case 12:
                inFmt = new SimpleDateFormat("yyyyMMddHHmm");
                break;
            case 10:
                inFmt = new SimpleDateFormat("yyyyMMddHH");
                break;
            case 8:
                inFmt = new SimpleDateFormat("yyyyMMdd");
                break;
            case 6:
                inFmt = new SimpleDateFormat("yyyyMM");
                break;
            default:
                return null;
        }

        Date dt = inFmt.parse(normalized, pos);
        if (dt == null) {
            return null;
        }
        return dt;
    }

    /**
     * 格式化Date为指定格式字符串
     */
    public static String format(Date date, String pattern) {
        if (date == null) {
            return "";
        }
        return new SimpleDateFormat(pattern).format(date);
    }
}
