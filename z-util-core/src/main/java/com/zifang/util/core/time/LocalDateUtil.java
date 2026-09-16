package com.zifang.util.core.time;

import com.zifang.util.core.time.converter.TimeConverter;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * java.time.LocalDate 工具类
 * <p>
 * 只处理LocalDate类型，提供LocalDate的格式化、解析、计算等功能
 */
public class LocalDateUtil {

    public static final String PATTERN_DEFAULT = "yyyy-MM-dd";
    public static final String PATTERN_COMPACT = "yyyyMMdd";
    public static final String PATTERN_MONTH = "yyyy-MM";
    public static final String PATTERN_MONTH_COMPACT = "yyyyMM";
    public static final String PATTERN_CHINESE = "yyyy年MM月dd日";
    public static final String PATTERN_CHINESE_MONTH = "yyyy年MM月";

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
     * * @param PATTERN_MONTH Object类型参数
     *
     * @return static final DateTimeFormatter FMT_MONTH =类型返回值
     */
    public static final DateTimeFormatter FMT_MONTH = DateTimeFormatter.ofPattern(PATTERN_MONTH);
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
     * * @param date LocalDate类型参数
     *
     * @return static String类型返回值
     */
    public static String format(LocalDate date) {
        return format(date, FMT_DEFAULT);
    }

    /**
     * format方法。
     * * @param date LocalDate类型参数
     *
     * @param pattern String类型参数
     * @return static String类型返回值
     */
    public static String format(LocalDate date, String pattern) {
        if (date == null) return null;
        return date.format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * format方法。
     * * @param date LocalDate类型参数
     *
     * @param formatter DateTimeFormatter类型参数
     * @return static String类型返回值
     */
    public static String format(LocalDate date, DateTimeFormatter formatter) {
        if (date == null) return null;
        return date.format(formatter);
    }

    // ==================== 解析 ====================

    /**
     * parse方法。
     * * @param dateStr String类型参数
     *
     * @return static LocalDate类型返回值
     */
    public static LocalDate parse(String dateStr) {
        return parse(dateStr, PATTERN_DEFAULT);
    }

    /**
     * parse方法。
     * * @param dateStr String类型参数
     *
     * @param pattern String类型参数
     * @return static LocalDate类型返回值
     */
    public static LocalDate parse(String dateStr, String pattern) {
        if (dateStr == null || dateStr.trim().isEmpty()) return null;
        return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * parseStrict方法。
     * * @param dateStr String类型参数
     *
     * @param patterns String...类型参数
     * @return static LocalDate类型返回值
     */
    public static LocalDate parseStrict(String dateStr, String... patterns) {
        if (dateStr == null || dateStr.trim().isEmpty()) return null;
        for (String pattern : patterns) {
            try {
                return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(pattern));
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
     * @return static LocalDate类型返回值
     */
    public static LocalDate now() {
        return LocalDate.now();
    }

    /**
     * today方法。
     *
     * @return static LocalDate类型返回值
     */
    public static LocalDate today() {
        return LocalDate.now();
    }

    /**
     * of方法。
     * * @param year int类型参数
     *
     * @param month      int类型参数
     * @param dayOfMonth int类型参数
     * @return static LocalDate类型返回值
     */
    public static LocalDate of(int year, int month, int dayOfMonth) {
        return LocalDate.of(year, month, dayOfMonth);
    }

    /**
     * ofYearDay方法。
     * * @param year int类型参数
     *
     * @param dayOfYear int类型参数
     * @return static LocalDate类型返回值
     */
    public static LocalDate ofYearDay(int year, int dayOfYear) {
        return LocalDate.ofYearDay(year, dayOfYear);
    }

    // ==================== 日期计算 ====================

    /**
     * plusDays方法。
     * * @param date LocalDate类型参数
     *
     * @param days long类型参数
     * @return static LocalDate类型返回值
     */
    public static LocalDate plusDays(LocalDate date, long days) {
        return date.plusDays(days);
    }

    /**
     * minusDays方法。
     * * @param date LocalDate类型参数
     *
     * @param days long类型参数
     * @return static LocalDate类型返回值
     */
    public static LocalDate minusDays(LocalDate date, long days) {
        return date.minusDays(days);
    }

    /**
     * plusWeeks方法。
     * * @param date LocalDate类型参数
     *
     * @param weeks long类型参数
     * @return static LocalDate类型返回值
     */
    public static LocalDate plusWeeks(LocalDate date, long weeks) {
        return date.plusWeeks(weeks);
    }

    /**
     * minusWeeks方法。
     * * @param date LocalDate类型参数
     *
     * @param weeks long类型参数
     * @return static LocalDate类型返回值
     */
    public static LocalDate minusWeeks(LocalDate date, long weeks) {
        return date.minusWeeks(weeks);
    }

    /**
     * plusMonths方法。
     * * @param date LocalDate类型参数
     *
     * @param months long类型参数
     * @return static LocalDate类型返回值
     */
    public static LocalDate plusMonths(LocalDate date, long months) {
        return date.plusMonths(months);
    }

    /**
     * minusMonths方法。
     * * @param date LocalDate类型参数
     *
     * @param months long类型参数
     * @return static LocalDate类型返回值
     */
    public static LocalDate minusMonths(LocalDate date, long months) {
        return date.minusMonths(months);
    }

    /**
     * plusYears方法。
     * * @param date LocalDate类型参数
     *
     * @param years long类型参数
     * @return static LocalDate类型返回值
     */
    public static LocalDate plusYears(LocalDate date, long years) {
        return date.plusYears(years);
    }

    /**
     * minusYears方法。
     * * @param date LocalDate类型参数
     *
     * @param years long类型参数
     * @return static LocalDate类型返回值
     */
    public static LocalDate minusYears(LocalDate date, long years) {
        return date.minusYears(years);
    }

    // ==================== 日期调整 ====================

    /**
     * firstDayOfMonth方法。
     * * @param date LocalDate类型参数
     *
     * @return static LocalDate类型返回值
     */
    public static LocalDate firstDayOfMonth(LocalDate date) {
        return date.with(TemporalAdjusters.firstDayOfMonth());
    }

    /**
     * lastDayOfMonth方法。
     * * @param date LocalDate类型参数
     *
     * @return static LocalDate类型返回值
     */
    public static LocalDate lastDayOfMonth(LocalDate date) {
        return date.with(TemporalAdjusters.lastDayOfMonth());
    }

    /**
     * firstDayOfYear方法。
     * * @param date LocalDate类型参数
     *
     * @return static LocalDate类型返回值
     */
    public static LocalDate firstDayOfYear(LocalDate date) {
        return date.with(TemporalAdjusters.firstDayOfYear());
    }

    /**
     * lastDayOfYear方法。
     * * @param date LocalDate类型参数
     *
     * @return static LocalDate类型返回值
     */
    public static LocalDate lastDayOfYear(LocalDate date) {
        return date.with(TemporalAdjusters.lastDayOfYear());
    }

    /**
     * firstDayOfWeek方法。
     * * @param date LocalDate类型参数
     *
     * @return static LocalDate类型返回值
     */
    public static LocalDate firstDayOfWeek(LocalDate date) {
        return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    /**
     * lastDayOfWeek方法。
     * * @param date LocalDate类型参数
     *
     * @return static LocalDate类型返回值
     */
    public static LocalDate lastDayOfWeek(LocalDate date) {
        return date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
    }

    /**
     * nextWorkingDay方法。
     * * @param date LocalDate类型参数
     *
     * @return static LocalDate类型返回值
     */
    public static LocalDate nextWorkingDay(LocalDate date) {
        return date.with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY));
    }

    // ==================== 日期比较 ====================

    /**
     * isBefore方法。
     * * @param date1 LocalDate类型参数
     *
     * @param date2 LocalDate类型参数
     * @return static boolean类型返回值
     */
    public static boolean isBefore(LocalDate date1, LocalDate date2) {
        return date1.isBefore(date2);
    }

    /**
     * isAfter方法。
     * * @param date1 LocalDate类型参数
     *
     * @param date2 LocalDate类型参数
     * @return static boolean类型返回值
     */
    public static boolean isAfter(LocalDate date1, LocalDate date2) {
        return date1.isAfter(date2);
    }

    /**
     * isEqual方法。
     * * @param date1 LocalDate类型参数
     *
     * @param date2 LocalDate类型参数
     * @return static boolean类型返回值
     */
    public static boolean isEqual(LocalDate date1, LocalDate date2) {
        return date1.isEqual(date2);
    }

    /**
     * isWeekend方法。
     * * @param date LocalDate类型参数
     *
     * @return static boolean类型返回值
     */
    public static boolean isWeekend(LocalDate date) {
        DayOfWeek dow = date.getDayOfWeek();
        return dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY;
    }

    /**
     * isLeapYear方法。
     * * @param date LocalDate类型参数
     *
     * @return static boolean类型返回值
     */
    public static boolean isLeapYear(LocalDate date) {
        return date.isLeapYear();
    }

    // ==================== 日期差 ====================

    /**
     * daysBetween方法。
     * * @param start LocalDate类型参数
     *
     * @param end LocalDate类型参数
     * @return static long类型返回值
     */
    public static long daysBetween(LocalDate start, LocalDate end) {
        return java.time.temporal.ChronoUnit.DAYS.between(start, end);
    }

    /**
     * weeksBetween方法。
     * * @param start LocalDate类型参数
     *
     * @param end LocalDate类型参数
     * @return static long类型返回值
     */
    public static long weeksBetween(LocalDate start, LocalDate end) {
        return java.time.temporal.ChronoUnit.WEEKS.between(start, end);
    }

    /**
     * monthsBetween方法。
     * * @param start LocalDate类型参数
     *
     * @param end LocalDate类型参数
     * @return static long类型返回值
     */
    public static long monthsBetween(LocalDate start, LocalDate end) {
        return java.time.temporal.ChronoUnit.MONTHS.between(start, end);
    }

    /**
     * yearsBetween方法。
     * * @param start LocalDate类型参数
     *
     * @param end LocalDate类型参数
     * @return static long类型返回值
     */
    public static long yearsBetween(LocalDate start, LocalDate end) {
        return java.time.temporal.ChronoUnit.YEARS.between(start, end);
    }

    // ==================== 获取日期部分 ====================

    /**
     * getYear方法。
     * * @param date LocalDate类型参数
     *
     * @return static int类型返回值
     */
    public static int getYear(LocalDate date) {
        return date.getYear();
    }

    /**
     * getMonth方法。
     * * @param date LocalDate类型参数
     *
     * @return static int类型返回值
     */
    public static int getMonth(LocalDate date) {
        return date.getMonthValue();
    }

    /**
     * getDayOfMonth方法。
     * * @param date LocalDate类型参数
     *
     * @return static int类型返回值
     */
    public static int getDayOfMonth(LocalDate date) {
        return date.getDayOfMonth();
    }

    /**
     * getDayOfYear方法。
     * * @param date LocalDate类型参数
     *
     * @return static int类型返回值
     */
    public static int getDayOfYear(LocalDate date) {
        return date.getDayOfYear();
    }

    /**
     * getDayOfWeek方法。
     * * @param date LocalDate类型参数
     *
     * @return static DayOfWeek类型返回值
     */
    public static DayOfWeek getDayOfWeek(LocalDate date) {
        return date.getDayOfWeek();
    }

    /**
     * getWeekdayName方法。
     * * @param date LocalDate类型参数
     *
     * @return static String类型返回值
     */
    public static String getWeekdayName(LocalDate date) {
        return date.getDayOfWeek().getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.CHINESE);
    }

    /**
     * getMonthName方法。
     * * @param date LocalDate类型参数
     *
     * @return static String类型返回值
     */
    public static String getMonthName(LocalDate date) {
        return date.getMonth().getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.CHINESE);
    }

    // ==================== 转换到其他类型 ====================

    /**
     * toDate方法。
     * * @param date LocalDate类型参数
     *
     * @return static Date类型返回值
     */
    public static Date toDate(LocalDate date) {
        return TimeConverter.toDate(date);
    }

    /**
     * toLocalDateTime方法。
     * * @param date LocalDate类型参数
     *
     * @return static LocalDateTime类型返回值
     */
    public static LocalDateTime toLocalDateTime(LocalDate date) {
        return TimeConverter.toLocalDateTime(date);
    }

    /**
     * toLocalDateTime方法。
     * * @param date LocalDate类型参数
     *
     * @param localTime LocalTime类型参数
     * @return static LocalDateTime类型返回值
     */
    public static LocalDateTime toLocalDateTime(LocalDate date, LocalTime localTime) {
        return TimeConverter.toLocalDateTime(date, localTime);
    }

    /**
     * toInstant方法。
     * * @param date LocalDate类型参数
     *
     * @return static Instant类型返回值
     */
    public static Instant toInstant(LocalDate date) {
        return TimeConverter.toInstant(date);
    }

    /**
     * toEpochMilli方法。
     * * @param date LocalDate类型参数
     *
     * @return static long类型返回值
     */
    public static long toEpochMilli(LocalDate date) {
        if (date == null) return 0;
        return date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    /**
     * toEpochDay方法。
     * * @param date LocalDate类型参数
     *
     * @return static String类型返回值
     */
    public static String toEpochDay(LocalDate date) {
        return String.valueOf(date.toEpochDay());
    }

    // ==================== 从其他类型转换 ====================

    /**
     * fromDate方法。
     * * @param date Date类型参数
     *
     * @return static LocalDate类型返回值
     */
    public static LocalDate fromDate(Date date) {
        return TimeConverter.toLocalDate(date);
    }

    /**
     * fromLocalDateTime方法。
     * * @param localDateTime LocalDateTime类型参数
     *
     * @return static LocalDate类型返回值
     */
    public static LocalDate fromLocalDateTime(LocalDateTime localDateTime) {
        return TimeConverter.toLocalDate(localDateTime);
    }

    /**
     * fromEpochDay方法。
     * * @param epochDay long类型参数
     *
     * @return static LocalDate类型返回值
     */
    public static LocalDate fromEpochDay(long epochDay) {
        return LocalDate.ofEpochDay(epochDay);
    }

    /**
     * fromInstant方法。
     * * @param instant Instant类型参数
     *
     * @return static LocalDate类型返回值
     */
    public static LocalDate fromInstant(Instant instant) {
        return TimeConverter.toLocalDate(instant);
    }

    /**
     * listDatesBetween方法。
     * 列出起止日期之间的每一天（含两端）
     *
     * @param start LocalDate类型参数，开始日期（含）
     * @param end   LocalDate类型参数，结束日期（含）
     * @return static List<LocalDate>类型返回值，按自然顺序排列；起止任一为null或start晚于end时返回空列表
     */
    public static List<LocalDate> listDatesBetween(LocalDate start, LocalDate end) {
        List<LocalDate> dates = new ArrayList<>();
        if (start == null || end == null || start.isAfter(end)) {
            return dates;
        }
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            dates.add(d);
        }
        return dates;
    }

    /**
     * age方法。
     * 计算出生日期相对指定日期的周岁年龄
     *
     * @param birthDate     LocalDate类型参数，出生日期
     * @param referenceDate LocalDate类型参数，参照日期
     * @return static int类型返回值，周岁年龄；任一参数为null或出生日期晚于参照日期时返回0
     */
    public static int age(LocalDate birthDate, LocalDate referenceDate) {
        if (birthDate == null || referenceDate == null || birthDate.isAfter(referenceDate)) {
            return 0;
        }
        return (int) birthDate.until(referenceDate, java.time.temporal.ChronoUnit.YEARS);
    }

    /**
     * age方法。
     * 计算出生日期相对当前日期的周岁年龄
     *
     * @param birthDate LocalDate类型参数，出生日期
     * @return static int类型返回值，周岁年龄；为null时返回0
     */
    public static int age(LocalDate birthDate) {
        return age(birthDate, LocalDate.now());
    }
}