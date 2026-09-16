package com.zifang.util.core.time;

import com.zifang.util.core.time.converter.TimeConverter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * Date工具类（统一版本）
 * <p>
 * 提供Date类型的格式化、解析、计算、转换等功能。
 * 与其他时间类型的转换通过{@link TimeConverter}完成。
 * </p>
 * <p>
 * 方法分类：
 * <ul>
 *   <li>格式化/解析：format/parse 方法</li>
 *   <li>获取当前时间：now/todayStart/todayEnd/dayStart/dayEnd</li>
 *   <li>日期计算：plus/minus 系列方法</li>
 *   <li>日期比较：isBefore/isAfter/isSameDay/isSameInstant</li>
 *   <li>时间差计算：daysBetween/hoursBetween/secondsBetween/millisBetween</li>
 *   <li>类型转换：toLocalDateTime/toLocalDate/toInstant 等</li>
 * </ul>
 * </p>
 *
 * @author zifang
 * @see TimeConverter
 * @see LocalDateTimeUtil
 * @see InstantUtil
 * @see DurationUtil
 */
public class DateUtil {

    // ==================== 常量定义 ====================

    public static final String PATTERN_DEFAULT = "yyyy-MM-dd HH:mm:ss";
    public static final String PATTERN_DATE = "yyyy-MM-dd";
    public static final String PATTERN_TIME = "HH:mm:ss";
    public static final String PATTERN_DATETIME_MS = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final String PATTERN_DATETIME_COMPACT = "yyyyMMddHHmmss";
    public static final String PATTERN_DATE_COMPACT = "yyyyMMdd";
    public static final String PATTERN_ISO = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

    /**
     * DateTimeFormatter.ofPattern方法。
     * * @param PATTERN_DEFAULT Object类型参数
     *
     * @return static final DateTimeFormatter FMT_DEFAULT =类型返回值
     */
    public static final DateTimeFormatter FMT_DEFAULT = DateTimeFormatter.ofPattern(PATTERN_DEFAULT);
    /**
     * DateTimeFormatter.ofPattern方法。
     * * @param PATTERN_DATE Object类型参数
     *
     * @return static final DateTimeFormatter FMT_DATE =类型返回值
     */
    public static final DateTimeFormatter FMT_DATE = DateTimeFormatter.ofPattern(PATTERN_DATE);
    /**
     * DateTimeFormatter.ofPattern方法。
     * * @param PATTERN_TIME Object类型参数
     *
     * @return static final DateTimeFormatter FMT_TIME =类型返回值
     */
    public static final DateTimeFormatter FMT_TIME = DateTimeFormatter.ofPattern(PATTERN_TIME);
    /**
     * DateTimeFormatter.ofPattern方法。
     * * @param PATTERN_DATETIME_MS Object类型参数
     *
     * @return static final DateTimeFormatter FMT_DATETIME_MS =类型返回值
     */
    public static final DateTimeFormatter FMT_DATETIME_MS = DateTimeFormatter.ofPattern(PATTERN_DATETIME_MS);

    private static final ThreadLocal<Map<String, SimpleDateFormat>> FORMAT_CACHE = ThreadLocal.withInitial(HashMap::new);

    // ==================== 格式化 ====================

    /**
     * 格式化日期为字符串，使用默认格式 PATTERN_DEFAULT
     */
    public static String format(Date date) {
        return format(date, PATTERN_DEFAULT);
    }

    /**
     * 格式化日期为字符串
     */
    public static String format(Date date, String pattern) {
        if (date == null) return null;
        return getFormat(pattern).format(date);
    }

    /**
     * 格式化日期为字符串（使用DateTimeFormatter）
     */
    public static String format(Date date, DateTimeFormatter formatter) {
        if (date == null) return null;
        return formatter.format(date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
    }

    // ==================== 解析 ====================

    /**
     * 解析日期字符串，使用默认格式
     */
    public static Date parse(String dateStr) throws ParseException {
        return parse(dateStr, PATTERN_DEFAULT);
    }

    /**
     * 解析日期字符串
     */
    public static Date parse(String dateStr, String pattern) throws ParseException {
        if (dateStr == null || dateStr.trim().isEmpty()) return null;
        return getFormat(pattern).parse(dateStr);
    }

    /**
     * 解析日期字符串（宽松模式，尝试多种模式）
     */
    public static Date parseStrict(String dateStr, String... patterns) {
        if (dateStr == null || dateStr.trim().isEmpty()) return null;
        for (String pattern : patterns) {
            try {
                return getFormat(pattern).parse(dateStr);
            } catch (ParseException e) {
                // 尝试下一个模式
            }
        }
        return null;
    }

    // ==================== 获取 ====================

    /**
     * 获取当前日期
     */
    public static Date now() {
        return new Date();
    }

    /**
     * 获取今天的开始时间
     */
    public static Date todayStart() {
        LocalDate today = LocalDate.now();
        return TimeConverter.toDate(today.atStartOfDay());
    }

    /**
     * 获取今天的结束时间
     */
    public static Date todayEnd() {
        LocalDate today = LocalDate.now();
        return TimeConverter.toDate(today.atTime(LocalTime.MAX));
    }

    /**
     * 获取指定日期的开始时间
     */
    public static Date dayStart(Date date) {
        LocalDate localDate = TimeConverter.toLocalDate(date);
        return TimeConverter.toDate(localDate.atStartOfDay());
    }

    /**
     * 获取指定日期的结束时间
     */
    public static Date dayEnd(Date date) {
        LocalDate localDate = TimeConverter.toLocalDate(date);
        return TimeConverter.toDate(localDate.atTime(LocalTime.MAX));
    }

    /**
     * 获取今天的开始时间字符串
     */
    public static String getTodayStartStr() {
        return format(new Date(), PATTERN_DEFAULT);
    }

    /**
     * 获取今天的结束时间字符串
     */
    public static String getTodayEndStr() {
        return format(new Date(), PATTERN_DEFAULT);
    }

    /**
     * 获取偏移天数开始时间
     *
     * @param offset 偏移量（明天为1，今天为0，昨天为-1）
     * @return 偏移天数开始时间的 LocalDateTime
     */
    public static LocalDateTime getDayStart(int offset) {
        return LocalDateTime.of(LocalDate.now().plusDays(offset), LocalTime.of(0, 0, 0));
    }

    /**
     * 获取偏移天数结束时间
     *
     * @param offset 偏移量（明天为1，今天为0，昨天为-1）
     * @return 偏移天数结束时间的 LocalDateTime
     */
    public static LocalDateTime getDayEnd(int offset) {
        return LocalDateTime.of(LocalDate.now().plusDays(offset), LocalTime.of(23, 59, 59));
    }

    // ==================== 日期计算 ====================

    /**
     * 增加天数
     */
    public static Date plusDays(Date date, long days) {
        LocalDateTime ldt = TimeConverter.toLocalDateTime(date);
        return TimeConverter.toDate(ldt.plusDays(days));
    }

    /**
     * 减少天数
     */
    public static Date minusDays(Date date, long days) {
        LocalDateTime ldt = TimeConverter.toLocalDateTime(date);
        return TimeConverter.toDate(ldt.minusDays(days));
    }

    /**
     * 增加小时
     */
    public static Date plusHours(Date date, long hours) {
        LocalDateTime ldt = TimeConverter.toLocalDateTime(date);
        return TimeConverter.toDate(ldt.plusHours(hours));
    }

    /**
     * 减少小时
     */
    public static Date minusHours(Date date, long hours) {
        LocalDateTime ldt = TimeConverter.toLocalDateTime(date);
        return TimeConverter.toDate(ldt.minusHours(hours));
    }

    /**
     * 增加分钟
     */
    public static Date plusMinutes(Date date, long minutes) {
        LocalDateTime ldt = TimeConverter.toLocalDateTime(date);
        return TimeConverter.toDate(ldt.plusMinutes(minutes));
    }

    /**
     * 减少分钟
     */
    public static Date minusMinutes(Date date, long minutes) {
        LocalDateTime ldt = TimeConverter.toLocalDateTime(date);
        return TimeConverter.toDate(ldt.minusMinutes(minutes));
    }

    /**
     * 增加秒
     */
    public static Date plusSeconds(Date date, long seconds) {
        LocalDateTime ldt = TimeConverter.toLocalDateTime(date);
        return TimeConverter.toDate(ldt.plusSeconds(seconds));
    }

    /**
     * 减少秒
     */
    public static Date minusSeconds(Date date, long seconds) {
        LocalDateTime ldt = TimeConverter.toLocalDateTime(date);
        return TimeConverter.toDate(ldt.minusSeconds(seconds));
    }

    // ==================== 日期比较 ====================

    /**
     * 判断是否在之前
     */
    public static boolean isBefore(Date date1, Date date2) {
        return date1.before(date2);
    }

    /**
     * 判断是否在之后
     */
    public static boolean isAfter(Date date1, Date date2) {
        return date1.after(date2);
    }

    /**
     * 判断两个日期是否同一天
     */
    public static boolean isSameDay(Date date1, Date date2) {
        LocalDate d1 = TimeConverter.toLocalDate(date1);
        LocalDate d2 = TimeConverter.toLocalDate(date2);
        return d1.equals(d2);
    }

    /**
     * 判断两个日期是否同一时刻
     */
    public static boolean isSameInstant(Date date1, Date date2) {
        return date1.equals(date2);
    }

    // ==================== 时间差 ====================

    /**
     * 计算两个日期之间的天数差
     */
    public static long daysBetween(Date start, Date end) {
        LocalDate d1 = TimeConverter.toLocalDate(start);
        LocalDate d2 = TimeConverter.toLocalDate(end);
        return ChronoUnit.DAYS.between(d1, d2);
    }

    /**
     * 计算两个日期之间的小时差
     */
    public static long hoursBetween(Date start, Date end) {
        LocalDateTime dt1 = TimeConverter.toLocalDateTime(start);
        LocalDateTime dt2 = TimeConverter.toLocalDateTime(end);
        return ChronoUnit.HOURS.between(dt1, dt2);
    }

    /**
     * 计算两个日期之间的分钟差
     */
    public static long minutesBetween(Date start, Date end) {
        LocalDateTime dt1 = TimeConverter.toLocalDateTime(start);
        LocalDateTime dt2 = TimeConverter.toLocalDateTime(end);
        return ChronoUnit.MINUTES.between(dt1, dt2);
    }

    /**
     * 计算两个日期之间的秒数差
     */
    public static long secondsBetween(Date start, Date end) {
        return (end.getTime() - start.getTime()) / 1000;
    }

    /**
     * 计算两个日期之间的毫秒差
     */
    public static long millisBetween(Date start, Date end) {
        return end.getTime() - start.getTime();
    }

    // ==================== 转换到其他类型 ====================

    /**
     * 转换为LocalDateTime
     */
    public static LocalDateTime toLocalDateTime(Date date) {
        return TimeConverter.toLocalDateTime(date);
    }

    /**
     * 转换为LocalDateTime（指定时区）
     */
    public static LocalDateTime toLocalDateTime(Date date, ZoneId zoneId) {
        return TimeConverter.toLocalDateTime(date, zoneId);
    }

    /**
     * 转换为LocalDate
     */
    public static LocalDate toLocalDate(Date date) {
        return TimeConverter.toLocalDate(date);
    }

    /**
     * 转换为LocalTime
     */
    public static LocalTime toLocalTime(Date date) {
        return TimeConverter.toLocalTime(date);
    }

    /**
     * 转换为Instant
     */
    public static Instant toInstant(Date date) {
        return TimeConverter.toInstant(date);
    }

    /**
     * 转换为时间戳（毫秒）
     */
    public static long toEpochMilli(Date date) {
        return TimeConverter.toEpochMilli(date);
    }

    // ==================== 从其他类型转换 ====================

    /**
     * 从LocalDateTime转换
     */
    public static Date fromLocalDateTime(LocalDateTime localDateTime) {
        return TimeConverter.toDate(localDateTime);
    }

    /**
     * 从LocalDateTime转换（指定时区）
     */
    public static Date fromLocalDateTime(LocalDateTime localDateTime, ZoneId zoneId) {
        return TimeConverter.toDate(localDateTime, zoneId);
    }

    /**
     * 从LocalDate转换
     */
    public static Date fromLocalDate(LocalDate localDate) {
        return TimeConverter.toDate(localDate);
    }

    /**
     * 从时间戳创建
     */
    public static Date fromEpochMilli(long epochMilli) {
        return new Date(epochMilli);
    }

    /**
     * 从Instant转换
     */
    public static Date fromInstant(Instant instant) {
        return TimeConverter.toDate(instant);
    }

    // ==================== Java 8 Time API 封装 ====================

    /**
     * 将时间戳（秒）转换为LocalDateTime（使用系统默认时区）
     */
    public static LocalDateTime timestampToLocalDateTime(long timestamp) {
        int offset = TimeZone.getDefault().getRawOffset() / 3600_000;
        return timestampToLocalDateTime(timestamp, offset);
    }

    /**
     * 将时间戳（秒）转换为LocalDateTime（指定时区偏移量）
     */
    public static LocalDateTime timestampToLocalDateTime(long timestamp, int zoneOffset) {
        return LocalDateTime.ofEpochSecond(timestamp, 0, ZoneOffset.ofHours(zoneOffset));
    }

    /**
     * 将LocalDateTime转换为时间戳（秒）
     */
    public static long localDateTimeToTimestamp(LocalDateTime localDateTime) {
        return localDateTimeToMilliTimestamp(localDateTime) / 1000;
    }

    /**
     * 将LocalDateTime转换为毫秒时间戳
     */
    public static long localDateTimeToMilliTimestamp(LocalDateTime localDateTime) {
        return localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    /**
     * 解析日期时间字符串（支持微秒）
     * <p>
     * 格式：yyyy-MM-dd HH:mm:ss[.微秒]
     */
    public static LocalDateTime parseLocalDateTime(String dateTime) {
        return parseLocalDateTime(dateTime, PATTERN_DEFAULT);
    }

    /**
     * 解析日期时间字符串（支持微秒）
     */
    public static LocalDateTime parseLocalDateTime(String dateTime, String pattern) {
        if (dateTime == null || dateTime.trim().isEmpty()) return null;
        String[] parts = dateTime.split("\\.");
        String baseStr = parts[0];
        LocalDateTime result = LocalDateTime.parse(baseStr, DateTimeFormatter.ofPattern(pattern));
        if (parts.length > 1) {
            int micros = Integer.parseInt(rightPadWithOver(parts[1], 6, "0"));
            result = result.plusNanos(micros * 1_000L);
        }
        return result;
    }

    /**
     * 解析时间字符串（支持微秒）
     * <p>
     * 格式：HH:mm:ss[.微秒]
     */
    public static LocalTime parseLocalTime(String time) {
        return parseLocalTime(time, PATTERN_TIME);
    }

    /**
     * 解析时间字符串（支持微秒）
     */
    public static LocalTime parseLocalTime(String time, String pattern) {
        if (time == null || time.trim().isEmpty()) return null;
        String[] parts = time.split("\\.");
        String baseStr = parts[0];
        LocalTime result = LocalTime.parse(baseStr, DateTimeFormatter.ofPattern(pattern));
        if (parts.length > 1) {
            int micros = Integer.parseInt(rightPadWithOver(parts[1], 6, "0"));
            result = result.plusNanos(micros * 1_000L);
        }
        return result;
    }

    /**
     * 获取带微秒的时间戳
     */
    public static long getMicrosecond(String dateTime) {
        if (dateTime == null || dateTime.trim().isEmpty()) return 0;
        String[] parts = dateTime.split("\\.");
        String baseStr = parts[0];
        LocalDateTime ldt = LocalDateTime.parse(baseStr, FMT_DEFAULT);
        long result = ldt.atZone(ZoneId.systemDefault()).toInstant().getEpochSecond() * 1_000_000;
        if (parts.length > 1) {
            int micros = Integer.parseInt(rightPadWithOver(parts[1], 6, "0"));
            result += micros;
        }
        return result;
    }

    // ==================== 内部方法 ====================

    private static String rightPadWithOver(String str, int size, String padStr) {
        if (str == null) return null;
        if (str.length() > size) {
            return str.substring(0, size);
        }
        StringBuilder sb = new StringBuilder(str);
        while (sb.length() < size) {
            sb.append(padStr);
        }
        return sb.toString();
    }

    private static SimpleDateFormat getFormat(String pattern) {
        if (pattern == null || pattern.trim().isEmpty()) {
            pattern = PATTERN_DEFAULT;
        }
        Map<String, SimpleDateFormat> cache = FORMAT_CACHE.get();
        SimpleDateFormat format = cache.get(pattern);
        if (format == null) {
            format = new SimpleDateFormat(pattern);
            format.setLenient(false);
            cache.put(pattern, format);
        }
        return format;
    }
}