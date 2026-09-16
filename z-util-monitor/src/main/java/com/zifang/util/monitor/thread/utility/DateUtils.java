package com.zifang.util.monitor.thread.utility;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 时间工具类。
 * <p>
 * 提供日期和时间相关的常用操作方法。
 *
 * @author zifang
 */
public final class DateUtils {

    /**
     * 默认日期格式：yyyy-MM-dd。
     */
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";

    /**
     * 默认日期时间格式：yyyy-MM-dd HH:mm:ss。
     */
    public static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 一天总毫秒数。
     */
    public static final long dayTotalMilliseconds = 24 * 60 * 60 * 1000L;

    /**
     * 一天总秒数。
     */
    public static final int dayTotalSeconds = 24 * 60 * 60;

    /**
     * 返回日期的字符串类型。
     * <p>
     * 例如：2014-06-04
     *
     * @param dateTimestamp 系统毫秒数
     * @return 指定毫秒时间戳对应的日期字符串
     */
    public static String getDateString(long dateTimestamp) {
        SimpleDateFormat df = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
        return df.format(dateTimestamp);
    }

    /**
     * 返回今天的日期。
     * <p>
     * 格式为：2014-06-04
     *
     * @return 今天的日期字符串
     */
    public static String getDateString() {
        SimpleDateFormat df = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
        return df.format(TimeUtil.getMillisTimestamp());
    }


    /**
     * 返回今天的日期时间。
     * <p>
     * 格式为：2014-06-04 13:23:22
     *
     * @return 今天的日期时间字符串
     */
    public static String getDateAndTimeString() {
        SimpleDateFormat df = new SimpleDateFormat(DEFAULT_DATE_TIME_FORMAT);
        return df.format(TimeUtil.getMillisTimestamp());
    }

    /**
     * 返回下一天日期的字符串。
     * <p>
     * 例如输入为2014-06-04的时间戳，返回2014-06-05
     *
     * @param dateTimestamp 系统毫秒数
     * @return 下一天日期字符串
     */
    public static String getNextDateString(long dateTimestamp) {
        SimpleDateFormat df = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
        return df.format(dateTimestamp + dayTotalMilliseconds);
    }

    /**
     * 根据时间戳，返回指定格式的日期时间字符串。
     *
     * @param timestamp 毫秒时间戳
     * @param format    日期时间格式，如"yyyy-MM-dd HH:mm:ss"
     * @return 格式化后的日期时间字符串
     */
    public static String getDateTimeString(long timestamp, String format) {
        SimpleDateFormat df = new SimpleDateFormat(format);
        return df.format(timestamp);
    }

    /**
     * 将Date对象转换为指定格式的字符串。
     *
     * @param date 日期对象
     * @param temp 日期格式，如"yyyy-MM-dd"
     * @return 格式化后的日期字符串
     */
    public static String dateToStr(Date date, String temp) {
        SimpleDateFormat format = new SimpleDateFormat(temp);
        String dateStr = format.format(date);
        return dateStr;
    }

    /**
     * 将字符串转换为Date对象。
     *
     * @param dateStr   日期字符串
     * @param formatStr 日期格式，如"yyyy-MM-dd"
     * @return 解析后的Date对象，如果解析失败返回null
     */
    public static Date strToDate(String dateStr, String formatStr) {
        Date date = null;
        try {
            SimpleDateFormat format = new SimpleDateFormat(formatStr);
            date = format.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    /**
     * 将字符串转换为Date对象后，返回毫秒时间戳。
     *
     * @param dateStr   日期字符串
     * @param formatStr 日期格式，如"yyyy-MM-dd"
     * @return 毫秒时间戳，解析失败返回0
     */
    public static long strToDateMillisTimestamp(String dateStr, String formatStr) {
        Date date = strToDate(dateStr, formatStr);
        return date != null ? date.getTime() : 0;
    }

    /**
     * 将字符串转换为Date对象后，返回秒级时间戳。
     *
     * @param dateStr   日期字符串
     * @param formatStr 日期格式，如"yyyy-MM-dd"
     * @return 秒级时间戳，解析失败返回0
     */
    public static int strToDateTimestamp(String dateStr, String formatStr) {
        Date date = strToDate(dateStr, formatStr);
        return date != null ? (int) (date.getTime() / 1000L) : 0;
    }

    /**
     * 计算两个日期之间相差的天数。
     *
     * @param date1  第一个日期字符串
     * @param date2  第二个日期字符串
     * @param format 日期格式，如"yyyy-MM-dd"
     * @return 相差的天数（date1 - date2）
     * @throws Exception 如果日期解析失败则抛出异常
     */
    public static long calcDay(String date1, String date2, String format) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        Calendar cal = Calendar.getInstance();
        cal.setTime(sdf.parse(date1));
        long time1 = cal.getTimeInMillis();
        cal.setTime(sdf.parse(date2));
        long time2 = cal.getTimeInMillis();
        return (time1 - time2) / (1000 * 3600 * 24);
    }

    /**
     * 比较两个Date类型的日期大小。
     *
     * @param sDate 开始日期
     * @param eDate 结束日期
     * @return 比较结果：0-相同，1-前者大，-1或2-后者大
     */
    public static int compareDate(Date sDate, Date eDate) {
        Calendar sC = Calendar.getInstance();
        sC.setTime(sDate);
        Calendar eC = Calendar.getInstance();
        eC.setTime(eDate);
        return sC.compareTo(eC);
    }

    /**
     * 比较两个String类型的日期大小。
     *
     * @param sDate     开始日期字符串
     * @param eDate     结束日期字符串
     * @param formatStr 日期格式，如"yyyy-MM-dd"
     * @return 比较结果：0-相同，1-前者大，-1-后者大
     */
    public static int compareDate(String sDate, String eDate, String formatStr) {
        Date startDate = strToDate(sDate, formatStr);
        Date endDate = strToDate(eDate, formatStr);
        return compareDate(startDate, endDate);
    }
}
