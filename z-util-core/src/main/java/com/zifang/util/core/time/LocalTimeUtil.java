package com.zifang.util.core.time;

import com.zifang.util.core.time.converter.TimeConverter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * java.time.LocalTime 工具类
 * <p>
 * 只处理LocalTime类型，提供LocalTime的格式化、解析、计算等功能
 */
public class LocalTimeUtil {

    public static final String PATTERN_DEFAULT = "HH:mm:ss";
    public static final String PATTERN_COMPACT = "HHmmss";
    public static final String PATTERN_SHORT = "HH:mm";
    public static final String PATTERN_CHINESE = "HH时mm分ss秒";
    public static final String PATTERN_CHINESE_SHORT = "HH时mm分";

    /**
     * DateTimeFormatter.ofPattern方法。
     * * @param PATTERN_DEFAULT Object类型参数
     *
     * @return static final DateTimeFormatter FMT_DEFAULT =类型返回值
     */
    public static final DateTimeFormatter FMT_DEFAULT = DateTimeFormatter.ofPattern(PATTERN_DEFAULT);
    /**
     * DateTimeFormatter.ofPattern方法。
     * * @param PATTERN_COMPACT Object类型参数
     *
     * @return static final DateTimeFormatter FMT_COMPACT =类型返回值
     */
    public static final DateTimeFormatter FMT_COMPACT = DateTimeFormatter.ofPattern(PATTERN_COMPACT);
    /**
     * DateTimeFormatter.ofPattern方法。
     * * @param PATTERN_SHORT Object类型参数
     *
     * @return static final DateTimeFormatter FMT_SHORT =类型返回值
     */
    public static final DateTimeFormatter FMT_SHORT = DateTimeFormatter.ofPattern(PATTERN_SHORT);
    /**
     * DateTimeFormatter.ofPattern方法。
     * * @param PATTERN_CHINESE Object类型参数
     *
     * @return static final DateTimeFormatter FMT_CHINESE =类型返回值
     */
    public static final DateTimeFormatter FMT_CHINESE = DateTimeFormatter.ofPattern(PATTERN_CHINESE);

    // ==================== 格式化 ====================

    /**
     * format方法。
     * * @param time LocalTime类型参数
     *
     * @return static String类型返回值
     */
    public static String format(LocalTime time) {
        return format(time, FMT_DEFAULT);
    }

    /**
     * format方法。
     * * @param time LocalTime类型参数
     *
     * @param pattern String类型参数
     * @return static String类型返回值
     */
    public static String format(LocalTime time, String pattern) {
        if (time == null) return null;
        return time.format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * format方法。
     * * @param time LocalTime类型参数
     *
     * @param formatter DateTimeFormatter类型参数
     * @return static String类型返回值
     */
    public static String format(LocalTime time, DateTimeFormatter formatter) {
        if (time == null) return null;
        return time.format(formatter);
    }

    // ==================== 解析 ====================

    /**
     * parse方法。
     * * @param timeStr String类型参数
     *
     * @return static LocalTime类型返回值
     */
    public static LocalTime parse(String timeStr) {
        return parse(timeStr, PATTERN_DEFAULT);
    }

    /**
     * parse方法。
     * * @param timeStr String类型参数
     *
     * @param pattern String类型参数
     * @return static LocalTime类型返回值
     */
    public static LocalTime parse(String timeStr, String pattern) {
        if (timeStr == null || timeStr.trim().isEmpty()) return null;
        return LocalTime.parse(timeStr, DateTimeFormatter.ofPattern(pattern));
    }

    // ==================== 获取 ====================

    /**
     * now方法。
     *
     * @return static LocalTime类型返回值
     */
    public static LocalTime now() {
        return LocalTime.now();
    }

    /**
     * of方法。
     * * @param hour int类型参数
     *
     * @param minute int类型参数
     * @return static LocalTime类型返回值
     */
    public static LocalTime of(int hour, int minute) {
        return LocalTime.of(hour, minute);
    }

    /**
     * of方法。
     * * @param hour int类型参数
     *
     * @param minute int类型参数
     * @param second int类型参数
     * @return static LocalTime类型返回值
     */
    public static LocalTime of(int hour, int minute, int second) {
        return LocalTime.of(hour, minute, second);
    }

    /**
     * of方法。
     * * @param hour int类型参数
     *
     * @param minute       int类型参数
     * @param second       int类型参数
     * @param nanoOfSecond int类型参数
     * @return static LocalTime类型返回值
     */
    public static LocalTime of(int hour, int minute, int second, int nanoOfSecond) {
        return LocalTime.of(hour, minute, second, nanoOfSecond);
    }

    /**
     * ofSecondOfDay方法。
     * * @param secondOfDay long类型参数
     *
     * @return static LocalTime类型返回值
     */
    public static LocalTime ofSecondOfDay(long secondOfDay) {
        return LocalTime.ofSecondOfDay(secondOfDay);
    }

    /**
     * ofNanoOfDay方法。
     * * @param nanoOfDay long类型参数
     *
     * @return static LocalTime类型返回值
     */
    public static LocalTime ofNanoOfDay(long nanoOfDay) {
        return LocalTime.ofNanoOfDay(nanoOfDay);
    }

    /**
     * midnight方法。
     *
     * @return static LocalTime类型返回值
     */
    public static LocalTime midnight() {
        return LocalTime.MIDNIGHT;
    }

    /**
     * noon方法。
     *
     * @return static LocalTime类型返回值
     */
    public static LocalTime noon() {
        return LocalTime.NOON;
    }

    // ==================== 时间计算 ====================

    /**
     * plusHours方法。
     * * @param time LocalTime类型参数
     *
     * @param hours long类型参数
     * @return static LocalTime类型返回值
     */
    public static LocalTime plusHours(LocalTime time, long hours) {
        return time.plusHours(hours);
    }

    /**
     * minusHours方法。
     * * @param time LocalTime类型参数
     *
     * @param hours long类型参数
     * @return static LocalTime类型返回值
     */
    public static LocalTime minusHours(LocalTime time, long hours) {
        return time.minusHours(hours);
    }

    /**
     * plusMinutes方法。
     * * @param time LocalTime类型参数
     *
     * @param minutes long类型参数
     * @return static LocalTime类型返回值
     */
    public static LocalTime plusMinutes(LocalTime time, long minutes) {
        return time.plusMinutes(minutes);
    }

    /**
     * minusMinutes方法。
     * * @param time LocalTime类型参数
     *
     * @param minutes long类型参数
     * @return static LocalTime类型返回值
     */
    public static LocalTime minusMinutes(LocalTime time, long minutes) {
        return time.minusMinutes(minutes);
    }

    /**
     * plusSeconds方法。
     * * @param time LocalTime类型参数
     *
     * @param seconds long类型参数
     * @return static LocalTime类型返回值
     */
    public static LocalTime plusSeconds(LocalTime time, long seconds) {
        return time.plusSeconds(seconds);
    }

    /**
     * minusSeconds方法。
     * * @param time LocalTime类型参数
     *
     * @param seconds long类型参数
     * @return static LocalTime类型返回值
     */
    public static LocalTime minusSeconds(LocalTime time, long seconds) {
        return time.minusSeconds(seconds);
    }

    /**
     * plusNanos方法。
     * * @param time LocalTime类型参数
     *
     * @param nanos long类型参数
     * @return static LocalTime类型返回值
     */
    public static LocalTime plusNanos(LocalTime time, long nanos) {
        return time.plusNanos(nanos);
    }

    /**
     * minusNanos方法。
     * * @param time LocalTime类型参数
     *
     * @param nanos long类型参数
     * @return static LocalTime类型返回值
     */
    public static LocalTime minusNanos(LocalTime time, long nanos) {
        return time.minusNanos(nanos);
    }

    // ==================== 时间比较 ====================

    /**
     * isBefore方法。
     * * @param time1 LocalTime类型参数
     *
     * @param time2 LocalTime类型参数
     * @return static boolean类型返回值
     */
    public static boolean isBefore(LocalTime time1, LocalTime time2) {
        return time1.isBefore(time2);
    }

    /**
     * isAfter方法。
     * * @param time1 LocalTime类型参数
     *
     * @param time2 LocalTime类型参数
     * @return static boolean类型返回值
     */
    public static boolean isAfter(LocalTime time1, LocalTime time2) {
        return time1.isAfter(time2);
    }

    /**
     * isEqual方法。
     * * @param time1 LocalTime类型参数
     *
     * @param time2 LocalTime类型参数
     * @return static boolean类型返回值
     */
    public static boolean isEqual(LocalTime time1, LocalTime time2) {
        return time1.equals(time2);
    }

    // ==================== 时间差 ====================

    /**
     * hoursBetween方法。
     * * @param start LocalTime类型参数
     *
     * @param end LocalTime类型参数
     * @return static long类型返回值
     */
    public static long hoursBetween(LocalTime start, LocalTime end) {
        return java.time.temporal.ChronoUnit.HOURS.between(start, end);
    }

    /**
     * minutesBetween方法。
     * * @param start LocalTime类型参数
     *
     * @param end LocalTime类型参数
     * @return static long类型返回值
     */
    public static long minutesBetween(LocalTime start, LocalTime end) {
        return java.time.temporal.ChronoUnit.MINUTES.between(start, end);
    }

    /**
     * secondsBetween方法。
     * * @param start LocalTime类型参数
     *
     * @param end LocalTime类型参数
     * @return static long类型返回值
     */
    public static long secondsBetween(LocalTime start, LocalTime end) {
        return java.time.temporal.ChronoUnit.SECONDS.between(start, end);
    }

    /**
     * millisBetween方法。
     * * @param start LocalTime类型参数
     *
     * @param end LocalTime类型参数
     * @return static long类型返回值
     */
    public static long millisBetween(LocalTime start, LocalTime end) {
        return java.time.temporal.ChronoUnit.MILLIS.between(start, end);
    }

    /**
     * nanosBetween方法。
     * * @param start LocalTime类型参数
     *
     * @param end LocalTime类型参数
     * @return static long类型返回值
     */
    public static long nanosBetween(LocalTime start, LocalTime end) {
        return java.time.temporal.ChronoUnit.NANOS.between(start, end);
    }

    // ==================== 获取时间部分 ====================

    /**
     * getHour方法。
     * * @param time LocalTime类型参数
     *
     * @return static int类型返回值
     */
    public static int getHour(LocalTime time) {
        return time.getHour();
    }

    /**
     * getMinute方法。
     * * @param time LocalTime类型参数
     *
     * @return static int类型返回值
     */
    public static int getMinute(LocalTime time) {
        return time.getMinute();
    }

    /**
     * getSecond方法。
     * * @param time LocalTime类型参数
     *
     * @return static int类型返回值
     */
    public static int getSecond(LocalTime time) {
        return time.getSecond();
    }

    /**
     * getNano方法。
     * * @param time LocalTime类型参数
     *
     * @return static int类型返回值
     */
    public static int getNano(LocalTime time) {
        return time.getNano();
    }

    /**
     * toSecondOfDay方法。
     * * @param time LocalTime类型参数
     *
     * @return static long类型返回值
     */
    public static long toSecondOfDay(LocalTime time) {
        return time.toSecondOfDay();
    }

    /**
     * toNanoOfDay方法。
     * * @param time LocalTime类型参数
     *
     * @return static long类型返回值
     */
    public static long toNanoOfDay(LocalTime time) {
        return time.toNanoOfDay();
    }

    // ==================== 判断 ====================

    /**
     * isMidnight方法。
     * * @param time LocalTime类型参数
     *
     * @return static boolean类型返回值
     */
    public static boolean isMidnight(LocalTime time) {
        return time.equals(LocalTime.MIDNIGHT);
    }

    /**
     * isNoon方法。
     * * @param time LocalTime类型参数
     *
     * @return static boolean类型返回值
     */
    public static boolean isNoon(LocalTime time) {
        return time.equals(LocalTime.NOON);
    }

    // ==================== 转换到其他类型 ====================

    /**
     * toDate方法。
     * * @param time LocalTime类型参数
     *
     * @return static Date类型返回值
     */
    public static Date toDate(LocalTime time) {
        if (time == null) return null;
        return TimeConverter.toDate(LocalDate.now().atTime(time));
    }

    /**
     * toInstant方法。
     * * @param time LocalTime类型参数
     *
     * @return static Instant类型返回值
     */
    public static Instant toInstant(LocalTime time) {
        return TimeConverter.toInstant(time);
    }

    /**
     * toLocalDateTime方法。
     * * @param time LocalTime类型参数
     *
     * @return static LocalDateTime类型返回值
     */
    public static LocalDateTime toLocalDateTime(LocalTime time) {
        return TimeConverter.toLocalDateTime(time);
    }

    /**
     * toLocalDateTime方法。
     * * @param date LocalDate类型参数
     *
     * @param time LocalTime类型参数
     * @return static LocalDateTime类型返回值
     */
    public static LocalDateTime toLocalDateTime(LocalDate date, LocalTime time) {
        return TimeConverter.toLocalDateTime(date, time);
    }

    // ==================== 从其他类型转换 ====================

    /**
     * fromDate方法。
     * * @param date Date类型参数
     *
     * @return static LocalTime类型返回值
     */
    public static LocalTime fromDate(Date date) {
        return TimeConverter.toLocalTime(date);
    }

    /**
     * fromInstant方法。
     * * @param instant Instant类型参数
     *
     * @return static LocalTime类型返回值
     */
    public static LocalTime fromInstant(Instant instant) {
        return TimeConverter.toLocalTime(instant);
    }

    /**
     * fromLocalDateTime方法。
     * * @param localDateTime LocalDateTime类型参数
     *
     * @return static LocalTime类型返回值
     */
    public static LocalTime fromLocalDateTime(LocalDateTime localDateTime) {
        return TimeConverter.toLocalTime(localDateTime);
    }

    /**
     * fromSecondOfDay方法。
     * * @param secondOfDay long类型参数
     *
     * @return static LocalTime类型返回值
     */
    public static LocalTime fromSecondOfDay(long secondOfDay) {
        return LocalTime.ofSecondOfDay(secondOfDay);
    }
}