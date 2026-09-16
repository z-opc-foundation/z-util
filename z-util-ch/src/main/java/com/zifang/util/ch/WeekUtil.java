package com.zifang.util.ch;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

/**
 * 中文星期工具类
 * <p>
 * 提供日期与中文星期名称之间的转换功能，包括：
 * <ul>
 *   <li>日期转中文星期名称，支持「周X」与「星期X」两种风格</li>
 *   <li>ISO星期序号（1~7，1表示周一）转中文星期名称</li>
 *   <li>中文星期名称转ISO星期序号，兼容「周X」「星期X」「礼拜X」三种写法</li>
 * </ul>
 *
 * @author zifang
 */
public class WeekUtil {

    /**
     * 中文星期名称的数字部分，索引0对应周一（ISO DayOfWeek值为1）
     */
    private static final String[] WEEK_DAY_NAMES = {"一", "二", "三", "四", "五", "六", "日"};

    /**
     * 简短风格前缀：周
     */
    private static final String PREFIX_SHORT = "周";

    /**
     * 完整风格前缀：星期
    */
    private static final String PREFIX_FULL = "星期";

    /**
     * 口语风格前缀：礼拜
     */
    private static final String PREFIX_COLLOQUIAL = "礼拜";

    /**
     * 无法识别星期名称时的返回值
     */
    public static final int UNRECOGNIZED = -1;

    /**
     * WeekUtil工具类私有构造。
     */
    private WeekUtil() {
    }

    /**
     * 获取日期的中文星期名称（周X风格）
     *
     * @param date 日期
     * @return 形如「周一」~「周日」的星期名称；日期为null时返回null
     */
    public static String getWeekDay(LocalDate date) {
        return getWeekDay(date, false);
    }

    /**
     * 获取日期的中文星期名称
     *
     * @param date   日期
     * @param fullStyle true返回「星期X」风格，false返回「周X」风格
     * @return 星期名称；日期为null时返回null
     */
    public static String getWeekDay(LocalDate date, boolean fullStyle) {
        if (date == null) {
            return null;
        }
        return toWeekDayName(date.getDayOfWeek().getValue(), fullStyle);
    }

    /**
     * 获取日期的中文星期名称（周X风格）
     *
     * @param date 日期
     * @return 形如「周一」~「周日」的星期名称；日期为null时返回null
     */
    public static String getWeekDay(Date date) {
        if (date == null) {
            return null;
        }
        return getWeekDay(toLocalDate(date), false);
    }

    /**
     * 获取日期的中文星期名称
     *
     * @param date     日期
     * @param fullStyle true返回「星期X」风格，false返回「周X」风格
     * @return 星期名称；日期为null时返回null
     */
    public static String getWeekDay(Date date, boolean fullStyle) {
        if (date == null) {
            return null;
        }
        return getWeekDay(toLocalDate(date), fullStyle);
    }

    /**
     * 获取今天的中文星期名称（周X风格）
     *
     * @return 形如「周一」~「周日」的星期名称
     */
    public static String getWeekDayOfToday() {
        return getWeekDay(LocalDate.now(), false);
    }

    /**
     * 将ISO星期序号转换为中文星期名称（周X风格）
     *
     * @param dayOfWeekValue ISO星期序号，1表示周一，7表示周日
     * @return 形如「周一」~「周日」的星期名称；序号越界时返回null
     */
    public static String toWeekDayName(int dayOfWeekValue) {
        return toWeekDayName(dayOfWeekValue, false);
    }

    /**
     * 将ISO星期序号转换为中文星期名称
     *
     * @param dayOfWeekValue ISO星期序号，1表示周一，7表示周日
     * @param fullStyle      true返回「星期X」风格，false返回「周X」风格
     * @return 星期名称；序号越界时返回null
     */
    public static String toWeekDayName(int dayOfWeekValue, boolean fullStyle) {
        if (dayOfWeekValue < 1 || dayOfWeekValue > 7) {
            return null;
        }
        return (fullStyle ? PREFIX_FULL : PREFIX_SHORT) + WEEK_DAY_NAMES[dayOfWeekValue - 1];
    }

    /**
     * 将中文星期名称转换为ISO星期序号
     * <p>
     * 兼容「周X」「星期X」「礼拜X」三种前缀写法。
     *
     * @param weekDayName 中文星期名称
     * @return ISO星期序号（1表示周一，7表示周日）；无法识别时返回{@link #UNRECOGNIZED}
     */
    public static int toWeekDayValue(String weekDayName) {
        if (weekDayName == null || weekDayName.length() < 2) {
            return UNRECOGNIZED;
        }
        // 去除前缀后取末位字符匹配
        String lastChar = weekDayName.substring(weekDayName.length() - 1);
        for (int i = 0; i < WEEK_DAY_NAMES.length; i++) {
            if (WEEK_DAY_NAMES[i].equals(lastChar)) {
                return i + 1;
            }
        }
        return UNRECOGNIZED;
    }

    /**
     * 将Date转换为LocalDate（系统默认时区）
     * <p>
     * 不直接使用Date#toInstant，避免java.sql.Date子类不支持该方法的限制。
     *
     * @param date 日期
     * @return LocalDate
     */
    private static LocalDate toLocalDate(Date date) {
        return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    /**
     * 将中文星期名称转换为{@link DayOfWeek}
     *
     * @param weekDayName 中文星期名称
     * @return 对应的DayOfWeek；无法识别时返回null
     */
    public static DayOfWeek toDayOfWeek(String weekDayName) {
        int value = toWeekDayValue(weekDayName);
        if (value == UNRECOGNIZED) {
            return null;
        }
        return DayOfWeek.of(value);
    }
}
