package com.zifang.util.doc.excel;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Excel 语义共用：1900 日期系统的序列号换算、数字格式是否日期、内置日期格式号判定。
 * xlsx（OOXML）与 xls（BIFF）两套解析器共用这一份，避免两套语义漂移。
 */
public final class ExcelFormats {

    /** Excel 内置日期/时间 numFmtId：14-22、27-36、45-47、50-58。 */
    private static final int[] BUILTIN_DATE_FMT = {
            14, 15, 16, 17, 18, 19, 20, 21, 22,
            27, 28, 29, 30, 31, 32, 33, 34, 35, 36,
            45, 46, 47, 50, 51, 52, 53, 54, 55, 56, 57, 58};

    private ExcelFormats() {
    }

    /**
     * 序列号 → 时间。1900 日期系统：1 = 1900-01-01，60 是 Excel 编造的 1900-02-29，
     * 61 起为真实日期，故 61 起用 1899-12-30 为基准，之前用 1899-12-31。
     */
    public static LocalDateTime serialToDateTime(double serial) {
        long days = (long) Math.floor(serial);
        double frac = serial - days;
        LocalDateTime base = days >= 61
                ? LocalDateTime.of(1899, 12, 30, 0, 0)
                : LocalDateTime.of(1899, 12, 31, 0, 0);
        return base.plusDays(days).plusNanos(Math.round(frac * 86_400_000_000_000d));
    }

    public static double dateTimeToSerial(LocalDateTime dateTime) {
        LocalDateTime base = dateTime.toLocalDate().isBefore(LocalDate.of(1900, 3, 1))
                ? LocalDateTime.of(1899, 12, 31, 0, 0)
                : LocalDateTime.of(1899, 12, 30, 0, 0);
        long days = java.time.temporal.ChronoUnit.DAYS.between(base.toLocalDate(), dateTime.toLocalDate());
        long seconds = java.time.temporal.ChronoUnit.SECONDS.between(base.toLocalTime(), dateTime.toLocalTime());
        return days + seconds / 86400d;
    }

    /** 数字格式的性质。 */
    public enum DateKind {
        NONE, DATE, TIME, DATETIME
    }

    /**
     * 判定数字格式是日期、时间还是都不是。
     * 内置格式号按 Excel 约定判定；自定义格式码扫描未加引号的 y/d/m/h/s 标记，
     * 跳过引号内文字与方括号区段，m 在没有时间标记时算月份。
     */
    public static DateKind classify(int numFmtId, String formatCode) {
        if (isBuiltinDateId(numFmtId)) {
            return isBuiltinTimeId(numFmtId) ? DateKind.TIME : DateKind.DATE;
        }
        if (formatCode == null) {
            return DateKind.NONE;
        }
        boolean hasY = false;
        boolean hasD = false;
        boolean hasH = false;
        boolean hasS = false;
        boolean hasM = false;
        boolean inQuote = false;
        boolean inBracket = false;
        for (int i = 0; i < formatCode.length(); i++) {
            char c = formatCode.charAt(i);
            if (c == '\\') {
                i++;
                continue;
            }
            if (c == '"') {
                inQuote = !inQuote;
                continue;
            }
            if (c == '[') {
                inBracket = true;
                continue;
            }
            if (c == ']') {
                inBracket = false;
                continue;
            }
            if (inQuote || inBracket) {
                continue;
            }
            switch (Character.toLowerCase(c)) {
                case 'y' -> hasY = true;
                case 'd' -> hasD = true;
                case 'h' -> hasH = true;
                case 's' -> hasS = true;
                case 'm' -> hasM = true;
                default -> {
                }
            }
        }
        boolean datePart = hasY || hasD || (hasM && !hasH && !hasS);
        boolean timePart = hasH || hasS;
        if (datePart && timePart) {
            return DateKind.DATETIME;
        }
        if (datePart) {
            return DateKind.DATE;
        }
        if (timePart) {
            return DateKind.TIME;
        }
        return DateKind.NONE;
    }

    public static boolean isDateFormat(int numFmtId, String formatCode) {
        return classify(numFmtId, formatCode) != DateKind.NONE;
    }

    /** 纯时间格式（不含年/月/日）判定。 */
    public static boolean isTimeOnly(int numFmtId, String formatCode) {
        return classify(numFmtId, formatCode) == DateKind.TIME;
    }

    private static boolean isBuiltinDateId(int numFmtId) {
        for (int id : BUILTIN_DATE_FMT) {
            if (id == numFmtId) {
                return true;
            }
        }
        return false;
    }

    /** 内置的纯时间格式号：h:mm、h:mm:ss、mm:ss 等。 */
    private static boolean isBuiltinTimeId(int numFmtId) {
        return switch (numFmtId) {
            case 18, 19, 20, 21, 22, 45, 46, 47 -> true;
            default -> false;
        };
    }

    /** 按日期/时间格式渲染成可读文本。 */
    public static String formatDate(LocalDateTime dateTime, boolean timeOnly) {
        if (timeOnly) {
            return dateTime.toLocalTime().withNano(0).toString();
        }
        String date = dateTime.toLocalDate().toString();
        return dateTime.toLocalTime().withNano(0).equals(java.time.LocalTime.MIDNIGHT)
                ? date
                : date + " " + dateTime.toLocalTime().withNano(0);
    }
}
