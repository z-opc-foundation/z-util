package com.zifang.util.doc.xlsx;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/** xlsx 底层工具：zip 容器读写、单元格坐标、Excel 日期序列号、日期格式判定。 */
final class XlsxSupport {

    /** Excel 内置日期/时间 numFmtId（14-22、27-36、45-47、50-58）。 */
    private static final int[] BUILTIN_DATE_FMT = {
            14, 15, 16, 17, 18, 19, 20, 21, 22,
            27, 28, 29, 30, 31, 32, 33, 34, 35, 36,
            45, 46, 47, 50, 51, 52, 53, 54, 55, 56, 57, 58};

    private XlsxSupport() {
    }

    static Map<String, byte[]> readZip(byte[] data) throws IOException {
        return com.zifang.util.doc.zip.ZipSupport.read(data);
    }

    static byte[] writeZip(Map<String, byte[]> entries) throws IOException {
        return com.zifang.util.doc.zip.ZipSupport.write(entries);
    }

    /** "B3" → 列号 1（0 起）。 */
    static int columnOf(String cellRef) {
        int col = 0;
        for (int i = 0; i < cellRef.length(); i++) {
            char c = cellRef.charAt(i);
            if (c >= 'A' && c <= 'Z') {
                col = col * 26 + (c - 'A' + 1);
            } else if (c >= 'a' && c <= 'z') {
                col = col * 26 + (c - 'a' + 1);
            } else {
                break;
            }
        }
        return Math.max(0, col - 1);
    }

    /** "B3" → 行号 3（1 起）。 */
    static int rowOf(String cellRef) {
        int i = 0;
        while (i < cellRef.length() && Character.isLetter(cellRef.charAt(i))) {
            i++;
        }
        if (i >= cellRef.length()) {
            return 1;
        }
        try {
            return Integer.parseInt(cellRef.substring(i));
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    /** 列号 0 起 → "A"/"AA"。 */
    static String columnName(int column) {
        StringBuilder sb = new StringBuilder();
        int c = column + 1;
        while (c > 0) {
            int rem = (c - 1) % 26;
            sb.insert(0, (char) ('A' + rem));
            c = (c - 1) / 26;
        }
        return sb.toString();
    }

    static String cellRef(int row, int column) {
        return columnName(column) + row;
    }

    /**
     * Excel 日期序列号 → 时间。1900 日期系统：1 = 1900-01-01，60 是 Excel 编造的 1900-02-29，
     * 61 起为真实日期，故 61 起用 1899-12-30 为基准，之前用 1899-12-31。
     */
    static LocalDateTime serialToDateTime(double serial) {
        long days = (long) Math.floor(serial);
        double frac = serial - days;
        LocalDateTime base = days >= 61
                ? LocalDateTime.of(1899, 12, 30, 0, 0)
                : LocalDateTime.of(1899, 12, 31, 0, 0);
        return base.plusDays(days).plusNanos(Math.round(frac * 86_400_000_000_000d));
    }

    static double dateTimeToSerial(LocalDateTime dateTime) {
        LocalDateTime base = dateTime.toLocalDate().isBefore(LocalDate.of(1900, 3, 1))
                ? LocalDateTime.of(1899, 12, 31, 0, 0)
                : LocalDateTime.of(1899, 12, 30, 0, 0);
        long days = java.time.temporal.ChronoUnit.DAYS.between(base.toLocalDate(), dateTime.toLocalDate());
        long seconds = java.time.temporal.ChronoUnit.SECONDS.between(base.toLocalTime(), dateTime.toLocalTime());
        return days + seconds / 86400d;
    }

    static boolean isDateFormat(int numFmtId, String formatCode) {
        if (numFmtId >= 14 && numFmtId <= 22) {
            return true;
        }
        if (numFmtId >= 27 && numFmtId <= 36) {
            return true;
        }
        if (numFmtId >= 45 && numFmtId <= 47) {
            return true;
        }
        if (numFmtId >= 50 && numFmtId <= 58) {
            return true;
        }
        return formatCode != null && looksLikeDateCode(formatCode);
    }

    /** 自定义格式码里出现未加引号的 y/m/d/h/s 即视为日期或时间。 */
    private static boolean looksLikeDateCode(String code) {
        boolean inQuote = false;
        boolean inBracket = false;
        for (int i = 0; i < code.length(); i++) {
            char c = code.charAt(i);
            if (c == '"') {
                inQuote = !inQuote;
            } else if (c == '[') {
                inBracket = true;
            } else if (c == ']') {
                inBracket = false;
            } else if (!inQuote && !inBracket) {
                char lower = Character.toLowerCase(c);
                if (lower == 'y' || lower == 'd') {
                    return true;
                }
                if (lower == 'm' || lower == 'h' || lower == 's') {
                    // m 同时是分钟，hh 才是时间；含 s/h 的多半是时间格式
                    if (lower == 's' || lower == 'h' || code.contains(":") || code.contains(";")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /** 判定是"纯时间"还是"日期时间"。 */
    static boolean isTimeOnly(String formatCode) {
        if (formatCode == null) {
            return false;
        }
        String stripped = formatCode.replaceAll("\"[^\"]*\"", "");
        return !stripped.matches(".*[yYdD].*");
    }

    static String formatDate(LocalDateTime dateTime, boolean timeOnly) {
        if (timeOnly) {
            return dateTime.toLocalTime().withNano(0).toString();
        }
        String date = dateTime.toLocalDate().toString();
        return dateTime.toLocalTime().withNano(0).equals(java.time.LocalTime.MIDNIGHT)
                ? date
                : date + " " + dateTime.toLocalTime().withNano(0);
    }
}
