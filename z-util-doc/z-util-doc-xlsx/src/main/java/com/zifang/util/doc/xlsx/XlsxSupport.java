package com.zifang.util.doc.xlsx;

import com.zifang.util.doc.excel.ExcelFormats;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

/** xlsx 底层工具：zip 容器读写、单元格坐标；日期语义复用 ExcelFormats。 */
final class XlsxSupport {

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

    static LocalDateTime serialToDateTime(double serial) {
        return ExcelFormats.serialToDateTime(serial);
    }

    static double dateTimeToSerial(LocalDateTime dateTime) {
        return ExcelFormats.dateTimeToSerial(dateTime);
    }

    static boolean isDateFormat(int numFmtId, String formatCode) {
        return ExcelFormats.isDateFormat(numFmtId, formatCode);
    }

    static boolean isTimeOnly(int numFmtId, String formatCode) {
        return ExcelFormats.isTimeOnly(numFmtId, formatCode);
    }

    static String formatDate(LocalDateTime dateTime, boolean timeOnly) {
        return ExcelFormats.formatDate(dateTime, timeOnly);
    }
}
