package com.zifang.util.pandas.time;

import com.zifang.util.pandas.num.Index;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * DateTimeIndex 类 - 时间序列索引
 * 对标 pandas DateTimeIndex
 */
public class DateTimeIndex extends Index {

    private final List<LocalDateTime> dates;
    private final String freq;
    private final DateTimeFormatter formatter;

    // ==================== 静态工厂方法 ====================

    /**
     * DateTimeIndex方法。
     * * @param dates ListLocalDateTime类型参数
     *
     * @param freq      String类型参数
     * @param formatter DateTimeFormatter类型参数
     */
    public DateTimeIndex(List<LocalDateTime> dates, String freq, DateTimeFormatter formatter) {
        super(generateLabels(dates, formatter));
        this.dates = new ArrayList<>(dates);
        this.freq = freq;
        this.formatter = formatter != null ? formatter : DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    }

    /**
     * DateTimeIndex方法。
     * * @param dates ListLocalDateTime类型参数
     */
    public DateTimeIndex(List<LocalDateTime> dates) {
        this(dates, null, null);
    }

    /**
     * DateTimeIndex方法。
     * * @param dateStrings String[]类型参数
     */
    public DateTimeIndex(String[] dateStrings) {
        super(dateStrings);
        this.dates = new ArrayList<>();
        for (String s : dateStrings) {
            this.dates.add(parseDateTime(s));
        }
        this.freq = null;
        this.formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    }

    /**
     * 创建日期范围
     * 对标 pandas.date_range
     */
    public static DateTimeIndex dateRange(String start, String end, String freq) {
        LocalDateTime startDate = parseDateTime(start);
        LocalDateTime endDate = parseDateTime(end);
        ChronoUnit unit = parseFreq(freq);

        List<LocalDateTime> dates = new ArrayList<>();
        LocalDateTime current = startDate;
        while (!current.isAfter(endDate)) {
            dates.add(current);
            current = current.plus(1, unit);
        }

        return new DateTimeIndex(dates, freq, null);
    }

    // ==================== 构造函数 ====================

    /**
     * dateRange方法。
     * * @param start String类型参数
     *
     * @param end String类型参数
     * @return static DateTimeIndex类型返回值
     */
    public static DateTimeIndex dateRange(String start, String end) {
        return dateRange(start, end, "D");
    }

    /**
     * 创建期间范围
     * 对标 pandas.period_range
     */
    public static DateTimeIndex periodRange(String start, String end, String freq) {
        return dateRange(start, end, freq);
    }

    /**
     * 创建时间戳范围
     * 对标 pandas.timedelta_range
     */
    public static DateTimeIndex timedeltaRange(String start, String end, String freq) {
        return dateRange(start, end, freq);
    }

    // ==================== 核心方法 ====================

    private static LocalDateTime parseDateTime(String str) {
        // 尝试不同的格式
        DateTimeFormatter[] formatters = {
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy/MM/dd"),
                DateTimeFormatter.ofPattern("dd-MM-yyyy"),
                DateTimeFormatter.ISO_DATE_TIME,
                DateTimeFormatter.ISO_LOCAL_DATE_TIME,
                DateTimeFormatter.ISO_LOCAL_DATE
        };

        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDateTime.parse(str, formatter);
            } catch (Exception e) {
                // 尝试解析为日期（没有时间）
                try {
                    return LocalDateTime.parse(str + " 00:00:00", formatters[0]);
                } catch (Exception e2) {
                    // 继续尝试下一个格式
                }
            }
        }

        throw new IllegalArgumentException("Unable to parse date: " + str);
    }

    private static ChronoUnit parseFreq(String freq) {
        if (freq == null) return ChronoUnit.DAYS;

        switch (freq.toUpperCase()) {
            case "Y":
            case "YEAR":
            case "YEARS":
            case "A":
            case "ANNUAL":
                return ChronoUnit.YEARS;
            case "M":
            case "MONTH":
            case "MONTHS":
            case "MONTHLY":
                return ChronoUnit.MONTHS;
            case "W":
            case "WEEK":
            case "WEEKS":
            case "WEEKLY":
                return ChronoUnit.WEEKS;
            case "D":
            case "DAY":
            case "DAYS":
            case "DAILY":
                return ChronoUnit.DAYS;
            case "H":
            case "HOUR":
            case "HOURS":
            case "HOURLY":
                return ChronoUnit.HOURS;
            case "MIN":
            case "MINUTE":
            case "MINUTES":
                return ChronoUnit.MINUTES;
            case "S":
            case "SEC":
            case "SECOND":
            case "SECONDS":
            case "SECONDLY":
                return ChronoUnit.SECONDS;
            default:
                return ChronoUnit.DAYS;
        }
    }

    private static String[] generateLabels(List<LocalDateTime> dates, DateTimeFormatter formatter) {
        if (formatter == null) {
            formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        }
        DateTimeFormatter finalFormatter = formatter;
        return dates.stream()
                .map(d -> d.format(finalFormatter))
                .toArray(String[]::new);
    }

    /**
     * getDate方法。
     * * @param i int类型参数
     *
     * @return LocalDateTime类型返回值
     */
    public LocalDateTime getDate(int i) {
        return dates.get(i);
    }

    // ==================== 时间操作 ====================

    /**
     * getDates方法。
     *
     * @return List<LocalDateTime>类型返回值
     */
    public List<LocalDateTime> getDates() {
        return new ArrayList<>(dates);
    }

    /**
     * getFreq方法。
     *
     * @return String类型返回值
     */
    public String getFreq() {
        return freq;
    }

    /**
     * size方法。
     *
     * @return int类型返回值
     */
    public int size() {
        return dates.size();
    }

    /**
     * 获取年份
     */
    public int[] year() {
        return dates.stream().mapToInt(LocalDateTime::getYear).toArray();
    }

    /**
     * 获取月份
     */
    public int[] month() {
        return dates.stream().mapToInt(LocalDateTime::getMonthValue).toArray();
    }

    /**
     * 获取日
     */
    public int[] day() {
        return dates.stream().mapToInt(LocalDateTime::getDayOfMonth).toArray();
    }

    /**
     * 获取小时
     */
    public int[] hour() {
        return dates.stream().mapToInt(LocalDateTime::getHour).toArray();
    }

    /**
     * 获取分钟
     */
    public int[] minute() {
        return dates.stream().mapToInt(LocalDateTime::getMinute).toArray();
    }

    /**
     * 获取秒
     */
    public int[] second() {
        return dates.stream().mapToInt(LocalDateTime::getSecond).toArray();
    }

    // ==================== 切片和选择 ====================

    /**
     * 获取星期几
     */
    public int[] dayofweek() {
        return dates.stream().mapToInt(d -> d.getDayOfWeek().getValue()).toArray();
    }

    /**
     * 获取年中第几天
     */
    public int[] dayofyear() {
        return dates.stream().mapToInt(d -> d.getDayOfYear()).toArray();
    }

    // ==================== 转换 ====================

    /**
     * 获取季度
     */
    public int[] quarter() {
        return dates.stream().mapToInt(d -> (d.getMonthValue() - 1) / 3 + 1).toArray();
    }

    /**
     * 按日期范围切片
     */
    public DateTimeIndex slice(String start, String end) {
        LocalDateTime startDate = parseDateTime(start);
        LocalDateTime endDate = parseDateTime(end);

        List<LocalDateTime> sliced = new ArrayList<>();
        for (LocalDateTime date : dates) {
            if (!date.isBefore(startDate) && !date.isAfter(endDate)) {
                sliced.add(date);
            }
        }

        return new DateTimeIndex(sliced, freq, formatter);
    }

    // ==================== 静态辅助方法 ====================

    /**
     * 按年份切片
     */
    public DateTimeIndex sliceYear(int year) {
        List<LocalDateTime> sliced = new ArrayList<>();
        for (LocalDateTime date : dates) {
            if (date.getYear() == year) {
                sliced.add(date);
            }
        }
        return new DateTimeIndex(sliced, freq, formatter);
    }

    /**
     * toStringArray方法。
     *
     * @return String[]类型返回值
     */
    public String[] toStringArray() {
        return dates.stream()
                .map(d -> d.format(formatter))
                .toArray(String[]::new);
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("DateTimeIndex(['");
        sb.append(String.join("', '", toStringArray()));
        sb.append("']");
        if (freq != null) {
            sb.append(", dtype='datetime64[ns]', freq='").append(freq).append("'");
        }
        sb.append(")");
        return sb.toString();
    }
}
