package com.zifang.util.core.time;

import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * java.sql.Date 工具类
 * <p>
 * 只处理 {@link java.sql.Date} 类型，提供格式化、解析、转换等功能。
 * 注意：java.sql.Date 只有日期部分，时间部分被截断为零值。
 * <p>
 * 主要功能：
 * <ul>
 *   <li>格式化：支持多种日期格式模式</li>
 *   <li>解析：支持严格解析和多种模式尝试</li>
 *   <li>日期计算：加减天数、月数、年数</li>
 *   <li>日期比较：比较日期先后、判断是否同一天</li>
 *   <li>类型转换：与 LocalDate、LocalDateTime、Instant 等互转</li>
 * </ul>
 * <p>
 * 示例用法：
 * <pre>{@code
 * // 格式化
 * String str = SqlDateUtil.format(SqlDateUtil.now());
 * String str2 = SqlDateUtil.format(date, SqlDateUtil.PATTERN_CHINESE);
 *
 * // 解析
 * Date date = SqlDateUtil.parse("2024-01-15");
 * Date date2 = SqlDateUtil.parseStrict("20240115", "yyyyMMdd", "yyyy-MM-dd");
 *
 * // 日期计算
 * Date tomorrow = SqlDateUtil.plusDays(date, 1);
 * Date lastMonth = SqlDateUtil.minusMonths(date, 1);
 *
 * // 类型转换
 * LocalDate ld = SqlDateUtil.toLocalDate(date);
 * Date fromLd = SqlDateUtil.fromLocalDate(LocalDate.now());
 * }</pre>
 *
 * @author zifang
 * @see java.sql.Date
 * @see LocalDate
 * @see DateTimeFormatter
 */
public class SqlDateUtil {

    /**
     * 默认日期格式：yyyy-MM-dd
     */
    public static final String PATTERN_DEFAULT = "yyyy-MM-dd";

    /**
     * 紧凑日期格式：yyyyMMdd
     */
    public static final String PATTERN_COMPACT = "yyyyMMdd";

    /**
     * 月份格式：yyyy-MM
     */
    public static final String PATTERN_MONTH = "yyyy-MM";

    /**
     * 中文日期格式：yyyy年MM月dd日
     */
    public static final String PATTERN_CHINESE = "yyyy年MM月dd日";

    /**
     * 默认日期格式化器
     */
    public static final DateTimeFormatter FMT_DEFAULT = DateTimeFormatter.ofPattern(PATTERN_DEFAULT);

    /**
     * 紧凑日期格式化器
     */
    public static final DateTimeFormatter FMT_COMPACT = DateTimeFormatter.ofPattern(PATTERN_COMPACT);

    /**
     * 中文日期格式化器
     */
    public static final DateTimeFormatter FMT_CHINESE = DateTimeFormatter.ofPattern(PATTERN_CHINESE);

    // ==================== 格式化 ====================

    /**
     * 使用默认格式（yyyy-MM-dd）格式化日期
     *
     * @param date 要格式化的日期（为 null 时返回 null）
     * @return 格式化后的日期字符串，如果 date 为 null 则返回 null
     */
    public static String format(Date date) {
        return format(date, PATTERN_DEFAULT);
    }

    /**
     * 使用指定模式格式化日期
     *
     * @param date    要格式化的日期（为 null 时返回 null）
     * @param pattern 日期模式，例如 "yyyy-MM-dd"
     * @return 格式化后的日期字符串，如果 date 为 null 则返回 null
     * @throws IllegalArgumentException 如果 pattern 格式无效
     */
    public static String format(Date date, String pattern) {
        if (date == null) return null;
        return new java.text.SimpleDateFormat(pattern).format(date);
    }

    /**
     * 使用指定格式化器格式化日期
     *
     * @param date      要格式化的日期（为 null 时返回 null）
     * @param formatter 日期时间格式化器
     * @return 格式化后的日期字符串，如果 date 为 null 则返回 null
     */
    public static String format(Date date, DateTimeFormatter formatter) {
        if (date == null) return null;
        return formatter.format(toLocalDate(date));
    }

    // ==================== 解析 ====================

    /**
     * 使用默认格式（yyyy-MM-dd）解析日期字符串
     *
     * @param dateStr 日期字符串（为 null 或空时返回 null）
     * @return 解析后的 Date 对象，解析失败返回 null
     */
    public static Date parse(String dateStr) {
        return parse(dateStr, PATTERN_DEFAULT);
    }

    /**
     * 使用指定模式解析日期字符串
     *
     * @param dateStr 日期字符串（为 null 或空时返回 null）
     * @param pattern 日期模式，例如 "yyyy-MM-dd"
     * @return 解析后的 Date 对象，解析失败返回 null
     * @throws IllegalArgumentException 如果 pattern 格式无效
     */
    public static Date parse(String dateStr, String pattern) {
        if (dateStr == null || dateStr.trim().isEmpty()) return null;
        try {
            return new Date(new java.text.SimpleDateFormat(pattern).parse(dateStr).getTime());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 使用多种模式严格解析日期字符串
     * <p>
     * 尝试使用提供的多种模式依次解析，直到成功或全部失败。
     * 这对于解析来自不同来源的日期字符串很有用。
     *
     * @param dateStr  日期字符串（为 null 或空时返回 null）
     * @param patterns 多种日期模式，按尝试顺序排列
     * @return 解析后的 Date 对象，所有模式都失败返回 null
     */
    public static Date parseStrict(String dateStr, String... patterns) {
        if (dateStr == null || dateStr.trim().isEmpty()) return null;
        for (String pattern : patterns) {
            Date result = parse(dateStr, pattern);
            if (result != null) return result;
        }
        return null;
    }

    // ==================== 获取 ====================

    /**
     * 获取当前时间的 java.sql.Date
     *
     * @return 表示当前时刻的 Date 对象
     */
    public static Date now() {
        return new Date(System.currentTimeMillis());
    }

    /**
     * 获取今天的 java.sql.Date（等价于 now）
     *
     * @return 表示今天的 Date 对象
     */
    public static Date today() {
        return new Date(System.currentTimeMillis());
    }

    // ==================== 日期计算 ====================

    /**
     * 日期加上指定天数
     *
     * @param date 原始日期（为 null 时返回 null）
     * @param days 要增加的天数（可以为负数）
     * @return 计算后的日期，如果 date 为 null 则返回 null
     */
    public static Date plusDays(Date date, long days) {
        if (date == null) return null;
        LocalDate ld = toLocalDate(date);
        return toSqlDate(ld.plusDays(days));
    }

    /**
     * 日期减去指定天数
     *
     * @param date 原始日期（为 null 时返回 null）
     * @param days 要减少的天数（可以为负数）
     * @return 计算后的日期，如果 date 为 null 则返回 null
     */
    public static Date minusDays(Date date, long days) {
        if (date == null) return null;
        LocalDate ld = toLocalDate(date);
        return toSqlDate(ld.minusDays(days));
    }

    /**
     * 日期加上指定月数
     *
     * @param date   原始日期（为 null 时返回 null）
     * @param months 要增加的月数（可以为负数）
     * @return 计算后的日期，如果 date 为 null 则返回 null
     */
    public static Date plusMonths(Date date, long months) {
        if (date == null) return null;
        LocalDate ld = toLocalDate(date);
        return toSqlDate(ld.plusMonths(months));
    }

    /**
     * 日期减去指定月数
     *
     * @param date   原始日期（为 null 时返回 null）
     * @param months 要减少的月数（可以为负数）
     * @return 计算后的日期，如果 date 为 null 则返回 null
     */
    public static Date minusMonths(Date date, long months) {
        if (date == null) return null;
        LocalDate ld = toLocalDate(date);
        return toSqlDate(ld.minusMonths(months));
    }

    /**
     * 日期加上指定年数
     *
     * @param date  原始日期（为 null 时返回 null）
     * @param years 要增加的年数（可以为负数）
     * @return 计算后的日期，如果 date 为 null 则返回 null
     */
    public static Date plusYears(Date date, long years) {
        if (date == null) return null;
        LocalDate ld = toLocalDate(date);
        return toSqlDate(ld.plusYears(years));
    }

    /**
     * 日期减去指定年数
     *
     * @param date  原始日期（为 null 时返回 null）
     * @param years 要减少的年数（可以为负数）
     * @return 计算后的日期，如果 date 为 null 则返回 null
     */
    public static Date minusYears(Date date, long years) {
        if (date == null) return null;
        LocalDate ld = toLocalDate(date);
        return toSqlDate(ld.minusYears(years));
    }

    // ==================== 日期比较 ====================

    /**
     * 判断 date1 是否在 date2 之前
     *
     * @param date1 第一个日期
     * @param date2 第二个日期
     * @return date1 是否在 date2 之前
     */
    public static boolean isBefore(Date date1, Date date2) {
        return date1.before(date2);
    }

    /**
     * 判断 date1 是否在 date2 之后
     *
     * @param date1 第一个日期
     * @param date2 第二个日期
     * @return date1 是否在 date2 之后
     */
    public static boolean isAfter(Date date1, Date date2) {
        return date1.after(date2);
    }

    /**
     * 判断两个日期是否是同一天
     *
     * @param date1 第一个日期
     * @param date2 第二个日期
     * @return 两个日期是否是同一天
     */
    public static boolean isSameDay(Date date1, Date date2) {
        LocalDate d1 = toLocalDate(date1);
        LocalDate d2 = toLocalDate(date2);
        return d1.equals(d2);
    }

    // ==================== 日期差 ====================

    /**
     * 计算两个日期之间的天数差
     *
     * @param start 开始日期（为 null 时返回 0）
     * @param end   结束日期（为 null 时返回 0）
     * @return 天数差（end - start），可以为负数
     */
    public static long daysBetween(Date start, Date end) {
        LocalDate d1 = toLocalDate(start);
        LocalDate d2 = toLocalDate(end);
        return java.time.temporal.ChronoUnit.DAYS.between(d1, d2);
    }

    // ==================== 获取日期部分 ====================

    /**
     * 获取日期中的年份
     *
     * @param date 日期（为 null 时返回 0）
     * @return 年份值
     */
    public static int getYear(Date date) {
        return toLocalDate(date).getYear();
    }

    /**
     * 获取日期中的月份
     *
     * @param date 日期（为 null 时返回 0）
     * @return 月份值（1-12）
     */
    public static int getMonth(Date date) {
        return toLocalDate(date).getMonthValue();
    }

    /**
     * 获取日期中的日
     *
     * @param date 日期（为 null 时返回 0）
     * @return 日值（1-31）
     */
    public static int getDayOfMonth(Date date) {
        return toLocalDate(date).getDayOfMonth();
    }

    /**
     * 获取日期是星期几
     *
     * @param date 日期（为 null 时返回 0）
     * @return 星期几（1=Monday, 7=Sunday）
     */
    public static int getDayOfWeek(Date date) {
        return toLocalDate(date).getDayOfWeek().getValue();
    }

    // ==================== 转换到其他类型 ====================

    /**
     * 将 java.sql.Date 转换为 java.util.Date
     * <p>
     * java.sql.Date 是 java.util.Date 的子类，此转换主要用于 API 场景需要 java.util.Date 的情况。
     *
     * @param date java.sql.Date（为 null 时返回 null）
     * @return java.util.Date，如果输入为 null 则返回 null
     */
    public static java.util.Date toUtilDate(Date date) {
        if (date == null) return null;
        return new java.util.Date(date.getTime());
    }

    /**
     * 将 java.sql.Date 转换为 LocalDate
     *
     * @param date java.sql.Date（为 null 时返回 null）
     * @return LocalDate，如果输入为 null 则返回 null
     */
    public static LocalDate toLocalDate(Date date) {
        if (date == null) return null;
        return date.toLocalDate();
    }

    /**
     * 将 java.sql.Date 转换为 LocalDateTime
     * <p>
     * java.sql.Date 只包含日期，时间部分被设置为午夜。
     *
     * @param date java.sql.Date（为 null 时返回 null）
     * @return LocalDateTime，如果输入为 null 则返回 null
     */
    public static LocalDateTime toLocalDateTime(Date date) {
        if (date == null) return null;
        return date.toLocalDate().atStartOfDay();
    }

    /**
     * 将 java.sql.Date 转换为 LocalTime
     * <p>
     * 由于 java.sql.Date 不包含时间，时间部分固定为午夜。
     *
     * @param date java.sql.Date（为 null 时返回 null）
     * @return LocalTime.MIDNIGHT (00:00)，如果输入为 null 则返回 null
     */
    public static LocalTime toLocalTime(Date date) {
        if (date == null) return null;
        return LocalTime.MIDNIGHT;
    }

    /**
     * 将 java.sql.Date 转换为 Instant
     *
     * @param date java.sql.Date（为 null 时返回 null）
     * @return Instant，如果输入为 null 则返回 null
     */
    public static Instant toInstant(Date date) {
        if (date == null) return null;
        return Instant.ofEpochMilli(date.getTime());
    }

    /**
     * 将 java.sql.Date 转换为 epoch 毫秒数
     *
     * @param date java.sql.Date（为 null 时返回 0）
     * @return epoch 毫秒数
     */
    public static long toEpochMilli(Date date) {
        if (date == null) return 0;
        return date.getTime();
    }

    // ==================== 从其他类型转换 ====================

    /**
     * 从 java.util.Date 转换为 java.sql.Date
     *
     * @param date java.util.Date（为 null 时返回 null）
     * @return java.sql.Date，如果输入为 null 则返回 null
     */
    public static Date fromUtilDate(java.util.Date date) {
        if (date == null) return null;
        return new Date(date.getTime());
    }

    /**
     * 从 LocalDate 转换为 java.sql.Date
     *
     * @param localDate LocalDate（为 null 时返回 null）
     * @return java.sql.Date，如果输入为 null 则返回 null
     */
    public static Date fromLocalDate(LocalDate localDate) {
        if (localDate == null) return null;
        return Date.valueOf(localDate);
    }

    /**
     * 从 LocalDateTime 转换为 java.sql.Date
     * <p>
     * 只使用 LocalDateTime 的日期部分，时间部分被丢弃。
     *
     * @param localDateTime LocalDateTime（为 null 时返回 null）
     * @return java.sql.Date，如果输入为 null 则返回 null
     */
    public static Date fromLocalDateTime(LocalDateTime localDateTime) {
        if (localDateTime == null) return null;
        return Date.valueOf(localDateTime.toLocalDate());
    }

    /**
     * 从 Instant 转换为 java.sql.Date
     *
     * @param instant Instant（为 null 时返回 null）
     * @return java.sql.Date，如果输入为 null 则返回 null
     */
    public static Date fromInstant(Instant instant) {
        if (instant == null) return null;
        return new Date(instant.toEpochMilli());
    }

    /**
     * 从 epoch 毫秒数创建 java.sql.Date
     *
     * @param epochMilli epoch 毫秒数
     * @return 对应的 java.sql.Date
     */
    public static Date fromEpochMilli(long epochMilli) {
        return new Date(epochMilli);
    }

    // ==================== 内部方法 ====================

    /**
     * 将 LocalDate 转换为 java.sql.Date
     *
     * @param localDate LocalDate
     * @return java.sql.Date
     */
    private static Date toSqlDate(LocalDate localDate) {
        return Date.valueOf(localDate);
    }
}
