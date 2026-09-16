package com.zifang.util.core.time;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 智能日期解析器
 * <p>
 * 从多种常见格式的字符串中自动推断格式并解析为日期时间，无需调用方显式指定格式模板。
 * <p>
 * 支持的输入形式：
 * <ul>
 *   <li>纯数字定长格式：yyyy、yyyyMM、yyyyMMdd、yyyyMMddHH、yyyyMMddHHmm、yyyyMMddHHmmss，
 *       如 2026、202608、20260822、2026082214、202608221430、20260822143005</li>
 *   <li>分隔符格式：以任意非数字字符分隔年月日时分秒，字段数3~6个，
 *       如 2026-08-22、2026/8/22、2026.08.22、2026-08-22 14:30、2026-08-22 14:30:05</li>
 *   <li>纯时间格式：H:mm 或 H:mm:ss，自动补充当天日期，如 14:30、14:30:05</li>
 * </ul>
 * <p>
 * 解析规则：
 * <ul>
 *   <li>两位年份按 2000 年窗口解释，如 26-08-22 解析为 2026-08-22</li>
 *   <li>采用严格日期校验，越界日期（如 2026-02-31）返回null而不是滚动到下月</li>
 *   <li>无法识别的输入统一返回null，不抛出异常</li>
 * </ul>
 *
 * @author zifang
 * @see DateUtil
 * @see LocalDateTimeUtil
 */
public class SmartDateParser {

    /**
     * 两位年份的解释基准：小于100的年份加上该基准
     */
    private static final int TWO_DIGIT_YEAR_BASE = 2000;

    /**
     * 解析日期时间字符串为{@link Date}
     *
     * @param text 待解析的日期时间字符串
     * @return 解析结果；无法解析时返回null
     */
    public static Date parse(String text) {
        LocalDateTime dateTime = parseDateTime(text);
        if (dateTime == null) {
            return null;
        }
        return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * 解析日期时间字符串为{@link LocalDate}，仅取日期部分
     *
     * @param text 待解析的日期时间字符串
     * @return 解析结果的日期部分；无法解析时返回null
     */
    public static LocalDate parseDate(String text) {
        LocalDateTime dateTime = parseDateTime(text);
        return dateTime == null ? null : dateTime.toLocalDate();
    }

    /**
     * 解析日期时间字符串为{@link LocalDateTime}
     *
     * @param text 待解析的日期时间字符串
     * @return 解析结果；无法解析时返回null
     */
    public static LocalDateTime parseDateTime(String text) {
        if (text == null) {
            return null;
        }
        String trimmed = text.trim();
        if (trimmed.isEmpty()) {
            return null;
        }

        // 纯数字：按定长格式解析
        if (isDigits(trimmed)) {
            return parseCompact(trimmed);
        }

        // 纯时间（H:mm 或 H:mm:ss）：补充当天日期
        if (trimmed.matches("^\\d{1,2}:\\d{1,2}(:\\d{1,2})?$")) {
            return parseTimeOfToday(trimmed);
        }

        // 分隔符格式：提取数字字段后按年月日时分秒重组
        return parseSegmented(trimmed);
    }

    /**
     * 解析纯数字定长字符串
     * <p>
     * 长度与格式的对应关系：4-yyyy、6-yyyyMM、8-yyyyMMdd、10-yyyyMMddHH、
     * 12-yyyyMMddHHmm、14-yyyyMMddHHmmss，其他长度不支持。
     *
     * @param digits 纯数字字符串
     * @return 解析结果；长度不合法或日期非法时返回null
     */
    private static LocalDateTime parseCompact(String digits) {
        int year;
        int month = 1;
        int day = 1;
        int hour = 0;
        int minute = 0;
        int second = 0;

        switch (digits.length()) {
            case 14:
                second = Integer.parseInt(digits.substring(12, 14));
                // fall through
            case 12:
                minute = Integer.parseInt(digits.substring(10, 12));
                // fall through
            case 10:
                hour = Integer.parseInt(digits.substring(8, 10));
                // fall through
            case 8:
                day = Integer.parseInt(digits.substring(6, 8));
                // fall through
            case 6:
                month = Integer.parseInt(digits.substring(4, 6));
                // fall through
            case 4:
                year = Integer.parseInt(digits.substring(0, 4));
                break;
            default:
                return null;
        }

        return build(year, month, day, hour, minute, second);
    }

    /**
     * 解析纯时间字符串，自动补充当天日期
     *
     * @param text 形如 H:mm 或 H:mm:ss 的时间字符串
     * @return 当天对应的时间点
     */
    private static LocalDateTime parseTimeOfToday(String text) {
        String[] parts = text.split(":");
        LocalDate today = LocalDate.now();
        int hour = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1]);
        int second = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;
        return build(today.getYear(), today.getMonthValue(), today.getDayOfMonth(), hour, minute, second);
    }

    /**
     * 解析带分隔符的字符串
     * <p>
     * 按非数字字符切分出数字字段，字段依次解释为年、月、日、时、分、秒，
     * 字段数须在3~6之间；不足的时分秒补零。
     *
     * @param text 带分隔符的日期时间字符串
     * @return 解析结果；字段数不合法或日期非法时返回null
     */
    private static LocalDateTime parseSegmented(String text) {
        String[] rawSegments = text.split("\\D+");
        List<Integer> fields = new ArrayList<>();
        for (String segment : rawSegments) {
            if (segment.isEmpty()) {
                continue;
            }
            if (!isDigits(segment)) {
                return null;
            }
            fields.add(Integer.parseInt(segment));
        }

        if (fields.size() < 3 || fields.size() > 6) {
            return null;
        }

        int year = fields.get(0);
        // 两位年份按2000年窗口解释
        if (year < 100) {
            year += TWO_DIGIT_YEAR_BASE;
        }
        int month = fields.get(1);
        int day = fields.get(2);
        int hour = fields.size() > 3 ? fields.get(3) : 0;
        int minute = fields.size() > 4 ? fields.get(4) : 0;
        int second = fields.size() > 5 ? fields.get(5) : 0;

        return build(year, month, day, hour, minute, second);
    }

    /**
     * 组装日期时间，越界字段（如2月31日、13月）会导致返回null
     *
     * @param year   年
     * @param month  月
     * @param day    日
     * @param hour   时
     * @param minute 分
     * @param second 秒
     * @return 组装结果；字段越界时返回null
     */
    private static LocalDateTime build(int year, int month, int day, int hour, int minute, int second) {
        try {
            return LocalDateTime.of(year, month, day, hour, minute, second);
        } catch (DateTimeException e) {
            return null;
        }
    }

    /**
     * 判断字符串是否全部由数字字符组成
     *
     * @param text 待判断字符串
     * @return 全部为数字返回true，否则返回false
     */
    private static boolean isDigits(String text) {
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return !text.isEmpty();
    }
}
