package com.zifang.util.core.time;

import java.time.Duration;
import java.time.Period;
import java.time.temporal.ChronoUnit;

/**
 * Duration 和 Period 工具类
 * <p>
 * Duration 处理时间量（秒、纳秒），适合测量机器时间；
 * Period 处理日期量（年、月、日），适合测量人类时间。
 * </p>
 * <p>
 * Duration 特点：
 * <ul>
 *   <li>基于时间单位（秒、毫秒、纳秒等）</li>
 *   <li>适用于时间长度测量、计时器、过期时间等</li>
 *   <li>格式化支持：天小时分秒、纯秒数等</li>
 * </ul>
 * </p>
 * <p>
 * Period 特点：
 * <ul>
 *   <li>基于日历日期单位（年、月、日）</li *   <li>适用于年龄计算、项目进度、租约期限等</li>
 *   <li>支持跨时区、跨夏令时的日期计算</li>
 * </ul>
 * </p>
 *
 * @author zifang
 * @see java.time.Duration
 * @see java.time.Period
 * @see ChronoUnit
 */
public class DurationUtil {

    // ==================== Duration 创建 ====================

    /**
     * 根据毫秒数创建Duration
     *
     * @param millis 毫秒数
     * @return 对应的Duration对象
     */
    public static Duration ofMillis(long millis) {
        return Duration.ofMillis(millis);
    }

    /**
     * 根据秒数创建Duration
     *
     * @param seconds 秒数
     * @return 对应的Duration对象
     */
    public static Duration ofSeconds(long seconds) {
        return Duration.ofSeconds(seconds);
    }

    /**
     * 根据分钟数创建Duration
     *
     * @param minutes 分钟数
     * @return 对应的Duration对象
     */
    public static Duration ofMinutes(long minutes) {
        return Duration.ofMinutes(minutes);
    }

    /**
     * 根据小时数创建Duration
     *
     * @param hours 小时数
     * @return 对应的Duration对象
     */
    public static Duration ofHours(long hours) {
        return Duration.ofHours(hours);
    }

    /**
     * 根据天数创建Duration
     *
     * @param days 天数
     * @return 对应的Duration对象
     */
    public static Duration ofDays(long days) {
        return Duration.ofDays(days);
    }

    /**
     * 计算两个时间点之间的Duration
     *
     * @param startInclusive 开始时间点（包含）
     * @param endExclusive   结束时间点（不包含）
     * @return 两个时间点之间的Duration
     * @throws NullPointerException if startInclusive or endExclusive is null
     */
    public static Duration between(java.time.temporal.Temporal startInclusive, java.time.temporal.Temporal endExclusive) {
        return Duration.between(startInclusive, endExclusive);
    }

    // ==================== Duration 格式化 ====================

    /**
     * 格式化Duration为字符串，显示秒和毫秒
     * <p>
     * 格式：如 "10秒" 或 "10.123秒"
     *
     * @param duration 要格式化的Duration
     * @return 格式化后的字符串，如果输入为null则返回null
     */
    public static String format(Duration duration) {
        if (duration == null) return null;
        long seconds = duration.getSeconds();
        int millis = duration.getNano() / 1_000_000;
        if (millis == 0) {
            return String.format("%d秒", seconds);
        }
        return String.format("%d.%03d秒", seconds, millis);
    }

    /**
     * 格式化Duration为 HH:mm:ss 格式的字符串
     * <p>
     * 格式：如 "01:02:03" 表示1小时2分钟3秒
     *
     * @param duration 要格式化的Duration
     * @return 格式化后的字符串，如果输入为null则返回null
     */
    public static String formatHMS(Duration duration) {
        if (duration == null) return null;
        long hours = duration.toHours();
        int minutes = (int) (duration.toMinutes() % 60);
        int seconds = (int) (duration.getSeconds() % 60);
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    /**
     * 格式化Duration为 天HH:mm:ss 格式的字符串
     * <p>
     * 格式：如 "1天 02:03:04" 或 "02:03:04"（不足一天不显示天）
     *
     * @param duration 要格式化的Duration
     * @return 格式化后的字符串，如果输入为null则返回null
     */
    public static String formatDHMS(Duration duration) {
        if (duration == null) return null;
        long days = duration.toDays();
        int hours = (int) ((duration.getSeconds() / 3600) % 24);
        int minutes = (int) ((duration.getSeconds() % 3600) / 60);
        int seconds = (int) (duration.getSeconds() % 60);
        if (days > 0) {
            return String.format("%d天 %02d:%02d:%02d", days, hours, minutes, seconds);
        }
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    // ==================== Duration 计算 ====================

    /**
     * 为Duration增加指定的毫秒数
     *
     * @param duration 原Duration，不能为null
     * @param millis   要增加的毫秒数
     * @return 增加后的新Duration对象
     * @throws NullPointerException if duration is null
     */
    public static Duration plusMillis(Duration duration, long millis) {
        return duration.plusMillis(millis);
    }

    /**
     * 为Duration减少指定的毫秒数
     *
     * @param duration 原Duration，不能为null
     * @param millis   要减少的毫秒数
     * @return 减少后的新Duration对象
     * @throws NullPointerException if duration is null
     */
    public static Duration minusMillis(Duration duration, long millis) {
        return duration.minusMillis(millis);
    }

    /**
     * 为Duration增加指定的秒数
     *
     * @param duration 原Duration，不能为null
     * @param seconds  要增加的秒数
     * @return 增加后的新Duration对象
     * @throws NullPointerException if duration is null
     */
    public static Duration plusSeconds(Duration duration, long seconds) {
        return duration.plusSeconds(seconds);
    }

    /**
     * 为Duration减少指定的秒数
     *
     * @param duration 原Duration，不能为null
     * @param seconds  要减少的秒数
     * @return 减少后的新Duration对象
     * @throws NullPointerException if duration is null
     */
    public static Duration minusSeconds(Duration duration, long seconds) {
        return duration.minusSeconds(seconds);
    }

    /**
     * Duration乘法运算
     *
     * @param duration     原Duration，不能为null
     * @param multiplicand 乘数
     * @return 乘积后的新Duration对象
     * @throws NullPointerException if duration is null
     */
    public static Duration multipliedBy(Duration duration, long multiplicand) {
        return duration.multipliedBy(multiplicand);
    }

    /**
     * Duration除法运算
     *
     * @param duration 原Duration，不能为null
     * @param divisor  除数，不能为0
     * @return 除后的新Duration对象
     * @throws NullPointerException if duration is null
     * @throws ArithmeticException  if divisor is zero
     */
    public static Duration dividedBy(Duration duration, long divisor) {
        return duration.dividedBy(divisor);
    }

    /**
     * 返回Duration的绝对值
     * <p>
     * 如果Duration为负数，则返回其negated值；否则返回原值
     *
     * @param duration 原Duration，不能为null
     * @return 绝对值后的Duration对象
     * @throws NullPointerException if duration is null
     */
    public static Duration abs(Duration duration) {
        return duration.isNegative() ? duration.negated() : duration;
    }

    // ==================== Duration 比较 ====================

    /**
     * 判断第一个Duration是否小于第二个
     *
     * @param duration1 第一个Duration，不能为null
     * @param duration2 第二个Duration，不能为null
     * @return 如果duration1小于duration2则返回true
     * @throws NullPointerException if duration1 or duration2 is null
     */
    public static boolean isBefore(Duration duration1, Duration duration2) {
        return duration1.compareTo(duration2) < 0;
    }

    /**
     * 判断第一个Duration是否大于第二个
     *
     * @param duration1 第一个Duration，不能为null
     * @param duration2 第二个Duration，不能为null
     * @return 如果duration1大于duration2则返回true
     * @throws NullPointerException if duration1 or duration2 is null
     */
    public static boolean isAfter(Duration duration1, Duration duration2) {
        return duration1.compareTo(duration2) > 0;
    }

    /**
     * 判断Duration是否为负数
     *
     * @param duration 要判断的Duration，不能为null
     * @return 如果为负数则返回true
     * @throws NullPointerException if duration is null
     */
    public static boolean isNegative(Duration duration) {
        return duration.isNegative();
    }

    /**
     * 判断Duration是否为零
     *
     * @param duration 要判断的Duration，不能为null
     * @return 如果为零则返回true
     * @throws NullPointerException if duration is null
     */
    public static boolean isZero(Duration duration) {
        return duration.isZero();
    }

    // ==================== Duration 转换 ====================

    /**
     * 将Duration转换为毫秒
     *
     * @param duration 要转换的Duration
     * @return 毫秒值，如果输入为null则返回0
     */
    public static long toMillis(Duration duration) {
        if (duration == null) return 0;
        return duration.toMillis();
    }

    /**
     * 将Duration转换为秒
     *
     * @param duration 要转换的Duration
     * @return 秒值（总计秒数），如果输入为null则返回0
     */
    public static long toSeconds(Duration duration) {
        if (duration == null) return 0;
        return duration.getSeconds();
    }

    /**
     * 将Duration转换为分钟
     *
     * @param duration 要转换的Duration
     * @return 分钟值（总计分钟数），如果输入为null则返回0
     */
    public static long toMinutes(Duration duration) {
        if (duration == null) return 0;
        return duration.toMinutes();
    }

    /**
     * 将Duration转换为小时
     *
     * @param duration 要转换的Duration
     * @return 小时值（总计小时数），如果输入为null则返回0
     */
    public static long toHours(Duration duration) {
        if (duration == null) return 0;
        return duration.toHours();
    }

    /**
     * 将Duration转换为天
     *
     * @param duration 要转换的Duration
     * @return 天数（总天数），如果输入为null则返回0
     */
    public static long toDays(Duration duration) {
        if (duration == null) return 0;
        return duration.toDays();
    }

    /**
     * 将Duration转换为纳秒
     *
     * @param duration 要转换的Duration
     * @return 纳秒值（总纳秒数），如果输入为null则返回0
     */
    public static long toNanos(Duration duration) {
        if (duration == null) return 0;
        return duration.toNanos();
    }

    // ==================== Duration 与 ChronoUnit 互转 ====================

    /**
     * 根据ChronoUnit和数量创建Duration
     *
     * @param unit   时间单位，不能为null
     * @param amount 数量
     * @return 对应的Duration对象
     * @throws NullPointerException if unit is null
     */
    public static Duration of(ChronoUnit unit, long amount) {
        return Duration.of(amount, unit);
    }

    /**
     * 将Duration转换为指定的ChronoUnit
     *
     * @param duration 要转换的Duration
     * @param unit     目标时间单位，不能为null
     * @return 转换后的数量
     * @throws NullPointerException if duration or unit is null
     */
    public static long toUnit(Duration duration, ChronoUnit unit) {
        return unit.between(java.time.Instant.EPOCH, java.time.Instant.EPOCH.plus(duration));
    }

    // ==================== Period 创建 ====================

    /**
     * 根据天数创建Period
     *
     * @param days 天数
     * @return 对应的Period对象
     */
    public static Period ofDays(int days) {
        return Period.ofDays(days);
    }

    /**
     * 根据周数创建Period
     *
     * @param weeks 周数
     * @return 对应的Period对象
     */
    public static Period ofWeeks(int weeks) {
        return Period.ofWeeks(weeks);
    }

    /**
     * 根据月数创建Period
     *
     * @param months 月数
     * @return 对应的Period对象
     */
    public static Period ofMonths(int months) {
        return Period.ofMonths(months);
    }

    /**
     * 根据年数创建Period
     *
     * @param years 年数
     * @return 对应的Period对象
     */
    public static Period ofYears(int years) {
        return Period.ofYears(years);
    }

    /**
     * 根据年、月、日创建Period
     *
     * @param years  年数
     * @param months 月数
     * @param days   天数
     * @return 对应的Period对象
     */
    public static Period of(int years, int months, int days) {
        return Period.of(years, months, days);
    }

    /**
     * 计算两个日期之间的Period
     *
     * @param start 开始日期（包含）
     * @param end   结束日期（包含）
     * @return 两个日期之间的Period
     * @throws NullPointerException if start or end is null
     */
    public static Period between(java.time.LocalDate start, java.time.LocalDate end) {
        return Period.between(start, end);
    }

    // ==================== Period 格式化 ====================

    /**
     * 格式化Period为中文字符串
     * <p>
     * 格式：如 "1年2月3日" 或 "2月3日" 或 "3日"
     *
     * @param period 要格式化的Period
     * @return 格式化后的字符串，如果输入为null则返回null
     */
    public static String format(Period period) {
        if (period == null) return null;
        StringBuilder sb = new StringBuilder();
        if (period.getYears() > 0) {
            sb.append(period.getYears()).append("年");
        }
        if (period.getMonths() > 0) {
            sb.append(period.getMonths()).append("月");
        }
        if (period.getDays() > 0) {
            sb.append(period.getDays()).append("日");
        }
        return sb.length() > 0 ? sb.toString() : "0日";
    }

    /**
     * 格式化Period为标准中文日期字符串
     * <p>
     * 格式：始终显示完整格式 "X年X月X日"
     *
     * @param period 要格式化的Period
     * @return 格式化后的字符串，如果输入为null则返回null
     */
    public static String formatYMD(Period period) {
        if (period == null) return null;
        return String.format("%d年%d月%d日", period.getYears(), period.getMonths(), period.getDays());
    }

    // ==================== Period 计算 ====================

    /**
     * 为Period增加指定的天数
     *
     * @param period 原Period，不能为null
     * @param days   要增加的天数
     * @return 增加后的新Period对象
     * @throws NullPointerException if period is null
     */
    public static Period plusDays(Period period, int days) {
        return period.plusDays(days);
    }

    /**
     * 为Period减少指定的天数
     *
     * @param period 原Period，不能为null
     * @param days   要减少的天数
     * @return 减少后的新Period对象
     * @throws NullPointerException if period is null
     */
    public static Period minusDays(Period period, int days) {
        return period.minusDays(days);
    }

    /**
     * 为Period增加指定的月数
     *
     * @param period 原Period，不能为null
     * @param months 要增加的月数
     * @return 增加后的新Period对象
     * @throws NullPointerException if period is null
     */
    public static Period plusMonths(Period period, int months) {
        return period.plusMonths(months);
    }

    /**
     * 为Period减少指定的月数
     *
     * @param period 原Period，不能为null
     * @param months 要减少的月数
     * @return 减少后的新Period对象
     * @throws NullPointerException if period is null
     */
    public static Period minusMonths(Period period, int months) {
        return period.minusMonths(months);
    }

    /**
     * 为Period增加指定的年数
     *
     * @param period 原Period，不能为null
     * @param years  要增加的年数
     * @return 增加后的新Period对象
     * @throws NullPointerException if period is null
     */
    public static Period plusYears(Period period, int years) {
        return period.plusYears(years);
    }

    /**
     * 为Period减少指定的年数
     *
     * @param period 原Period，不能为null
     * @param years  要减少的年数
     * @return 减少后的新Period对象
     * @throws NullPointerException if period is null
     */
    public static Period minusYears(Period period, int years) {
        return period.minusYears(years);
    }

    /**
     * Period乘法运算
     *
     * @param period       原Period，不能为null
     * @param multiplicand 乘数
     * @return 乘积后的新Period对象
     * @throws NullPointerException if period is null
     */
    public static Period multipliedBy(Period period, int multiplicand) {
        return period.multipliedBy(multiplicand);
    }

    /**
     * 标准化Period
     * <p>
     * 将月数超过12的转为"年+月"的形式
     *
     * @param period 原Period，不能为null
     * @return 标准化后的新Period对象
     * @throws NullPointerException if period is null
     */
    public static Period normalized(Period period) {
        return period.normalized();
    }

    // ==================== Period 转换 ====================

    /**
     * 将Period转换为总天数
     * <p>
     * 转换规则：1年=365天，1月=30天
     *
     * @param period 要转换的Period
     * @return 总天数，如果输入为null则返回0
     */
    public static long toDays(Period period) {
        if (period == null) return 0;
        return period.getDays() + (long) period.getMonths() * 30 + (long) period.getYears() * 365;
    }

    /**
     * 获取Period的年数
     *
     * @param period 要获取的Period，不能为null
     * @return 年数
     * @throws NullPointerException if period is null
     */
    public static int getYears(Period period) {
        return period.getYears();
    }

    /**
     * 获取Period的月数
     *
     * @param period 要获取的Period，不能为null
     * @return 月数
     * @throws NullPointerException if period is null
     */
    public static int getMonths(Period period) {
        return period.getMonths();
    }

    /**
     * 获取Period的天数
     *
     * @param period 要获取的Period，不能为null
     * @return 天数
     * @throws NullPointerException if period is null
     */
    public static int getDays(Period period) {
        return period.getDays();
    }
}
