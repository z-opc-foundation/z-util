package com.zifang.util.core.time;

import com.zifang.util.core.time.converter.TimeConverter;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;

/**
 * java.time.ZonedDateTime 工具类
 * <p>
 * 只处理ZonedDateTime类型，提供ZonedDateTime的格式化、解析、计算、转换等功能
 */
public class ZonedDateTimeUtil {

    public static final String PATTERN_DEFAULT = "yyyy-MM-dd HH:mm:ss";
    public static final String PATTERN_DATE = "yyyy-MM-dd";
    public static final String PATTERN_TIME = "HH:mm:ss";
    public static final String PATTERN_DATETIME_MS = "yyyy-MM-dd HH:mm:ss.SSS";
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

    // ==================== 格式化 ====================

    /**
     * format方法。
     * * @param dateTime ZonedDateTime类型参数
     *
     * @return static String类型返回值
     */
    public static String format(ZonedDateTime dateTime) {
        return format(dateTime, FMT_DEFAULT);
    }

    /**
     * format方法。
     * * @param dateTime ZonedDateTime类型参数
     *
     * @param pattern String类型参数
     * @return static String类型返回值
     */
    public static String format(ZonedDateTime dateTime, String pattern) {
        if (dateTime == null) return null;
        return dateTime.format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * format方法。
     * * @param dateTime ZonedDateTime类型参数
     *
     * @param formatter DateTimeFormatter类型参数
     * @return static String类型返回值
     */
    public static String format(ZonedDateTime dateTime, DateTimeFormatter formatter) {
        if (dateTime == null) return null;
        return dateTime.format(formatter);
    }

    /**
     * format方法。
     * * @param dateTime ZonedDateTime类型参数
     *
     * @param zoneId  ZoneId类型参数
     * @param pattern String类型参数
     * @return static String类型返回值
     */
    public static String format(ZonedDateTime dateTime, ZoneId zoneId, String pattern) {
        if (dateTime == null) return null;
        return dateTime.withZoneSameInstant(zoneId).format(DateTimeFormatter.ofPattern(pattern));
    }

    // ==================== 解析 ====================

    /**
     * parse方法。
     * * @param dateTimeStr String类型参数
     *
     * @return static ZonedDateTime类型返回值
     */
    public static ZonedDateTime parse(String dateTimeStr) {
        return parse(dateTimeStr, PATTERN_DEFAULT);
    }

    /**
     * parse方法。
     * * @param dateTimeStr String类型参数
     *
     * @param pattern String类型参数
     * @return static ZonedDateTime类型返回值
     */
    public static ZonedDateTime parse(String dateTimeStr, String pattern) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) return null;
        return ZonedDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern(pattern).withZone(ZoneId.systemDefault()));
    }

    /**
     * parse方法。
     * * @param dateTimeStr String类型参数
     *
     * @param pattern String类型参数
     * @param zoneId  ZoneId类型参数
     * @return static ZonedDateTime类型返回值
     */
    public static ZonedDateTime parse(String dateTimeStr, String pattern, ZoneId zoneId) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) return null;
        return ZonedDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern(pattern).withZone(zoneId));
    }

    // ==================== 获取 ====================

    /**
     * now方法。
     *
     * @return static ZonedDateTime类型返回值
     */
    public static ZonedDateTime now() {
        return ZonedDateTime.now();
    }

    /**
     * now方法。
     * * @param zoneId ZoneId类型参数
     *
     * @return static ZonedDateTime类型返回值
     */
    public static ZonedDateTime now(ZoneId zoneId) {
        return ZonedDateTime.now(zoneId);
    }

    /**
     * of方法。
     * * @param localDateTime LocalDateTime类型参数
     *
     * @param zoneId ZoneId类型参数
     * @return static ZonedDateTime类型返回值
     */
    public static ZonedDateTime of(LocalDateTime localDateTime, ZoneId zoneId) {
        return localDateTime.atZone(zoneId);
    }

    /**
     * of方法。
     * * @param year int类型参数
     *
     * @param month      int类型参数
     * @param dayOfMonth int类型参数
     * @param hour       int类型参数
     * @param minute     int类型参数
     * @param second     int类型参数
     * @param zoneId     ZoneId类型参数
     * @return static ZonedDateTime类型返回值
     */
    public static ZonedDateTime of(int year, int month, int dayOfMonth, int hour, int minute, int second, ZoneId zoneId) {
        return ZonedDateTime.of(year, month, dayOfMonth, hour, minute, second, 0, zoneId);
    }

    /**
     * ofInstant方法。
     * * @param instant Instant类型参数
     *
     * @param zoneId ZoneId类型参数
     * @return static ZonedDateTime类型返回值
     */
    public static ZonedDateTime ofInstant(Instant instant, ZoneId zoneId) {
        return ZonedDateTime.ofInstant(instant, zoneId);
    }

    // ==================== 日期时间计算 ====================

    /**
     * plusYears方法。
     * * @param dateTime ZonedDateTime类型参数
     *
     * @param years long类型参数
     * @return static ZonedDateTime类型返回值
     */
    public static ZonedDateTime plusYears(ZonedDateTime dateTime, long years) {
        return dateTime.plusYears(years);
    }

    /**
     * minusYears方法。
     * * @param dateTime ZonedDateTime类型参数
     *
     * @param years long类型参数
     * @return static ZonedDateTime类型返回值
     */
    public static ZonedDateTime minusYears(ZonedDateTime dateTime, long years) {
        return dateTime.minusYears(years);
    }

    /**
     * plusMonths方法。
     * * @param dateTime ZonedDateTime类型参数
     *
     * @param months long类型参数
     * @return static ZonedDateTime类型返回值
     */
    public static ZonedDateTime plusMonths(ZonedDateTime dateTime, long months) {
        return dateTime.plusMonths(months);
    }

    /**
     * minusMonths方法。
     * * @param dateTime ZonedDateTime类型参数
     *
     * @param months long类型参数
     * @return static ZonedDateTime类型返回值
     */
    public static ZonedDateTime minusMonths(ZonedDateTime dateTime, long months) {
        return dateTime.minusMonths(months);
    }

    /**
     * plusDays方法。
     * * @param dateTime ZonedDateTime类型参数
     *
     * @param days long类型参数
     * @return static ZonedDateTime类型返回值
     */
    public static ZonedDateTime plusDays(ZonedDateTime dateTime, long days) {
        return dateTime.plusDays(days);
    }

    /**
     * minusDays方法。
     * * @param dateTime ZonedDateTime类型参数
     *
     * @param days long类型参数
     * @return static ZonedDateTime类型返回值
     */
    public static ZonedDateTime minusDays(ZonedDateTime dateTime, long days) {
        return dateTime.minusDays(days);
    }

    /**
     * plusHours方法。
     * * @param dateTime ZonedDateTime类型参数
     *
     * @param hours long类型参数
     * @return static ZonedDateTime类型返回值
     */
    public static ZonedDateTime plusHours(ZonedDateTime dateTime, long hours) {
        return dateTime.plusHours(hours);
    }

    /**
     * minusHours方法。
     * * @param dateTime ZonedDateTime类型参数
     *
     * @param hours long类型参数
     * @return static ZonedDateTime类型返回值
     */
    public static ZonedDateTime minusHours(ZonedDateTime dateTime, long hours) {
        return dateTime.minusHours(hours);
    }

    /**
     * plusMinutes方法。
     * * @param dateTime ZonedDateTime类型参数
     *
     * @param minutes long类型参数
     * @return static ZonedDateTime类型返回值
     */
    public static ZonedDateTime plusMinutes(ZonedDateTime dateTime, long minutes) {
        return dateTime.plusMinutes(minutes);
    }

    /**
     * minusMinutes方法。
     * * @param dateTime ZonedDateTime类型参数
     *
     * @param minutes long类型参数
     * @return static ZonedDateTime类型返回值
     */
    public static ZonedDateTime minusMinutes(ZonedDateTime dateTime, long minutes) {
        return dateTime.minusMinutes(minutes);
    }

    /**
     * plusSeconds方法。
     * * @param dateTime ZonedDateTime类型参数
     *
     * @param seconds long类型参数
     * @return static ZonedDateTime类型返回值
     */
    public static ZonedDateTime plusSeconds(ZonedDateTime dateTime, long seconds) {
        return dateTime.plusSeconds(seconds);
    }

    /**
     * minusSeconds方法。
     * * @param dateTime ZonedDateTime类型参数
     *
     * @param seconds long类型参数
     * @return static ZonedDateTime类型返回值
     */
    public static ZonedDateTime minusSeconds(ZonedDateTime dateTime, long seconds) {
        return dateTime.minusSeconds(seconds);
    }

    // ==================== 日期调整 ====================

    /**
     * firstDayOfMonth方法。
     * * @param dateTime ZonedDateTime类型参数
     *
     * @return static ZonedDateTime类型返回值
     */
    public static ZonedDateTime firstDayOfMonth(ZonedDateTime dateTime) {
        return dateTime.with(TemporalAdjusters.firstDayOfMonth());
    }

    /**
     * lastDayOfMonth方法。
     * * @param dateTime ZonedDateTime类型参数
     *
     * @return static ZonedDateTime类型返回值
     */
    public static ZonedDateTime lastDayOfMonth(ZonedDateTime dateTime) {
        return dateTime.with(TemporalAdjusters.lastDayOfMonth());
    }

    /**
     * firstDayOfYear方法。
     * * @param dateTime ZonedDateTime类型参数
     *
     * @return static ZonedDateTime类型返回值
     */
    public static ZonedDateTime firstDayOfYear(ZonedDateTime dateTime) {
        return dateTime.with(TemporalAdjusters.firstDayOfYear());
    }

    /**
     * lastDayOfYear方法。
     * * @param dateTime ZonedDateTime类型参数
     *
     * @return static ZonedDateTime类型返回值
     */
    public static ZonedDateTime lastDayOfYear(ZonedDateTime dateTime) {
        return dateTime.with(TemporalAdjusters.lastDayOfYear());
    }

    /**
     * withHour方法。
     * * @param dateTime ZonedDateTime类型参数
     *
     * @param hour int类型参数
     * @return static ZonedDateTime类型返回值
     */
    public static ZonedDateTime withHour(ZonedDateTime dateTime, int hour) {
        return dateTime.withHour(hour);
    }

    /**
     * withMinute方法。
     * * @param dateTime ZonedDateTime类型参数
     *
     * @param minute int类型参数
     * @return static ZonedDateTime类型返回值
     */
    public static ZonedDateTime withMinute(ZonedDateTime dateTime, int minute) {
        return dateTime.withMinute(minute);
    }

    /**
     * withSecond方法。
     * * @param dateTime ZonedDateTime类型参数
     *
     * @param second int类型参数
     * @return static ZonedDateTime类型返回值
     */
    public static ZonedDateTime withSecond(ZonedDateTime dateTime, int second) {
        return dateTime.withSecond(second);
    }

    /**
     * withZoneSameInstant方法。
     * * @param dateTime ZonedDateTime类型参数
     *
     * @param zoneId ZoneId类型参数
     * @return static ZonedDateTime类型返回值
     */
    public static ZonedDateTime withZoneSameInstant(ZonedDateTime dateTime, ZoneId zoneId) {
        return dateTime.withZoneSameInstant(zoneId);
    }

    /**
     * withZoneSameLocal方法。
     * * @param dateTime ZonedDateTime类型参数
     *
     * @param zoneId ZoneId类型参数
     * @return static ZonedDateTime类型返回值
     */
    public static ZonedDateTime withZoneSameLocal(ZonedDateTime dateTime, ZoneId zoneId) {
        return dateTime.withZoneSameLocal(zoneId);
    }

    // ==================== 日期时间比较 ====================

    /**
     * isBefore方法。
     * * @param dateTime1 ZonedDateTime类型参数
     *
     * @param dateTime2 ZonedDateTime类型参数
     * @return static boolean类型返回值
     */
    public static boolean isBefore(ZonedDateTime dateTime1, ZonedDateTime dateTime2) {
        return dateTime1.isBefore(dateTime2);
    }

    /**
     * isAfter方法。
     * * @param dateTime1 ZonedDateTime类型参数
     *
     * @param dateTime2 ZonedDateTime类型参数
     * @return static boolean类型返回值
     */
    public static boolean isAfter(ZonedDateTime dateTime1, ZonedDateTime dateTime2) {
        return dateTime1.isAfter(dateTime2);
    }

    /**
     * isEqual方法。
     * * @param dateTime1 ZonedDateTime类型参数
     *
     * @param dateTime2 ZonedDateTime类型参数
     * @return static boolean类型返回值
     */
    public static boolean isEqual(ZonedDateTime dateTime1, ZonedDateTime dateTime2) {
        return dateTime1.isEqual(dateTime2);
    }

    /**
     * isSameDay方法。
     * * @param dateTime1 ZonedDateTime类型参数
     *
     * @param dateTime2 ZonedDateTime类型参数
     * @return static boolean类型返回值
     */
    public static boolean isSameDay(ZonedDateTime dateTime1, ZonedDateTime dateTime2) {
        return dateTime1.toLocalDate().isEqual(dateTime2.toLocalDate());
    }

    // ==================== 日期时间差 ====================

    /**
     * daysBetween方法。
     * * @param start ZonedDateTime类型参数
     *
     * @param end ZonedDateTime类型参数
     * @return static long类型返回值
     */
    public static long daysBetween(ZonedDateTime start, ZonedDateTime end) {
        return java.time.temporal.ChronoUnit.DAYS.between(start, end);
    }

    /**
     * hoursBetween方法。
     * * @param start ZonedDateTime类型参数
     *
     * @param end ZonedDateTime类型参数
     * @return static long类型返回值
     */
    public static long hoursBetween(ZonedDateTime start, ZonedDateTime end) {
        return java.time.temporal.ChronoUnit.HOURS.between(start, end);
    }

    /**
     * minutesBetween方法。
     * * @param start ZonedDateTime类型参数
     *
     * @param end ZonedDateTime类型参数
     * @return static long类型返回值
     */
    public static long minutesBetween(ZonedDateTime start, ZonedDateTime end) {
        return java.time.temporal.ChronoUnit.MINUTES.between(start, end);
    }

    /**
     * secondsBetween方法。
     * * @param start ZonedDateTime类型参数
     *
     * @param end ZonedDateTime类型参数
     * @return static long类型返回值
     */
    public static long secondsBetween(ZonedDateTime start, ZonedDateTime end) {
        return java.time.temporal.ChronoUnit.SECONDS.between(start, end);
    }

    // ==================== 获取日期时间部分 ====================

    /**
     * getYear方法。
     * * @param dateTime ZonedDateTime类型参数
     *
     * @return static int类型返回值
     */
    public static int getYear(ZonedDateTime dateTime) {
        return dateTime.getYear();
    }

    /**
     * getMonth方法。
     * * @param dateTime ZonedDateTime类型参数
     *
     * @return static int类型返回值
     */
    public static int getMonth(ZonedDateTime dateTime) {
        return dateTime.getMonthValue();
    }

    /**
     * getDayOfMonth方法。
     * * @param dateTime ZonedDateTime类型参数
     *
     * @return static int类型返回值
     */
    public static int getDayOfMonth(ZonedDateTime dateTime) {
        return dateTime.getDayOfMonth();
    }

    /**
     * getHour方法。
     * * @param dateTime ZonedDateTime类型参数
     *
     * @return static int类型返回值
     */
    public static int getHour(ZonedDateTime dateTime) {
        return dateTime.getHour();
    }

    /**
     * getMinute方法。
     * * @param dateTime ZonedDateTime类型参数
     *
     * @return static int类型返回值
     */
    public static int getMinute(ZonedDateTime dateTime) {
        return dateTime.getMinute();
    }

    /**
     * getSecond方法。
     * * @param dateTime ZonedDateTime类型参数
     *
     * @return static int类型返回值
     */
    public static int getSecond(ZonedDateTime dateTime) {
        return dateTime.getSecond();
    }

    /**
     * getDayOfWeek方法。
     * * @param dateTime ZonedDateTime类型参数
     *
     * @return static DayOfWeek类型返回值
     */
    public static DayOfWeek getDayOfWeek(ZonedDateTime dateTime) {
        return dateTime.getDayOfWeek();
    }

    /**
     * getOffset方法。
     * * @param dateTime ZonedDateTime类型参数
     *
     * @return static ZoneOffset类型返回值
     */
    public static ZoneOffset getOffset(ZonedDateTime dateTime) {
        return dateTime.getOffset();
    }

    /**
     * getZone方法。
     * * @param dateTime ZonedDateTime类型参数
     *
     * @return static ZoneId类型返回值
     */
    public static ZoneId getZone(ZonedDateTime dateTime) {
        return dateTime.getZone();
    }

    // ==================== 转换到其他类型 ====================

    /**
     * toDate方法。
     * * @param dateTime ZonedDateTime类型参数
     *
     * @return static Date类型返回值
     */
    public static Date toDate(ZonedDateTime dateTime) {
        return TimeConverter.toDate(dateTime);
    }

    /**
     * toLocalDateTime方法。
     * * @param dateTime ZonedDateTime类型参数
     *
     * @return static LocalDateTime类型返回值
     */
    public static LocalDateTime toLocalDateTime(ZonedDateTime dateTime) {
        return dateTime.toLocalDateTime();
    }

    /**
     * toLocalDate方法。
     * * @param dateTime ZonedDateTime类型参数
     *
     * @return static LocalDate类型返回值
     */
    public static LocalDate toLocalDate(ZonedDateTime dateTime) {
        return dateTime.toLocalDate();
    }

    /**
     * toLocalTime方法。
     * * @param dateTime ZonedDateTime类型参数
     *
     * @return static LocalTime类型返回值
     */
    public static LocalTime toLocalTime(ZonedDateTime dateTime) {
        return dateTime.toLocalTime();
    }

    /**
     * toInstant方法。
     * * @param dateTime ZonedDateTime类型参数
     *
     * @return static Instant类型返回值
     */
    public static Instant toInstant(ZonedDateTime dateTime) {
        return dateTime.toInstant();
    }

    /**
     * toEpochMilli方法。
     * * @param dateTime ZonedDateTime类型参数
     *
     * @return static long类型返回值
     */
    public static long toEpochMilli(ZonedDateTime dateTime) {
        return dateTime.toInstant().toEpochMilli();
    }

    /**
     * toZonedDateTime方法。
     * * @param dateTime ZonedDateTime类型参数
     *
     * @param zoneId ZoneId类型参数
     * @return static ZonedDateTime类型返回值
     */
    public static ZonedDateTime toZonedDateTime(ZonedDateTime dateTime, ZoneId zoneId) {
        return dateTime.withZoneSameInstant(zoneId);
    }

    // ==================== 从其他类型转换 ====================

    /**
     * fromDate方法。
     * * @param date Date类型参数
     *
     * @return static ZonedDateTime类型返回值
     */
    public static ZonedDateTime fromDate(Date date) {
        return TimeConverter.toZonedDateTime(date);
    }

    /**
     * fromDate方法。
     * * @param date Date类型参数
     *
     * @param zoneId ZoneId类型参数
     * @return static ZonedDateTime类型返回值
     */
    public static ZonedDateTime fromDate(Date date, ZoneId zoneId) {
        return TimeConverter.toZonedDateTime(date, zoneId);
    }

    /**
     * fromLocalDateTime方法。
     * * @param localDateTime LocalDateTime类型参数
     *
     * @return static ZonedDateTime类型返回值
     */
    public static ZonedDateTime fromLocalDateTime(LocalDateTime localDateTime) {
        return TimeConverter.toZonedDateTime(localDateTime);
    }

    /**
     * fromLocalDateTime方法。
     * * @param localDateTime LocalDateTime类型参数
     *
     * @param zoneId ZoneId类型参数
     * @return static ZonedDateTime类型返回值
     */
    public static ZonedDateTime fromLocalDateTime(LocalDateTime localDateTime, ZoneId zoneId) {
        return TimeConverter.toZonedDateTime(localDateTime, zoneId);
    }

    /**
     * fromInstant方法。
     * * @param instant Instant类型参数
     *
     * @return static ZonedDateTime类型返回值
     */
    public static ZonedDateTime fromInstant(Instant instant) {
        return instant.atZone(ZoneId.systemDefault());
    }

    /**
     * fromInstant方法。
     * * @param instant Instant类型参数
     *
     * @param zoneId ZoneId类型参数
     * @return static ZonedDateTime类型返回值
     */
    public static ZonedDateTime fromInstant(Instant instant, ZoneId zoneId) {
        return instant.atZone(zoneId);
    }

    /**
     * fromLocalDate方法。
     * * @param localDate LocalDate类型参数
     *
     * @return static ZonedDateTime类型返回值
     */
    public static ZonedDateTime fromLocalDate(LocalDate localDate) {
        return localDate.atStartOfDay(ZoneId.systemDefault());
    }

    /**
     * fromLocalDate方法。
     * * @param localDate LocalDate类型参数
     *
     * @param zoneId ZoneId类型参数
     * @return static ZonedDateTime类型返回值
     */
    public static ZonedDateTime fromLocalDate(LocalDate localDate, ZoneId zoneId) {
        return localDate.atStartOfDay(zoneId);
    }

    /**
     * fromEpochMilli方法。
     * * @param epochMilli long类型参数
     *
     * @return static ZonedDateTime类型返回值
     */
    public static ZonedDateTime fromEpochMilli(long epochMilli) {
        return Instant.ofEpochMilli(epochMilli).atZone(ZoneId.systemDefault());
    }

    /**
     * fromEpochMilli方法。
     * * @param epochMilli long类型参数
     *
     * @param zoneId ZoneId类型参数
     * @return static ZonedDateTime类型返回值
     */
    public static ZonedDateTime fromEpochMilli(long epochMilli, ZoneId zoneId) {
        return Instant.ofEpochMilli(epochMilli).atZone(zoneId);
    }
}
