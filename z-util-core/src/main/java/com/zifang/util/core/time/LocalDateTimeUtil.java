package com.zifang.util.core.time;

import com.zifang.util.core.time.converter.TimeConverter;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * java.time.LocalDateTime 工具类
 * <p>
 * 只处理LocalDateTime类型，提供LocalDateTime的格式化、解析、计算等功能
 */
public class LocalDateTimeUtil {

    public static final String PATTERN_DEFAULT = "yyyy-MM-dd HH:mm:ss";
    public static final String PATTERN_DATE = "yyyy-MM-dd";
    public static final String PATTERN_TIME = "HH:mm:ss";
    public static final String PATTERN_DATETIME_MS = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final String PATTERN_DATETIME_COMPACT = "yyyyMMddHHmmss";
    public static final String PATTERN_ISO = "yyyy-MM-dd'T'HH:mm:ss";
    public static final String PATTERN_ISO_MS = "yyyy-MM-dd'T'HH:mm:ss.SSS";

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
     * * @param dateTime LocalDateTime类型参数
     *
     * @return static String类型返回值
     */
    public static String format(LocalDateTime dateTime) {
        return format(dateTime, FMT_DEFAULT);
    }

    /**
     * format方法。
     * * @param dateTime LocalDateTime类型参数
     *
     * @param pattern String类型参数
     * @return static String类型返回值
     */
    public static String format(LocalDateTime dateTime, String pattern) {
        if (dateTime == null) return null;
        return dateTime.format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * format方法。
     * * @param dateTime LocalDateTime类型参数
     *
     * @param formatter DateTimeFormatter类型参数
     * @return static String类型返回值
     */
    public static String format(LocalDateTime dateTime, DateTimeFormatter formatter) {
        if (dateTime == null) return null;
        return dateTime.format(formatter);
    }

    // ==================== 解析 ====================

    /**
     * parse方法。
     * * @param dateTimeStr String类型参数
     *
     * @return static LocalDateTime类型返回值
     */
    public static LocalDateTime parse(String dateTimeStr) {
        return parse(dateTimeStr, PATTERN_DEFAULT);
    }

    /**
     * parse方法。
     * * @param dateTimeStr String类型参数
     *
     * @param pattern String类型参数
     * @return static LocalDateTime类型返回值
     */
    public static LocalDateTime parse(String dateTimeStr, String pattern) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) return null;
        return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * parseStrict方法。
     * * @param dateTimeStr String类型参数
     *
     * @param patterns String...类型参数
     * @return static LocalDateTime类型返回值
     */
    public static LocalDateTime parseStrict(String dateTimeStr, String... patterns) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) return null;
        for (String pattern : patterns) {
            try {
                return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern(pattern));
            } catch (Exception e) {
                // 尝试下一个模式
            }
        }
        return null;
    }

    // ==================== 获取 ====================

    /**
     * now方法。
     *
     * @return static LocalDateTime类型返回值
     */
    public static LocalDateTime now() {
        return LocalDateTime.now();
    }

    /**
     * todayStart方法。
     *
     * @return static LocalDateTime类型返回值
     */
    public static LocalDateTime todayStart() {
        return LocalDate.now().atStartOfDay();
    }

    /**
     * todayEnd方法。
     *
     * @return static LocalDateTime类型返回值
     */
    public static LocalDateTime todayEnd() {
        return LocalDate.now().atTime(LocalTime.MAX);
    }

    /**
     * dayStart方法。
     * 获取指定时间所在天的开始时间（00:00:00）
     *
     * @param dateTime LocalDateTime类型参数
     * @return static LocalDateTime类型返回值，dateTime为null时返回null
     */
    public static LocalDateTime dayStart(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.toLocalDate().atStartOfDay();
    }

    /**
     * dayEnd方法。
     * 获取指定时间所在天的结束时间（23:59:59.999999999）
     *
     * @param dateTime LocalDateTime类型参数
     * @return static LocalDateTime类型返回值，dateTime为null时返回null
     */
    public static LocalDateTime dayEnd(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.toLocalDate().atTime(LocalTime.MAX);
    }

    /**
     * minuteStart方法。
     * 获取指定时间所在分钟的开始（秒与纳秒归零）
     *
     * @param dateTime LocalDateTime类型参数
     * @return static LocalDateTime类型返回值，dateTime为null时返回null
     */
    public static LocalDateTime minuteStart(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.withSecond(0).withNano(0);
    }

    /**
     * minuteEnd方法。
     * 获取指定时间所在分钟的结束（秒置为59，纳秒归零）
     *
     * @param dateTime LocalDateTime类型参数
     * @return static LocalDateTime类型返回值，dateTime为null时返回null
     */
    public static LocalDateTime minuteEnd(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.withSecond(59).withNano(0);
    }

    /**
     * of方法。
     * * @param year int类型参数
     *
     * @param month      int类型参数
     * @param dayOfMonth int类型参数
     * @param hour       int类型参数
     * @param minute     int类型参数
     * @return static LocalDateTime类型返回值
     */
    public static LocalDateTime of(int year, int month, int dayOfMonth, int hour, int minute) {
        return LocalDateTime.of(year, month, dayOfMonth, hour, minute);
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
     * @return static LocalDateTime类型返回值
     */
    public static LocalDateTime of(int year, int month, int dayOfMonth, int hour, int minute, int second) {
        return LocalDateTime.of(year, month, dayOfMonth, hour, minute, second);
    }

    /**
     * of方法。
     * * @param year int类型参数
     *
     * @param month        int类型参数
     * @param dayOfMonth   int类型参数
     * @param hour         int类型参数
     * @param minute       int类型参数
     * @param second       int类型参数
     * @param nanoOfSecond int类型参数
     * @return static LocalDateTime类型返回值
     */
    public static LocalDateTime of(int year, int month, int dayOfMonth, int hour, int minute, int second, int nanoOfSecond) {
        return LocalDateTime.of(year, month, dayOfMonth, hour, minute, second, nanoOfSecond);
    }

    /**
     * of方法。
     * * @param date LocalDate类型参数
     *
     * @param time LocalTime类型参数
     * @return static LocalDateTime类型返回值
     */
    public static LocalDateTime of(LocalDate date, LocalTime time) {
        return date.atTime(time);
    }

    // ==================== 日期时间计算 ====================

    /**
     * plusYears方法。
     * * @param dateTime LocalDateTime类型参数
     *
     * @param years long类型参数
     * @return static LocalDateTime类型返回值
     */
    public static LocalDateTime plusYears(LocalDateTime dateTime, long years) {
        return dateTime.plusYears(years);
    }

    /**
     * minusYears方法。
     * * @param dateTime LocalDateTime类型参数
     *
     * @param years long类型参数
     * @return static LocalDateTime类型返回值
     */
    public static LocalDateTime minusYears(LocalDateTime dateTime, long years) {
        return dateTime.minusYears(years);
    }

    /**
     * plusMonths方法。
     * * @param dateTime LocalDateTime类型参数
     *
     * @param months long类型参数
     * @return static LocalDateTime类型返回值
     */
    public static LocalDateTime plusMonths(LocalDateTime dateTime, long months) {
        return dateTime.plusMonths(months);
    }

    /**
     * minusMonths方法。
     * * @param dateTime LocalDateTime类型参数
     *
     * @param months long类型参数
     * @return static LocalDateTime类型返回值
     */
    public static LocalDateTime minusMonths(LocalDateTime dateTime, long months) {
        return dateTime.minusMonths(months);
    }

    /**
     * plusDays方法。
     * * @param dateTime LocalDateTime类型参数
     *
     * @param days long类型参数
     * @return static LocalDateTime类型返回值
     */
    public static LocalDateTime plusDays(LocalDateTime dateTime, long days) {
        return dateTime.plusDays(days);
    }

    /**
     * minusDays方法。
     * * @param dateTime LocalDateTime类型参数
     *
     * @param days long类型参数
     * @return static LocalDateTime类型返回值
     */
    public static LocalDateTime minusDays(LocalDateTime dateTime, long days) {
        return dateTime.minusDays(days);
    }

    /**
     * plusHours方法。
     * * @param dateTime LocalDateTime类型参数
     *
     * @param hours long类型参数
     * @return static LocalDateTime类型返回值
     */
    public static LocalDateTime plusHours(LocalDateTime dateTime, long hours) {
        return dateTime.plusHours(hours);
    }

    /**
     * minusHours方法。
     * * @param dateTime LocalDateTime类型参数
     *
     * @param hours long类型参数
     * @return static LocalDateTime类型返回值
     */
    public static LocalDateTime minusHours(LocalDateTime dateTime, long hours) {
        return dateTime.minusHours(hours);
    }

    /**
     * plusMinutes方法。
     * * @param dateTime LocalDateTime类型参数
     *
     * @param minutes long类型参数
     * @return static LocalDateTime类型返回值
     */
    public static LocalDateTime plusMinutes(LocalDateTime dateTime, long minutes) {
        return dateTime.plusMinutes(minutes);
    }

    /**
     * minusMinutes方法。
     * * @param dateTime LocalDateTime类型参数
     *
     * @param minutes long类型参数
     * @return static LocalDateTime类型返回值
     */
    public static LocalDateTime minusMinutes(LocalDateTime dateTime, long minutes) {
        return dateTime.minusMinutes(minutes);
    }

    /**
     * plusSeconds方法。
     * * @param dateTime LocalDateTime类型参数
     *
     * @param seconds long类型参数
     * @return static LocalDateTime类型返回值
     */
    public static LocalDateTime plusSeconds(LocalDateTime dateTime, long seconds) {
        return dateTime.plusSeconds(seconds);
    }

    /**
     * minusSeconds方法。
     * * @param dateTime LocalDateTime类型参数
     *
     * @param seconds long类型参数
     * @return static LocalDateTime类型返回值
     */
    public static LocalDateTime minusSeconds(LocalDateTime dateTime, long seconds) {
        return dateTime.minusSeconds(seconds);
    }

    // ==================== 日期调整 ====================

    /**
     * firstDayOfMonth方法。
     * * @param dateTime LocalDateTime类型参数
     *
     * @return static LocalDateTime类型返回值
     */
    public static LocalDateTime firstDayOfMonth(LocalDateTime dateTime) {
        return dateTime.with(TemporalAdjusters.firstDayOfMonth());
    }

    /**
     * lastDayOfMonth方法。
     * * @param dateTime LocalDateTime类型参数
     *
     * @return static LocalDateTime类型返回值
     */
    public static LocalDateTime lastDayOfMonth(LocalDateTime dateTime) {
        return dateTime.with(TemporalAdjusters.lastDayOfMonth());
    }

    /**
     * firstDayOfYear方法。
     * * @param dateTime LocalDateTime类型参数
     *
     * @return static LocalDateTime类型返回值
     */
    public static LocalDateTime firstDayOfYear(LocalDateTime dateTime) {
        return dateTime.with(TemporalAdjusters.firstDayOfYear());
    }

    /**
     * lastDayOfYear方法。
     * * @param dateTime LocalDateTime类型参数
     *
     * @return static LocalDateTime类型返回值
     */
    public static LocalDateTime lastDayOfYear(LocalDateTime dateTime) {
        return dateTime.with(TemporalAdjusters.lastDayOfYear());
    }

    /**
     * firstDayOfWeek方法。
     * * @param dateTime LocalDateTime类型参数
     *
     * @return static LocalDateTime类型返回值
     */
    public static LocalDateTime firstDayOfWeek(LocalDateTime dateTime) {
        return dateTime.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    /**
     * lastDayOfWeek方法。
     * * @param dateTime LocalDateTime类型参数
     *
     * @return static LocalDateTime类型返回值
     */
    public static LocalDateTime lastDayOfWeek(LocalDateTime dateTime) {
        return dateTime.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
    }

    /**
     * quarterStart方法。
     * 获取指定时间所在季度的开始时间（季度首月1日 00:00:00）
     *
     * @param dateTime LocalDateTime类型参数
     * @return static LocalDateTime类型返回值，dateTime为null时返回null
     */
    public static LocalDateTime quarterStart(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        Month firstMonthOfQuarter = dateTime.getMonth().firstMonthOfQuarter();
        return LocalDate.of(dateTime.getYear(), firstMonthOfQuarter, 1).atStartOfDay();
    }

    /**
     * quarterEnd方法。
     * 获取指定时间所在季度的结束时间（季度末月最后一日 23:59:59.999999999）
     *
     * @param dateTime LocalDateTime类型参数
     * @return static LocalDateTime类型返回值，dateTime为null时返回null
     */
    public static LocalDateTime quarterEnd(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        Month lastMonthOfQuarter = dateTime.getMonth().firstMonthOfQuarter().plus(2);
        LocalDate lastDay = LocalDate.of(dateTime.getYear(), lastMonthOfQuarter, 1)
                .with(TemporalAdjusters.lastDayOfMonth());
        return lastDay.atTime(LocalTime.MAX);
    }

    /**
     * halfYearStart方法。
     * 获取指定时间所在半年的开始时间（1月或7月1日 00:00:00）
     *
     * @param dateTime LocalDateTime类型参数
     * @return static LocalDateTime类型返回值，dateTime为null时返回null
     */
    public static LocalDateTime halfYearStart(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        Month firstMonthOfHalfYear = dateTime.getMonthValue() > 6 ? Month.JULY : Month.JANUARY;
        return LocalDate.of(dateTime.getYear(), firstMonthOfHalfYear, 1).atStartOfDay();
    }

    /**
     * halfYearEnd方法。
     * 获取指定时间所在半年的结束时间（6月或12月最后一日 23:59:59.999999999）
     *
     * @param dateTime LocalDateTime类型参数
     * @return static LocalDateTime类型返回值，dateTime为null时返回null
     */
    public static LocalDateTime halfYearEnd(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        Month lastMonthOfHalfYear = dateTime.getMonthValue() > 6 ? Month.DECEMBER : Month.JUNE;
        LocalDate lastDay = LocalDate.of(dateTime.getYear(), lastMonthOfHalfYear, 1)
                .with(TemporalAdjusters.lastDayOfMonth());
        return lastDay.atTime(LocalTime.MAX);
    }

    /**
     * withHour方法。
     * * @param dateTime LocalDateTime类型参数
     *
     * @param hour int类型参数
     * @return static LocalDateTime类型返回值
     */
    public static LocalDateTime withHour(LocalDateTime dateTime, int hour) {
        return dateTime.withHour(hour);
    }

    /**
     * withMinute方法。
     * * @param dateTime LocalDateTime类型参数
     *
     * @param minute int类型参数
     * @return static LocalDateTime类型返回值
     */
    public static LocalDateTime withMinute(LocalDateTime dateTime, int minute) {
        return dateTime.withMinute(minute);
    }

    /**
     * withSecond方法。
     * * @param dateTime LocalDateTime类型参数
     *
     * @param second int类型参数
     * @return static LocalDateTime类型返回值
     */
    public static LocalDateTime withSecond(LocalDateTime dateTime, int second) {
        return dateTime.withSecond(second);
    }

    // ==================== 日期时间比较 ====================

    /**
     * isBefore方法。
     * * @param dateTime1 LocalDateTime类型参数
     *
     * @param dateTime2 LocalDateTime类型参数
     * @return static boolean类型返回值
     */
    public static boolean isBefore(LocalDateTime dateTime1, LocalDateTime dateTime2) {
        return dateTime1.isBefore(dateTime2);
    }

    /**
     * isAfter方法。
     * * @param dateTime1 LocalDateTime类型参数
     *
     * @param dateTime2 LocalDateTime类型参数
     * @return static boolean类型返回值
     */
    public static boolean isAfter(LocalDateTime dateTime1, LocalDateTime dateTime2) {
        return dateTime1.isAfter(dateTime2);
    }

    /**
     * isEqual方法。
     * * @param dateTime1 LocalDateTime类型参数
     *
     * @param dateTime2 LocalDateTime类型参数
     * @return static boolean类型返回值
     */
    public static boolean isEqual(LocalDateTime dateTime1, LocalDateTime dateTime2) {
        return dateTime1.isEqual(dateTime2);
    }

    /**
     * isSameDay方法。
     * * @param dateTime1 LocalDateTime类型参数
     *
     * @param dateTime2 LocalDateTime类型参数
     * @return static boolean类型返回值
     */
    public static boolean isSameDay(LocalDateTime dateTime1, LocalDateTime dateTime2) {
        return dateTime1.toLocalDate().isEqual(dateTime2.toLocalDate());
    }

    // ==================== 日期时间差 ====================

    /**
     * daysBetween方法。
     * * @param start LocalDateTime类型参数
     *
     * @param end LocalDateTime类型参数
     * @return static long类型返回值
     */
    public static long daysBetween(LocalDateTime start, LocalDateTime end) {
        return java.time.temporal.ChronoUnit.DAYS.between(start, end);
    }

    /**
     * hoursBetween方法。
     * * @param start LocalDateTime类型参数
     *
     * @param end LocalDateTime类型参数
     * @return static long类型返回值
     */
    public static long hoursBetween(LocalDateTime start, LocalDateTime end) {
        return java.time.temporal.ChronoUnit.HOURS.between(start, end);
    }

    /**
     * minutesBetween方法。
     * * @param start LocalDateTime类型参数
     *
     * @param end LocalDateTime类型参数
     * @return static long类型返回值
     */
    public static long minutesBetween(LocalDateTime start, LocalDateTime end) {
        return java.time.temporal.ChronoUnit.MINUTES.between(start, end);
    }

    /**
     * secondsBetween方法。
     * * @param start LocalDateTime类型参数
     *
     * @param end LocalDateTime类型参数
     * @return static long类型返回值
     */
    public static long secondsBetween(LocalDateTime start, LocalDateTime end) {
        return java.time.temporal.ChronoUnit.SECONDS.between(start, end);
    }

    /**
     * millisBetween方法。
     * * @param start LocalDateTime类型参数
     *
     * @param end LocalDateTime类型参数
     * @return static long类型返回值
     */
    public static long millisBetween(LocalDateTime start, LocalDateTime end) {
        return java.time.temporal.ChronoUnit.MILLIS.between(start, end);
    }

    // ==================== 获取日期时间部分 ====================

    /**
     * getYear方法。
     * * @param dateTime LocalDateTime类型参数
     *
     * @return static int类型返回值
     */
    public static int getYear(LocalDateTime dateTime) {
        return dateTime.getYear();
    }

    /**
     * getMonth方法。
     * * @param dateTime LocalDateTime类型参数
     *
     * @return static int类型返回值
     */
    public static int getMonth(LocalDateTime dateTime) {
        return dateTime.getMonthValue();
    }

    /**
     * getDayOfMonth方法。
     * * @param dateTime LocalDateTime类型参数
     *
     * @return static int类型返回值
     */
    public static int getDayOfMonth(LocalDateTime dateTime) {
        return dateTime.getDayOfMonth();
    }

    /**
     * getHour方法。
     * * @param dateTime LocalDateTime类型参数
     *
     * @return static int类型返回值
     */
    public static int getHour(LocalDateTime dateTime) {
        return dateTime.getHour();
    }

    /**
     * getMinute方法。
     * * @param dateTime LocalDateTime类型参数
     *
     * @return static int类型返回值
     */
    public static int getMinute(LocalDateTime dateTime) {
        return dateTime.getMinute();
    }

    /**
     * getSecond方法。
     * * @param dateTime LocalDateTime类型参数
     *
     * @return static int类型返回值
     */
    public static int getSecond(LocalDateTime dateTime) {
        return dateTime.getSecond();
    }

    /**
     * getDayOfWeek方法。
     * * @param dateTime LocalDateTime类型参数
     *
     * @return static DayOfWeek类型返回值
     */
    public static DayOfWeek getDayOfWeek(LocalDateTime dateTime) {
        return dateTime.getDayOfWeek();
    }

    // ==================== 转换到其他类型 ====================

    /**
     * toDate方法。
     * * @param dateTime LocalDateTime类型参数
     *
     * @return static Date类型返回值
     */
    public static Date toDate(LocalDateTime dateTime) {
        return TimeConverter.toDate(dateTime);
    }

    /**
     * toDate方法。
     * * @param dateTime LocalDateTime类型参数
     *
     * @param zoneId ZoneId类型参数
     * @return static Date类型返回值
     */
    public static Date toDate(LocalDateTime dateTime, ZoneId zoneId) {
        return TimeConverter.toDate(dateTime, zoneId);
    }

    /**
     * toLocalDate方法。
     * * @param dateTime LocalDateTime类型参数
     *
     * @return static LocalDate类型返回值
     */
    public static LocalDate toLocalDate(LocalDateTime dateTime) {
        return TimeConverter.toLocalDate(dateTime);
    }

    /**
     * toLocalTime方法。
     * * @param dateTime LocalDateTime类型参数
     *
     * @return static LocalTime类型返回值
     */
    public static LocalTime toLocalTime(LocalDateTime dateTime) {
        return TimeConverter.toLocalTime(dateTime);
    }

    /**
     * toInstant方法。
     * * @param dateTime LocalDateTime类型参数
     *
     * @return static Instant类型返回值
     */
    public static Instant toInstant(LocalDateTime dateTime) {
        return TimeConverter.toInstant(dateTime);
    }

    /**
     * toInstant方法。
     * * @param dateTime LocalDateTime类型参数
     *
     * @param zoneId ZoneId类型参数
     * @return static Instant类型返回值
     */
    public static Instant toInstant(LocalDateTime dateTime, ZoneId zoneId) {
        return TimeConverter.toInstant(dateTime, zoneId);
    }

    /**
     * toEpochMilli方法。
     * * @param dateTime LocalDateTime类型参数
     *
     * @return static long类型返回值
     */
    public static long toEpochMilli(LocalDateTime dateTime) {
        return TimeConverter.toEpochMilli(dateTime);
    }

    /**
     * toEpochSecond方法。
     * * @param dateTime LocalDateTime类型参数
     *
     * @return static long类型返回值
     */
    public static long toEpochSecond(LocalDateTime dateTime) {
        return TimeConverter.toEpochSecond(dateTime);
    }

    /**
     * toZonedDateTime方法。
     * * @param dateTime LocalDateTime类型参数
     *
     * @return static ZonedDateTime类型返回值
     */
    public static ZonedDateTime toZonedDateTime(LocalDateTime dateTime) {
        return TimeConverter.toZonedDateTime(dateTime);
    }

    /**
     * toZonedDateTime方法。
     * * @param dateTime LocalDateTime类型参数
     *
     * @param zoneId ZoneId类型参数
     * @return static ZonedDateTime类型返回值
     */
    public static ZonedDateTime toZonedDateTime(LocalDateTime dateTime, ZoneId zoneId) {
        return TimeConverter.toZonedDateTime(dateTime, zoneId);
    }

    // ==================== 从其他类型转换 ====================

    /**
     * fromDate方法。
     * * @param date Date类型参数
     *
     * @return static LocalDateTime类型返回值
     */
    public static LocalDateTime fromDate(Date date) {
        return TimeConverter.toLocalDateTime(date);
    }

    /**
     * fromDate方法。
     * * @param date Date类型参数
     *
     * @param zoneId ZoneId类型参数
     * @return static LocalDateTime类型返回值
     */
    public static LocalDateTime fromDate(Date date, ZoneId zoneId) {
        return TimeConverter.toLocalDateTime(date, zoneId);
    }

    /**
     * fromLocalDate方法。
     * * @param date LocalDate类型参数
     *
     * @return static LocalDateTime类型返回值
     */
    public static LocalDateTime fromLocalDate(LocalDate date) {
        return TimeConverter.toLocalDateTime(date);
    }

    /**
     * fromLocalDate方法。
     * * @param date LocalDate类型参数
     *
     * @param localTime LocalTime类型参数
     * @return static LocalDateTime类型返回值
     */
    public static LocalDateTime fromLocalDate(LocalDate date, LocalTime localTime) {
        return TimeConverter.toLocalDateTime(date, localTime);
    }

    /**
     * fromLocalTime方法。
     * * @param localTime LocalTime类型参数
     *
     * @return static LocalDateTime类型返回值
     */
    public static LocalDateTime fromLocalTime(LocalTime localTime) {
        return TimeConverter.toLocalDateTime(localTime);
    }

    /**
     * fromInstant方法。
     * * @param instant Instant类型参数
     *
     * @return static LocalDateTime类型返回值
     */
    public static LocalDateTime fromInstant(Instant instant) {
        return TimeConverter.toLocalDateTime(instant);
    }

    /**
     * fromInstant方法。
     * * @param instant Instant类型参数
     *
     * @param zoneId ZoneId类型参数
     * @return static LocalDateTime类型返回值
     */
    public static LocalDateTime fromInstant(Instant instant, ZoneId zoneId) {
        return TimeConverter.toLocalDateTime(instant, zoneId);
    }

    /**
     * fromEpochMilli方法。
     * * @param epochMilli long类型参数
     *
     * @return static LocalDateTime类型返回值
     */
    public static LocalDateTime fromEpochMilli(long epochMilli) {
        return TimeConverter.fromEpochMilli(epochMilli);
    }

    /**
     * fromZonedDateTime方法。
     * * @param zonedDateTime ZonedDateTime类型参数
     *
     * @return static LocalDateTime类型返回值
     */
    public static LocalDateTime fromZonedDateTime(ZonedDateTime zonedDateTime) {
        return TimeConverter.toLocalDateTime(zonedDateTime);
    }

    /**
     * listDateTimesBetween方法。
     * 列出起止时间之间每天的同一时刻（按天步进，含两端）
     *
     * @param start LocalDateTime类型参数，开始时间（含）
     * @param end   LocalDateTime类型参数，结束时间（含）
     * @return static List<LocalDateTime>类型返回值，按自然顺序排列；起止任一为null或start晚于end时返回空列表
     */
    public static List<LocalDateTime> listDateTimesBetween(LocalDateTime start, LocalDateTime end) {
        List<LocalDateTime> dateTimes = new ArrayList<>();
        if (start == null || end == null || start.isAfter(end)) {
            return dateTimes;
        }
        for (LocalDateTime dt = start; !dt.isAfter(end); dt = dt.plusDays(1)) {
            dateTimes.add(dt);
        }
        return dateTimes;
    }
}