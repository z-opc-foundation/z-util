package com.zifang.util.core.time.converter;

import com.zifang.util.core.time.DurationUtil;

import java.time.*;
import java.util.Date;

/**
 * 时间类型转换器
 * <p>
 * 提供所有Java时间类型之间的互相转换能力
 */
public class TimeConverter {

    private TimeConverter() {
    }

    // ==================== Date <-> LocalDateTime ====================

    /**
     * toLocalDateTime方法。
     * * @param date Date类型参数
     *
     * @return static LocalDateTime类型返回值
     */
    public static LocalDateTime toLocalDateTime(Date date) {
        if (date == null) return null;
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * toLocalDateTime方法。
     * * @param date Date类型参数
     *
     * @param zoneId ZoneId类型参数
     * @return static LocalDateTime类型返回值
     */
    public static LocalDateTime toLocalDateTime(Date date, ZoneId zoneId) {
        if (date == null) return null;
        return date.toInstant().atZone(zoneId).toLocalDateTime();
    }

    /**
     * toDate方法。
     * * @param localDateTime LocalDateTime类型参数
     *
     * @return static Date类型返回值
     */
    public static Date toDate(LocalDateTime localDateTime) {
        if (localDateTime == null) return null;
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * toDate方法。
     * * @param localDateTime LocalDateTime类型参数
     *
     * @param zoneId ZoneId类型参数
     * @return static Date类型返回值
     */
    public static Date toDate(LocalDateTime localDateTime, ZoneId zoneId) {
        if (localDateTime == null) return null;
        return Date.from(localDateTime.atZone(zoneId).toInstant());
    }

    // ==================== Date <-> LocalDate ====================

    /**
     * toLocalDate方法。
     * * @param date Date类型参数
     *
     * @return static LocalDate类型返回值
     */
    public static LocalDate toLocalDate(Date date) {
        if (date == null) return null;
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    /**
     * toLocalDate方法。
     * * @param date Date类型参数
     *
     * @param zoneId ZoneId类型参数
     * @return static LocalDate类型返回值
     */
    public static LocalDate toLocalDate(Date date, ZoneId zoneId) {
        if (date == null) return null;
        return date.toInstant().atZone(zoneId).toLocalDate();
    }

    /**
     * toDate方法。
     * * @param localDate LocalDate类型参数
     *
     * @return static Date类型返回值
     */
    public static Date toDate(LocalDate localDate) {
        if (localDate == null) return null;
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /**
     * toDate方法。
     * * @param localDate LocalDate类型参数
     *
     * @param zoneId ZoneId类型参数
     * @return static Date类型返回值
     */
    public static Date toDate(LocalDate localDate, ZoneId zoneId) {
        if (localDate == null) return null;
        return Date.from(localDate.atStartOfDay(zoneId).toInstant());
    }

    // ==================== Date <-> LocalTime ====================

    /**
     * toLocalTime方法。
     * * @param date Date类型参数
     *
     * @return static LocalTime类型返回值
     */
    public static LocalTime toLocalTime(Date date) {
        if (date == null) return null;
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
    }

    /**
     * toLocalTime方法。
     * * @param date Date类型参数
     *
     * @param zoneId ZoneId类型参数
     * @return static LocalTime类型返回值
     */
    public static LocalTime toLocalTime(Date date, ZoneId zoneId) {
        if (date == null) return null;
        return date.toInstant().atZone(zoneId).toLocalTime();
    }

    // ==================== Date <-> Instant ====================

    /**
     * toInstant方法。
     * * @param date Date类型参数
     *
     * @return static Instant类型返回值
     */
    public static Instant toInstant(Date date) {
        if (date == null) return null;
        return date.toInstant();
    }

    /**
     * toDate方法。
     * * @param instant Instant类型参数
     *
     * @return static Date类型返回值
     */
    public static Date toDate(Instant instant) {
        if (instant == null) return null;
        return Date.from(instant);
    }

    // ==================== Date <-> ZonedDateTime ====================

    /**
     * toZonedDateTime方法。
     * * @param date Date类型参数
     *
     * @return static ZonedDateTime类型返回值
     */
    public static ZonedDateTime toZonedDateTime(Date date) {
        if (date == null) return null;
        return date.toInstant().atZone(ZoneId.systemDefault());
    }

    /**
     * toZonedDateTime方法。
     * * @param date Date类型参数
     *
     * @param zoneId ZoneId类型参数
     * @return static ZonedDateTime类型返回值
     */
    public static ZonedDateTime toZonedDateTime(Date date, ZoneId zoneId) {
        if (date == null) return null;
        return date.toInstant().atZone(zoneId);
    }

    /**
     * toDate方法。
     * * @param zonedDateTime ZonedDateTime类型参数
     *
     * @return static Date类型返回值
     */
    public static Date toDate(ZonedDateTime zonedDateTime) {
        if (zonedDateTime == null) return null;
        return Date.from(zonedDateTime.toInstant());
    }

    // ==================== LocalDateTime <-> LocalDate ====================

    /**
     * toLocalDate方法。
     * * @param localDateTime LocalDateTime类型参数
     *
     * @return static LocalDate类型返回值
     */
    public static LocalDate toLocalDate(LocalDateTime localDateTime) {
        if (localDateTime == null) return null;
        return localDateTime.toLocalDate();
    }

    /**
     * toLocalDateTime方法。
     * * @param localDate LocalDate类型参数
     *
     * @return static LocalDateTime类型返回值
     */
    public static LocalDateTime toLocalDateTime(LocalDate localDate) {
        if (localDate == null) return null;
        return localDate.atStartOfDay();
    }

    /**
     * toLocalDateTime方法。
     * * @param localDate LocalDate类型参数
     *
     * @param localTime LocalTime类型参数
     * @return static LocalDateTime类型返回值
     */
    public static LocalDateTime toLocalDateTime(LocalDate localDate, LocalTime localTime) {
        if (localDate == null) return null;
        return localDate.atTime(localTime != null ? localTime : LocalTime.MIDNIGHT);
    }

    // ==================== LocalDateTime <-> LocalTime ====================

    /**
     * toLocalTime方法。
     * * @param localDateTime LocalDateTime类型参数
     *
     * @return static LocalTime类型返回值
     */
    public static LocalTime toLocalTime(LocalDateTime localDateTime) {
        if (localDateTime == null) return null;
        return localDateTime.toLocalTime();
    }

    /**
     * toLocalDateTime方法。
     * * @param localTime LocalTime类型参数
     *
     * @return static LocalDateTime类型返回值
     */
    public static LocalDateTime toLocalDateTime(LocalTime localTime) {
        if (localTime == null) return null;
        return LocalDate.now().atTime(localTime);
    }

    // ==================== LocalDateTime <-> Instant ====================

    /**
     * toInstant方法。
     * * @param localDateTime LocalDateTime类型参数
     *
     * @return static Instant类型返回值
     */
    public static Instant toInstant(LocalDateTime localDateTime) {
        if (localDateTime == null) return null;
        return localDateTime.atZone(ZoneId.systemDefault()).toInstant();
    }

    /**
     * toInstant方法。
     * * @param localDateTime LocalDateTime类型参数
     *
     * @param zoneId ZoneId类型参数
     * @return static Instant类型返回值
     */
    public static Instant toInstant(LocalDateTime localDateTime, ZoneId zoneId) {
        if (localDateTime == null) return null;
        return localDateTime.atZone(zoneId).toInstant();
    }

    /**
     * toLocalDateTime方法。
     * * @param instant Instant类型参数
     *
     * @return static LocalDateTime类型返回值
     */
    public static LocalDateTime toLocalDateTime(Instant instant) {
        if (instant == null) return null;
        return instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * toLocalDateTime方法。
     * * @param instant Instant类型参数
     *
     * @param zoneId ZoneId类型参数
     * @return static LocalDateTime类型返回值
     */
    public static LocalDateTime toLocalDateTime(Instant instant, ZoneId zoneId) {
        if (instant == null) return null;
        return instant.atZone(zoneId).toLocalDateTime();
    }

    // ==================== LocalDateTime <-> ZonedDateTime ====================

    /**
     * toZonedDateTime方法。
     * * @param localDateTime LocalDateTime类型参数
     *
     * @return static ZonedDateTime类型返回值
     */
    public static ZonedDateTime toZonedDateTime(LocalDateTime localDateTime) {
        if (localDateTime == null) return null;
        return localDateTime.atZone(ZoneId.systemDefault());
    }

    /**
     * toZonedDateTime方法。
     * * @param localDateTime LocalDateTime类型参数
     *
     * @param zoneId ZoneId类型参数
     * @return static ZonedDateTime类型返回值
     */
    public static ZonedDateTime toZonedDateTime(LocalDateTime localDateTime, ZoneId zoneId) {
        if (localDateTime == null) return null;
        return localDateTime.atZone(zoneId);
    }

    /**
     * toLocalDateTime方法。
     * * @param zonedDateTime ZonedDateTime类型参数
     *
     * @return static LocalDateTime类型返回值
     */
    public static LocalDateTime toLocalDateTime(ZonedDateTime zonedDateTime) {
        if (zonedDateTime == null) return null;
        return zonedDateTime.toLocalDateTime();
    }

    // ==================== LocalDate <-> Instant ====================

    /**
     * toInstant方法。
     * * @param localDate LocalDate类型参数
     *
     * @return static Instant类型返回值
     */
    public static Instant toInstant(LocalDate localDate) {
        if (localDate == null) return null;
        return localDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
    }

    /**
     * toInstant方法。
     * * @param localDate LocalDate类型参数
     *
     * @param zoneId ZoneId类型参数
     * @return static Instant类型返回值
     */
    public static Instant toInstant(LocalDate localDate, ZoneId zoneId) {
        if (localDate == null) return null;
        return localDate.atStartOfDay(zoneId).toInstant();
    }

    /**
     * toLocalDate方法。
     * * @param instant Instant类型参数
     *
     * @return static LocalDate类型返回值
     */
    public static LocalDate toLocalDate(Instant instant) {
        if (instant == null) return null;
        return instant.atZone(ZoneId.systemDefault()).toLocalDate();
    }

    /**
     * toLocalDate方法。
     * * @param instant Instant类型参数
     *
     * @param zoneId ZoneId类型参数
     * @return static LocalDate类型返回值
     */
    public static LocalDate toLocalDate(Instant instant, ZoneId zoneId) {
        if (instant == null) return null;
        return instant.atZone(zoneId).toLocalDate();
    }

    // ==================== LocalTime <-> Instant ====================

    /**
     * toInstant方法。
     * * @param localTime LocalTime类型参数
     *
     * @return static Instant类型返回值
     */
    public static Instant toInstant(LocalTime localTime) {
        if (localTime == null) return null;
        return LocalDate.now().atTime(localTime).atZone(ZoneId.systemDefault()).toInstant();
    }

    /**
     * toInstant方法。
     * * @param localTime LocalTime类型参数
     *
     * @param zoneId ZoneId类型参数
     * @return static Instant类型返回值
     */
    public static Instant toInstant(LocalTime localTime, ZoneId zoneId) {
        if (localTime == null) return null;
        return LocalDate.now().atTime(localTime).atZone(zoneId).toInstant();
    }

    /**
     * toLocalTime方法。
     * * @param instant Instant类型参数
     *
     * @return static LocalTime类型返回值
     */
    public static LocalTime toLocalTime(Instant instant) {
        if (instant == null) return null;
        return instant.atZone(ZoneId.systemDefault()).toLocalTime();
    }

    /**
     * toLocalTime方法。
     * * @param instant Instant类型参数
     *
     * @param zoneId ZoneId类型参数
     * @return static LocalTime类型返回值
     */
    public static LocalTime toLocalTime(Instant instant, ZoneId zoneId) {
        if (instant == null) return null;
        return instant.atZone(zoneId).toLocalTime();
    }

    // ==================== Epoch Milliseconds ====================

    /**
     * toEpochMilli方法。
     * * @param date Date类型参数
     *
     * @return static long类型返回值
     */
    public static long toEpochMilli(Date date) {
        if (date == null) return 0;
        return date.getTime();
    }

    /**
     * toDateFromEpochMilli方法。
     * * @param epochMilli long类型参数
     *
     * @return static Date类型返回值
     */
    public static Date toDateFromEpochMilli(long epochMilli) {
        return new Date(epochMilli);
    }

    /**
     * toEpochMilli方法。
     * * @param localDateTime LocalDateTime类型参数
     *
     * @return static long类型返回值
     */
    public static long toEpochMilli(LocalDateTime localDateTime) {
        if (localDateTime == null) return 0;
        return localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    /**
     * fromEpochMilli方法。
     * * @param epochMilli long类型参数
     *
     * @return static LocalDateTime类型返回值
     */
    public static LocalDateTime fromEpochMilli(long epochMilli) {
        return Instant.ofEpochMilli(epochMilli).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * toEpochSecond方法。
     * * @param localDateTime LocalDateTime类型参数
     *
     * @return static long类型返回值
     */
    public static long toEpochSecond(LocalDateTime localDateTime) {
        if (localDateTime == null) return 0;
        return localDateTime.atZone(ZoneId.systemDefault()).toInstant().getEpochSecond();
    }

    /**
     * fromEpochSecond方法。
     * * @param epochSecond long类型参数
     *
     * @return static LocalDateTime类型返回值
     */
    public static LocalDateTime fromEpochSecond(long epochSecond) {
        return Instant.ofEpochSecond(epochSecond).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    // ==================== Duration <-> Long ====================

    /**
     * toMillis方法。
     * * @param duration Duration类型参数
     *
     * @return static long类型返回值
     */
    public static long toMillis(Duration duration) {
        return DurationUtil.toMillis(duration);
    }

    /**
     * toDuration方法。
     * * @param millis long类型参数
     *
     * @return static Duration类型返回值
     */
    public static Duration toDuration(long millis) {
        return Duration.ofMillis(millis);
    }

    /**
     * toSeconds方法。
     * * @param duration Duration类型参数
     *
     * @return static long类型返回值
     */
    public static long toSeconds(Duration duration) {
        return DurationUtil.toSeconds(duration);
    }

    /**
     * toDurationSeconds方法。
     * * @param seconds long类型参数
     *
     * @return static Duration类型返回值
     */
    public static Duration toDurationSeconds(long seconds) {
        return Duration.ofSeconds(seconds);
    }
}