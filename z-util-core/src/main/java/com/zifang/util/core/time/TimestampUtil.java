package com.zifang.util.core.time;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * java.sql.Timestamp 工具类
 * <p>
 * 只处理Timestamp类型，提供Timestamp的格式化、解析、转换等功能。
 * 该工具类封装了JDK的Timestamp操作，提供了更友好的API，
 * 支持与LocalDateTime、Instant、Date等类型之间的相互转换。
 * </p>
 * <p>
 * 时间格式化支持的模式：
 * <ul>
 *   <li>{@code yyyy-MM-dd HH:mm:ss} - 默认格式，精确到秒</li>
 *   <li>{@code yyyy-MM-dd HH:mm:ss.SSS} - 精确到毫秒</li>
 *   <li>{@code yyyyMMddHHmmss} - 紧凑格式，精确到秒</li>
 * </ul>
 * </p>
 *
 * @author zifang
 * @see java.sql.Timestamp
 * @see DateUtil
 * @see InstantUtil
 */
public class TimestampUtil {

    /**
     * 默认时间格式：yyyy-MM-dd HH:mm:ss
     */
    public static final String PATTERN_DEFAULT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 毫秒级时间格式：yyyy-MM-dd HH:mm:ss.SSS
     */
    public static final String PATTERN_DATETIME_MS = "yyyy-MM-dd HH:mm:ss.SSS";

    /**
     * 紧凑时间格式：yyyyMMddHHmmss
     */
    public static final String PATTERN_COMPACT = "yyyyMMddHHmmss";

    /**
     * 默认DateTimeFormatter，格式为 {@link #PATTERN_DEFAULT}
     */
    public static final DateTimeFormatter FMT_DEFAULT = DateTimeFormatter.ofPattern(PATTERN_DEFAULT);

    /**
     * 毫秒级DateTimeFormatter，格式为 {@link #PATTERN_DATETIME_MS}
     */
    public static final DateTimeFormatter FMT_DATETIME_MS = DateTimeFormatter.ofPattern(PATTERN_DATETIME_MS);

    // ==================== 格式化 ====================

    /**
     * 将Timestamp格式化为字符串，使用默认格式 {@link #PATTERN_DEFAULT}
     *
     * @param timestamp 要格式化的时间戳，不能为null
     * @return 格式化后的时间字符串
     * @throws NullPointerException if timestamp is null
     */
    public static String format(Timestamp timestamp) {
        return format(timestamp, PATTERN_DEFAULT);
    }

    /**
     * 将Timestamp格式化为指定格式的字符串
     *
     * @param timestamp 要格式化的时间戳，不能为null
     * @param pattern   时间格式模式，如 "yyyy-MM-dd HH:mm:ss"
     * @return 格式化后的时间字符串
     * @throws NullPointerException if timestamp is null
     */
    public static String format(Timestamp timestamp, String pattern) {
        if (timestamp == null) return null;
        return new SimpleDateFormat(pattern).format(timestamp);
    }

    /**
     * 将Timestamp格式化为字符串，使用指定的DateTimeFormatter
     *
     * @param timestamp 要格式化的时间戳，不能为null
     * @param formatter 日期时间格式化器，不能为null
     * @return 格式化后的时间字符串
     * @throws NullPointerException if timestamp or formatter is null
     */
    public static String format(Timestamp timestamp, DateTimeFormatter formatter) {
        if (timestamp == null) return null;
        return formatter.format(toLocalDateTime(timestamp));
    }

    // ==================== 解析 ====================

    /**
     * 解析时间字符串，使用默认格式 {@link #PATTERN_DEFAULT}
     *
     * @param dateStr 时间字符串，格式需符合 {@link #PATTERN_DEFAULT}
     * @return 解析后的Timestamp，如果输入为空则返回null
     * @throws IllegalArgumentException if the string does not match the expected format
     */
    public static Timestamp parse(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return null;
        return Timestamp.valueOf(dateStr);
    }

    /**
     * 解析指定格式的时间字符串
     *
     * @param dateStr 时间字符串
     * @param pattern 时间格式模式，如 "yyyy-MM-dd HH:mm:ss"
     * @return 解析后的Timestamp，解析失败返回null
     */
    public static Timestamp parse(String dateStr, String pattern) {
        if (dateStr == null || dateStr.trim().isEmpty()) return null;
        try {
            return new Timestamp(new SimpleDateFormat(pattern).parse(dateStr).getTime());
        } catch (Exception e) {
            return null;
        }
    }

    // ==================== 获取 ====================

    /**
     * 获取当前时间的Timestamp
     *
     * @return 当前时间的Timestamp对象
     */
    public static Timestamp now() {
        return new Timestamp(System.currentTimeMillis());
    }

    /**
     * 根据毫秒值创建Timestamp
     *
     * @param time 毫秒值，表示自1970年1月1日以来的毫秒数
     * @return 指定毫秒值的Timestamp对象
     */
    public static Timestamp of(long time) {
        return new Timestamp(time);
    }

    /**
     * 根据LocalDateTime创建Timestamp
     *
     * @param localDateTime LocalDateTime对象，不能为null
     * @return 对应的Timestamp对象
     * @throws NullPointerException if localDateTime is null
     */
    public static Timestamp of(LocalDateTime localDateTime) {
        return Timestamp.valueOf(localDateTime);
    }

    /**
     * 根据Instant创建Timestamp
     *
     * @param instant Instant对象，不能为null
     * @return 对应的Timestamp对象
     * @throws NullPointerException if instant is null
     */
    public static Timestamp of(Instant instant) {
        return Timestamp.from(instant);
    }

    // ==================== 时间计算 ====================

    /**
     * 为Timestamp增加指定的毫秒数
     *
     * @param timestamp 原时间戳，不能为null
     * @param millis    要增加的毫秒数，可以为负数
     * @return 增加后的新Timestamp对象
     * @throws NullPointerException if timestamp is null
     */
    public static Timestamp plusMillis(Timestamp timestamp, long millis) {
        return new Timestamp(timestamp.getTime() + millis);
    }

    /**
     * 为Timestamp减少指定的毫秒数
     *
     * @param timestamp 原时间戳，不能为null
     * @param millis    要减少的毫秒数，可以为负数
     * @return 减少后的新Timestamp对象
     * @throws NullPointerException if timestamp is null
     */
    public static Timestamp minusMillis(Timestamp timestamp, long millis) {
        return new Timestamp(timestamp.getTime() - millis);
    }

    /**
     * 为Timestamp增加指定的秒数
     *
     * @param timestamp 原时间戳，不能为null
     * @param seconds   要增加的秒数，可以为负数
     * @return 增加后的新Timestamp对象
     * @throws NullPointerException if timestamp is null
     */
    public static Timestamp plusSeconds(Timestamp timestamp, long seconds) {
        return new Timestamp(timestamp.getTime() + seconds * 1000);
    }

    /**
     * 为Timestamp减少指定的秒数
     *
     * @param timestamp 原时间戳，不能为null
     * @param seconds   要减少的秒数，可以为负数
     * @return 减少后的新Timestamp对象
     * @throws NullPointerException if timestamp is null
     */
    public static Timestamp minusSeconds(Timestamp timestamp, long seconds) {
        return new Timestamp(timestamp.getTime() - seconds * 1000);
    }

    // ==================== 时间比较 ====================

    /**
     * 判断第一个Timestamp是否在第二个之前
     *
     * @param ts1 第一个时间戳，不能为null
     * @param ts2 第二个时间戳，不能为null
     * @return 如果ts1在ts2之前则返回true
     * @throws NullPointerException if ts1 or ts2 is null
     */
    public static boolean isBefore(Timestamp ts1, Timestamp ts2) {
        return ts1.before(ts2);
    }

    /**
     * 判断第一个Timestamp是否在第二个之后
     *
     * @param ts1 第一个时间戳，不能为null
     * @param ts2 第二个时间戳，不能为null
     * @return 如果ts1在ts2之后则返回true
     * @throws NullPointerException if ts1 or ts2 is null
     */
    public static boolean isAfter(Timestamp ts1, Timestamp ts2) {
        return ts1.after(ts2);
    }

    // ==================== 转换到其他类型 ====================

    /**
     * 将Timestamp转换为java.util.Date
     *
     * @param timestamp 要转换的时间戳
     * @return 转换后的Date对象，如果输入为null则返回null
     */
    public static Date toDate(Timestamp timestamp) {
        if (timestamp == null) return null;
        return new Date(timestamp.getTime());
    }

    /**
     * 将Timestamp转换为LocalDateTime
     *
     * @param timestamp 要转换的时间戳
     * @return 转换后的LocalDateTime对象，如果输入为null则返回null
     */
    public static LocalDateTime toLocalDateTime(Timestamp timestamp) {
        if (timestamp == null) return null;
        return timestamp.toLocalDateTime();
    }

    /**
     * 将Timestamp转换为LocalDate
     *
     * @param timestamp 要转换的时间戳
     * @return 转换后的LocalDate对象（不含时间部分），如果输入为null则返回null
     */
    public static LocalDate toLocalDate(Timestamp timestamp) {
        if (timestamp == null) return null;
        return timestamp.toLocalDateTime().toLocalDate();
    }

    /**
     * 将Timestamp转换为Instant
     *
     * @param timestamp 要转换的时间戳
     * @return 转换后的Instant对象，如果输入为null则返回null
     */
    public static Instant toInstant(Timestamp timestamp) {
        if (timestamp == null) return null;
        return timestamp.toInstant();
    }

    /**
     * 将Timestamp转换为自1970年1月1日以来的毫秒数
     *
     * @param timestamp 要转换的时间戳
     * @return 毫秒值，如果输入为null则返回0
     */
    public static long toEpochMilli(Timestamp timestamp) {
        if (timestamp == null) return 0;
        return timestamp.getTime();
    }

    // ==================== 从其他类型转换 ====================

    /**
     * 从java.util.Date转换为Timestamp
     *
     * @param date 要转换的Date对象
     * @return 转换后的Timestamp对象，如果输入为null则返回null
     */
    public static Timestamp fromDate(Date date) {
        if (date == null) return null;
        return new Timestamp(date.getTime());
    }

    /**
     * 从LocalDateTime转换为Timestamp
     *
     * @param localDateTime 要转换的LocalDateTime对象
     * @return 转换后的Timestamp对象，如果输入为null则返回null
     */
    public static Timestamp fromLocalDateTime(LocalDateTime localDateTime) {
        if (localDateTime == null) return null;
        return Timestamp.valueOf(localDateTime);
    }

    /**
     * 从LocalDate转换为Timestamp
     * <p>
     * 转换后的Timestamp时间为指定日期的凌晨00:00:00
     *
     * @param localDate 要转换的LocalDate对象
     * @return 转换后的Timestamp对象，如果输入为null则返回null
     */
    public static Timestamp fromLocalDate(LocalDate localDate) {
        if (localDate == null) return null;
        return Timestamp.valueOf(localDate.atStartOfDay());
    }

    /**
     * 从Instant转换为Timestamp
     *
     * @param instant 要转换的Instant对象
     * @return 转换后的Timestamp对象，如果输入为null则返回null
     */
    public static Timestamp fromInstant(Instant instant) {
        if (instant == null) return null;
        return Timestamp.from(instant);
    }

    /**
     * 从毫秒值创建Timestamp
     *
     * @param epochMilli 自1970年1月1日以来的毫秒数
     * @return 对应的Timestamp对象
     */
    public static Timestamp fromEpochMilli(long epochMilli) {
        return new Timestamp(epochMilli);
    }
}
