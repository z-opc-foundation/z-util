package com.zifang.util.doc.xls;

import com.zifang.util.doc.DocBlock;
import com.zifang.util.doc.excel.ExcelFormats;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * BIFF（Excel 97-2003 二进制）记录流解析。
 * 覆盖：BOUNDSHEET 工作表目录、SST 共享字符串（含 CONTINUE 跨段续接）、LABELSST/LABEL/RK/NUMBER/
 * MULRK/BOOLERR/FORMULA+STRING 单元格、XF 与 FORMAT 数字格式（日期识别）、CODEPAGE 解码。
 * BIFF8 为主，BIFF5/BIFF4 按字节串降级解析。
 */
final class BiffWorkbook {

    // 全局子流记录
    private static final int REC_BOF = 0x0809;
    private static final int REC_EOF = 0x000A;
    private static final int REC_BOUNDSHEET = 0x0085;
    private static final int REC_SST = 0x00FC;
    private static final int REC_CONTINUE = 0x003C;
    private static final int REC_FORMAT = 0x041E;
    private static final int REC_XF = 0x00E0;
    private static final int REC_CODEPAGE = 0x0042;

    // 工作表子流记录
    private static final int REC_LABELSST = 0x00FD;
    private static final int REC_LABEL = 0x0204;
    private static final int REC_LABEL_B2 = 0x0004;
    private static final int REC_NUMBER_B2 = 0x0003;
    private static final int REC_INTEGER_B2 = 0x0002;
    private static final int REC_BOOLERR_B2 = 0x0005;
    private static final int REC_NUMBER = 0x0203;
    private static final int REC_RK = 0x027E;
    private static final int REC_MULRK = 0x00BD;
    private static final int REC_BOOLERR = 0x0205;
    private static final int REC_FORMULA = 0x0006;
    private static final int REC_STRING = 0x0207;

    /** 一个工作表。 */
    static final class Sheet {
        String name;
        int position;
        boolean hidden;
        List<DocBlock.Sheet.SheetRow> rows = new ArrayList<>();
    }

    private final byte[] data;
    private final List<String> sharedStrings = new ArrayList<>();
    private final Map<Integer, String> formatCodes = new HashMap<>();
    private final List<int[]> xfs = new ArrayList<>();        // {formatKey, isStyle, parentStyleIndex}
    private final List<Sheet> sheets = new ArrayList<>();
    private Charset charset = StandardCharsets.ISO_8859_1;
    private int biffVersion = 80;
    private int afterBof;
    private int firstSubstreamType = -1;

    private BiffWorkbook(byte[] data) {
        this.data = data;
    }

    static BiffWorkbook parse(byte[] workbookStream) {
        BiffWorkbook book = new BiffWorkbook(workbookStream);
        int substreamType = book.readFirstBof();
        if (substreamType == 0x0010 || substreamType == 0x0020 || substreamType == 0x0040) {
            // BIFF2-4：整个流就是一张表，没有全局子流与 BOUNDSHEET
            Sheet sheet = new Sheet();
            sheet.name = "Sheet 1";
            book.parseSheetRecords(sheet, book.afterBof);
            book.sheets.add(sheet);
            return book;
        }
        book.readGlobals();
        book.readSheets();
        return book;
    }

    /** 读首个 BOF：版本 + 子流类型（0x0005 全局、0x0010 工作表、0x0020 图表、0x0040 宏）。 */
    private int readFirstBof() {
        if (data.length < 4) {
            return -1;
        }
        int type = u16(0);
        int size = u16(2);
        int start = 4;
        boolean isBof = type == REC_BOF || type == 0x0409 || type == 0x0209 || type == 0x0009;
        if (isBof && start + 4 <= data.length) {
            readBof(start, type);
            firstSubstreamType = u16(start + 2);
            afterBof = Math.min(data.length, start + size);
        } else {
            afterBof = 0;
        }
        return firstSubstreamType;
    }

    List<Sheet> sheets() {
        return sheets;
    }

    // ==================== 全局子流 ====================

    private void readGlobals() {
        int p = afterBof;
        while (p + 4 <= data.length) {
            int type = u16(p);
            int size = u16(p + 2);
            int start = p + 4;
            int end = start + size;
            if (end > data.length) {
                break;
            }
            switch (type) {
                case REC_BOF -> readBof(start, type);
                case REC_CODEPAGE -> charset = charsetOf(u16(start));
                case REC_BOUNDSHEET -> readBoundSheet(start, end);
                case REC_SST -> {
                    p = readSharedStrings(start, end);
                    continue;
                }
                case REC_FORMAT -> readFormat(start, end);
                case REC_XF -> readXf(start, end);
                case REC_EOF -> {
                    return;
                }
                default -> {
                }
            }
            p = end;
        }
    }

    /**
     * BOF：版本与子流类型（0x0005 全局、0x0010 工作表、0x0020 图表、0x0040 宏）。
     * 版本判定与主流实现一致：opcode 高字节是 0x08 时看版本字段，
     * 否则 0x04→BIFF4、0x02→BIFF3、0x00→BIFF2。
     */
    private void readBof(int start, int opcode) {
        if (start + 4 > data.length) {
            return;
        }
        int vers = u16(start);
        if ((opcode >> 8) == 0x08) {
            if (vers == 0x0600) {
                biffVersion = 80;
            } else if (vers == 0x0500) {
                biffVersion = 50;
            } else {
                biffVersion = switch (vers) {
                    case 0x0300 -> 30;
                    case 0x0400 -> 40;
                    default -> 21;
                };
            }
            return;
        }
        biffVersion = switch (opcode >> 8) {
            case 0x04 -> 40;
            case 0x02 -> 30;
            default -> 21;
        };
    }

    private void readBoundSheet(int start, int end) {
        if (end - start < 8) {
            return;
        }
        Sheet sheet = new Sheet();
        sheet.position = u32(start);
        int grbit = u16(start + 4);
        sheet.hidden = (grbit & 0x03) != 0;
        int nameLen = data[start + 6] & 0xFF;
        int nameStart;
        boolean wide;
        if (biffVersion >= 80) {
            int flags = data[start + 7] & 0xFF;
            wide = (flags & 0x01) != 0;
            nameStart = start + 8;
        } else {
            wide = false;
            nameStart = start + 7;
        }
        int nameBytes = wide ? nameLen * 2 : nameLen;
        int nameEnd = Math.min(end, nameStart + nameBytes);
        sheet.name = decodeText(nameStart, nameEnd, wide);
        sheets.add(sheet);
    }

    private void readFormat(int start, int end) {
        if (end - start < 2) {
            return;
        }
        int ifmt = u16(start);
        int[] cursor = {start + 2};
        formatCodes.put(ifmt, readUnicodeString(cursor, end));
    }

    /**
     * XF 记录（BIFF8）：fontIndex(2) + formatKey(2) + 类型位(2) + ...
     * 类型位里 bit2 是 fStyle（1=样式 XF，0=单元格 XF），高位是父样式 XF 索引。
     */
    private void readXf(int start, int end) {
        if (end - start < 8) {
            return;
        }
        int ifnt = u16(start + 2);
        int typeBits = u16(start + 4);
        boolean isStyle = (typeBits & 0x0004) != 0;
        int parent = (typeBits & 0xFFF0) >> 4;
        xfs.add(new int[]{ifnt, isStyle ? 1 : 0, parent});
    }

    /** SST + 后续 CONTINUE 记录；返回新的记录游标位置。 */
    private int readSharedStrings(int start, int end) {
        List<byte[]> segments = new ArrayList<>();
        segments.add(slice(start, end));
        int p = end;
        while (p + 4 <= data.length) {
            int type = u16(p);
            int size = u16(p + 2);
            int segStart = p + 4;
            int segEnd = segStart + size;
            if (segEnd > data.length) {
                break;
            }
            if (type == REC_CONTINUE) {
                segments.add(slice(segStart, segEnd));
                p = segEnd;
                continue;
            }
            break;
        }
        int unique = segments.isEmpty() ? 0 : u32At(segments.get(0), 4);
        SstCursor cursor = new SstCursor(segments);
        cursor.skip(8);
        for (int i = 0; i < unique; i++) {
            String value = cursor.readString();
            sharedStrings.add(value == null ? "" : value);
        }
        return p;
    }

    // ==================== 工作表子流 ====================

    private void readSheets() {
        for (Sheet sheet : sheets) {
            parseSheetRecords(sheet, sheet.position);
        }
    }

    /** 解析一张工作表的记录流（从 start 到 EOF 记录）。 */
    private void parseSheetRecords(Sheet sheet, int startPos) {
        {
            int p = startPos;
            if (p < 0 || p + 4 > data.length) {
                return;
            }
            Map<Integer, TreeMap<Integer, DocBlock.Sheet.SheetCell>> grid = new TreeMap<>();
            int pendingRow = -1;
            int pendingCol = -1;
            sheetLoop:
            while (p + 4 <= data.length) {
                int type = u16(p);
                int size = u16(p + 2);
                int start = p + 4;
                int end = start + size;
                if (end > data.length) {
                    break;
                }
                switch (type) {
                    case REC_LABELSST -> {
                        int isst = u32(start + 6);
                        String text = isst >= 0 && isst < sharedStrings.size() ? sharedStrings.get(isst) : "";
                        put(grid, u16(start), u16(start + 2),
                                new DocBlock.Sheet.SheetCell(u16(start + 2),
                                        DocBlock.Sheet.CellType.STRING, text, null));
                    }
                    case REC_LABEL -> {
                        // 行列之后是 2 字节单元格属性（BIFF8 是 ixfe），随后是字符串
                        int[] cursor = {start + 6};
                        String text = biffVersion >= 80
                                ? readUnicodeString(cursor, end)
                                : readByteString(cursor, end, 2);
                        put(grid, u16(start), u16(start + 2),
                                new DocBlock.Sheet.SheetCell(u16(start + 2),
                                        DocBlock.Sheet.CellType.STRING, text, null));
                    }
                    case REC_LABEL_B2 -> {
                        // BIFF2：行列之后是 3 字节单元格属性，字符串长度 1 字节
                        int[] cursor = {start + 7};
                        String text = readByteString(cursor, end, 1);
                        put(grid, u16(start), u16(start + 2),
                                new DocBlock.Sheet.SheetCell(u16(start + 2),
                                        DocBlock.Sheet.CellType.STRING, text, null));
                    }
                    case REC_INTEGER_B2 -> {
                        int value = (data[start + 7] & 0xFF) | ((data[start + 8] & 0xFF) << 8);
                        put(grid, u16(start), u16(start + 2), new DocBlock.Sheet.SheetCell(u16(start + 2),
                                DocBlock.Sheet.CellType.NUMBER, Integer.toString(value), null));
                    }
                    case REC_NUMBER_B2 -> {
                        double d = readDouble(start + 7);
                        put(grid, u16(start), u16(start + 2), numericCell(u16(start), u16(start + 2), 0, d));
                    }
                    case REC_BOOLERR_B2 -> {
                        int value = data[start + 7] & 0xFF;
                        boolean isError = data[start + 8] != 0;
                        DocBlock.Sheet.SheetCell cell = isError
                                ? new DocBlock.Sheet.SheetCell(u16(start + 2), DocBlock.Sheet.CellType.ERROR,
                                errorText(value), null)
                                : new DocBlock.Sheet.SheetCell(u16(start + 2), DocBlock.Sheet.CellType.BOOLEAN,
                                value != 0 ? "TRUE" : "FALSE", null);
                        put(grid, u16(start), u16(start + 2), cell);
                    }
                    case REC_NUMBER -> {
                        double d = readDouble(start + 6);
                        put(grid, u16(start), u16(start + 2), numericCell(u16(start), u16(start + 2), u16(start + 4), d));
                    }
                    case REC_RK -> {
                        double d = decodeRk(u32(start + 6));
                        put(grid, u16(start), u16(start + 2), numericCell(u16(start), u16(start + 2), u16(start + 4), d));
                    }
                    case REC_MULRK -> {
                        int colFirst = u16(start + 2);
                        int colLast = u16(end - 2);
                        int p2 = start + 4;
                        for (int col = colFirst; col <= colLast && p2 + 6 <= end - 2; col++) {
                            int xf = u16(p2);
                            double d = decodeRk(u32(p2 + 2));
                            put(grid, u16(start), col, numericCell(u16(start), col, xf, d));
                            p2 += 6;
                        }
                    }
                    case REC_BOOLERR -> {
                        int value = data[start + 6] & 0xFF;
                        boolean isError = data[start + 7] != 0;
                        DocBlock.Sheet.SheetCell cell = isError
                                ? new DocBlock.Sheet.SheetCell(u16(start + 2), DocBlock.Sheet.CellType.ERROR,
                                errorText(value), null)
                                : new DocBlock.Sheet.SheetCell(u16(start + 2), DocBlock.Sheet.CellType.BOOLEAN,
                                value != 0 ? "TRUE" : "FALSE", null);
                        put(grid, u16(start), u16(start + 2), cell);
                    }
                    case REC_FORMULA -> {
                        int row = u16(start);
                        int col = u16(start + 2);
                        int xf = u16(start + 4);
                        // 缓存值 8 字节：数值时是 double，特殊值时后两字节为 0xFF
                        boolean special = (data[start + 12] & 0xFF) == 0xFF && (data[start + 13] & 0xFF) == 0xFF;
                        if (special) {
                            int marker = data[start + 6] & 0xFF;
                            if (marker == 0) {
                                // 字符串缓存值在紧随其后的 STRING 记录里（零长文本则没有该记录）
                                put(grid, row, col, new DocBlock.Sheet.SheetCell(col,
                                        DocBlock.Sheet.CellType.STRING, "", null));
                                pendingRow = row;
                                pendingCol = col;
                            } else if (marker == 1) {
                                put(grid, row, col, new DocBlock.Sheet.SheetCell(col,
                                        DocBlock.Sheet.CellType.BOOLEAN,
                                        (data[start + 8] & 0xFF) != 0 ? "TRUE" : "FALSE", null));
                            } else if (marker == 2) {
                                put(grid, row, col, new DocBlock.Sheet.SheetCell(col,
                                        DocBlock.Sheet.CellType.ERROR, errorText(data[start + 8] & 0xFF), null));
                            } else if (marker == 3) {
                                // 空字符串
                                put(grid, row, col, new DocBlock.Sheet.SheetCell(col,
                                        DocBlock.Sheet.CellType.STRING, "", null));
                            }
                        } else {
                            double d = readDouble(start + 6);
                            put(grid, row, col, numericCell(u16(start), col, xf, d));
                        }
                    }
                    case REC_STRING -> {
                        int[] cursor = {start};
                        String text = readUnicodeString(cursor, end);
                        if (pendingRow >= 0) {
                            put(grid, pendingRow, pendingCol,
                                    new DocBlock.Sheet.SheetCell(pendingCol,
                                            DocBlock.Sheet.CellType.STRING, text, null));
                            pendingRow = -1;
                            pendingCol = -1;
                        }
                    }
                    case REC_EOF -> {
                        p = end;
                        break sheetLoop;
                    }
                    default -> {
                    }
                }
                p = end;
            }
            for (Map.Entry<Integer, TreeMap<Integer, DocBlock.Sheet.SheetCell>> row : grid.entrySet()) {
                // BIFF 行号从 0 起，模型统一用 Excel 的 1 起行号
                sheet.rows.add(new DocBlock.Sheet.SheetRow(row.getKey() + 1, new ArrayList<>(row.getValue().values())));
            }
        }
    }

    private DocBlock.Sheet.SheetCell numericCell(int row, int column, int xf, double value) {
        int ifmt = formatIndexOf(xf);
        String code = formatCodes.get(ifmt);
        if (ExcelFormats.isDateFormat(ifmt, code)) {
            LocalDateTime dateTime = ExcelFormats.serialToDateTime(value);
            boolean timeOnly = ExcelFormats.isTimeOnly(ifmt, code);
            return new DocBlock.Sheet.SheetCell(column,
                    timeOnly ? DocBlock.Sheet.CellType.TIME : DocBlock.Sheet.CellType.DATE,
                    ExcelFormats.formatDate(dateTime, timeOnly), null);
        }
        return new DocBlock.Sheet.SheetCell(column, DocBlock.Sheet.CellType.NUMBER, numberText(value), null);
    }

    /** 整数值走整数文本，超过 2^53 用双精度文本避免精度丢失。 */
    private static String numberText(double value) {
        if (value == Math.rint(value) && Math.abs(value) < 9.007199254740992E15 && !Double.isInfinite(value)) {
            return Long.toString((long) value);
        }
        return Double.toString(value);
    }

    /**
     * 单元格的数字格式索引。单元格记录的 xf 下标指向完整 XF 数组（样式 XF 在前）；
     * 格式为 General(0) 时按 Excel 语义继承父样式 XF 的格式。
     */
    private int formatIndexOf(int xfIndex) {
        int index = xfIndex;
        for (int guard = 0; guard < 16; guard++) {
            if (index < 0 || index >= xfs.size()) {
                return 0;
            }
            int[] xf = xfs.get(index);
            if (xf[0] != 0) {
                return xf[0];
            }
            int parent = xf[2];
            if (parent == 0xFFF || parent == index) {
                return 0;
            }
            index = parent;
        }
        return 0;
    }

    private void put(Map<Integer, TreeMap<Integer, DocBlock.Sheet.SheetCell>> grid,
                     int row, int column, DocBlock.Sheet.SheetCell cell) {
        grid.computeIfAbsent(row, k -> new TreeMap<>()).put(column, cell);
    }

    // ==================== 底层读取 ====================

    private byte[] slice(int start, int end) {
        byte[] out = new byte[end - start];
        System.arraycopy(data, start, out, 0, out.length);
        return out;
    }

    private static int u32At(byte[] bytes, int off) {
        return (bytes[off] & 0xFF) | ((bytes[off + 1] & 0xFF) << 8)
                | ((bytes[off + 2] & 0xFF) << 16) | ((bytes[off + 3] & 0xFF) << 24);
    }

    private int u16(int off) {
        return (data[off] & 0xFF) | ((data[off + 1] & 0xFF) << 8);
    }

    private int u32(int off) {
        return (data[off] & 0xFF) | ((data[off + 1] & 0xFF) << 8)
                | ((data[off + 2] & 0xFF) << 16) | ((data[off + 3] & 0xFF) << 24);
    }

    private double readDouble(int off) {
        long bits = 0;
        for (int i = 7; i >= 0; i--) {
            bits = (bits << 8) | (data[off + i] & 0xFF);
        }
        return Double.longBitsToDouble(bits);
    }

    /** RK 压缩数字：bit0 = ×100（值要除以 100），bit1 = 整数（否则是 double 的高 4 字节）。 */
    private static double decodeRk(int rk) {
        double value;
        if ((rk & 0x02) != 0) {
            value = rk >> 2;
        } else {
            long bits = ((long) (rk & 0xFFFFFFFC)) << 32;
            value = Double.longBitsToDouble(bits);
        }
        if ((rk & 0x01) != 0) {
            value /= 100.0;
        }
        return value;
    }

    private static String errorText(int code) {
        return switch (code) {
            case 0x00 -> "#NULL!";
            case 0x07 -> "#DIV/0!";
            case 0x0F -> "#VALUE!";
            case 0x17 -> "#REF!";
            case 0x1D -> "#NAME?";
            case 0x24 -> "#NUM!";
            case 0x2A -> "#N/A";
            default -> "#ERR" + code;
        };
    }

    /**
     * 读一条字符串。BIFF8：cch(2) + flags(1) + 字符（压缩或 UTF-16LE）；
     * BIFF5 及更早：cch(1) + 字节串（按工作簿代码页）。
     */
    private String readUnicodeString(int[] cursor, int end) {
        if (cursor[0] + 3 > end) {
            return "";
        }
        int cch = u16(cursor[0]);
        int flags = data[cursor[0] + 2] & 0xFF;
        cursor[0] += 3;
        boolean wide = (flags & 0x01) != 0;
        int byteLen = cch * (wide ? 2 : 1);
        int stop = Math.min(end, cursor[0] + byteLen);
        String text = decodeText(cursor[0], stop, wide);
        cursor[0] = stop;
        return text;
    }

    /** BIFF5 及更早的字节串：长度前缀 1 或 2 字节，按工作簿代码页解码。 */
    private String readByteString(int[] cursor, int end, int lengthBytes) {
        if (lengthBytes == 2) {
            if (cursor[0] + 2 > end) {
                return "";
            }
            int cch = u16(cursor[0]);
            cursor[0] += 2;
            int stop = Math.min(end, cursor[0] + cch);
            String text = decodeText(cursor[0], stop, false);
            cursor[0] = stop;
            return text;
        }
        if (cursor[0] >= end) {
            return "";
        }
        int cch = data[cursor[0]] & 0xFF;
        cursor[0] += 1;
        int stop = Math.min(end, cursor[0] + cch);
        String text = decodeText(cursor[0], stop, false);
        cursor[0] = stop;
        return text;
    }

    /**
     * 解码一段字节。BIFF8 的压缩串一律按 latin-1（与主流实现一致）；
     * BIFF5 及更早按工作簿代码页解码。
     */
    private String decodeText(int start, int end, boolean wide) {
        if (end <= start) {
            return "";
        }
        if (wide) {
            return new String(data, start, end - start, StandardCharsets.UTF_16LE);
        }
        Charset cs = biffVersion >= 80 ? StandardCharsets.ISO_8859_1 : charset;
        return new String(data, start, end - start, cs);
    }

    private static Charset charsetOf(int codepage) {
        return switch (codepage) {
            case 1200 -> StandardCharsets.UTF_16LE;
            case 1252 -> StandardCharsets.ISO_8859_1;
            case 65001 -> StandardCharsets.UTF_8;
            default -> Charset.forName(charsetName(codepage));
        };
    }

    private static String charsetName(int codepage) {
        return switch (codepage) {
            case 10000 -> "MacRoman";
            case 936 -> "GBK";
            case 950 -> "Big5";
            case 932 -> "Shift_JIS";
            case 949 -> "EUC-KR";
            case 1251 -> "windows-1251";
            case 1250 -> "windows-1250";
            case 1253 -> "windows-1253";
            case 1254 -> "windows-1254";
            case 1255 -> "windows-1255";
            case 1256 -> "windows-1256";
            case 1257 -> "windows-1257";
            case 1258 -> "windows-1258";
            default -> "windows-1252";
        };
    }

    /**
     * SST 游标：跨 CONTINUE 记录读字符串。
     * 规则：字符数据被 CONTINUE 切断时，新段首字节是该串剩余部分的压缩标志；
     * 若恰好在串边界断开，新段首字节是下一个串的 flags。
     */
    private static final class SstCursor {
        private final List<byte[]> segments;
        private int segIndex;
        private int pos;

        SstCursor(List<byte[]> segments) {
            this.segments = segments;
        }

        void skip(int bytes) {
            pos += bytes;
        }

        private byte[] current() {
            return segments.get(segIndex);
        }

        private boolean atSegmentEnd() {
            return pos >= current().length;
        }

        private void advance() {
            if (segIndex + 1 < segments.size()) {
                segIndex++;
                pos = 0;
            }
        }

        /** 读下一个字符串；返回 null 表示数据已耗尽。 */
        String readString() {
            if (segIndex >= segments.size()) {
                return null;
            }
            if (atSegmentEnd()) {
                advance();
                if (segIndex >= segments.size()) {
                    return null;
                }
                // 串边界处断开的 CONTINUE：首字节是本串的 flags
                int flags = current()[pos++] & 0xFF;
                return readStringBody(flags);
            }
            byte[] seg = current();
            if (pos + 3 > seg.length) {
                // cch 跨段：罕见，按整体剩余读取
                int cch = 0;
                int need = 3 - (seg.length - pos);
                for (int i = 0; i < need && pos < seg.length; i++) {
                    cch |= (seg[pos++] & 0xFF) << (8 * i);
                }
                int flags = 0;
                if (atSegmentEnd()) {
                    advance();
                    if (segIndex < segments.size()) {
                        flags = current()[pos++] & 0xFF;
                    }
                }
                return readStringBody(cch, flags);
            }
            int cch = (seg[pos] & 0xFF) | ((seg[pos + 1] & 0xFF) << 8);
            int flags = seg[pos + 2] & 0xFF;
            pos += 3;
            return readStringBody(cch, flags);
        }

        private String readStringBody(int flags) {
            byte[] seg = current();
            if (pos + 2 > seg.length) {
                return "";
            }
            int cch = (seg[pos] & 0xFF) | ((seg[pos + 1] & 0xFF) << 8);
            pos += 2;
            return readStringBody(cch, flags);
        }

        private String readStringBody(int cch, int flags) {
            boolean wide = (flags & 0x01) != 0;
            boolean rich = (flags & 0x08) != 0;
            boolean ext = (flags & 0x04) != 0;
            int cRun = 0;
            int cbExt = 0;
            if (rich) {
                if (pos + 2 > current().length) {
                    return "";
                }
                cRun = (current()[pos] & 0xFF) | ((current()[pos + 1] & 0xFF) << 8);
                pos += 2;
            }
            if (ext) {
                if (pos + 4 > current().length) {
                    return "";
                }
                cbExt = (current()[pos] & 0xFF) | ((current()[pos + 1] & 0xFF) << 8)
                        | ((current()[pos + 2] & 0xFF) << 16) | ((current()[pos + 3] & 0xFF) << 24);
                pos += 4;
            }
            StringBuilder sb = new StringBuilder();
            int left = cch;
            boolean compressed = !wide;
            while (left > 0) {
                if (segIndex >= segments.size()) {
                    break;
                }
                byte[] seg = current();
                if (pos >= seg.length) {
                    advance();
                    if (segIndex >= segments.size()) {
                        break;
                    }
                    // CONTINUE 首字节是剩余部分的压缩标志
                    compressed = (current()[pos++] & 0xFF) == 0;
                    continue;
                }
                int avail = (seg.length - pos) / (compressed ? 1 : 2);
                int take = Math.min(avail, left);
                if (compressed) {
                    sb.append(new String(seg, pos, take, StandardCharsets.ISO_8859_1));
                    pos += take;
                } else {
                    sb.append(new String(seg, pos, take * 2, StandardCharsets.UTF_16LE));
                    pos += take * 2;
                }
                left -= take;
            }
            // 跳过富文本运行与扩展数据
            skipBytes(cRun * 4);
            skipBytes(cbExt);
            return sb.toString();
        }

        private void skipBytes(int count) {
            while (count > 0 && segIndex < segments.size()) {
                byte[] seg = current();
                int avail = seg.length - pos;
                if (avail <= 0) {
                    advance();
                    if (segIndex >= segments.size()) {
                        return;
                    }
                    continue;
                }
                int take = Math.min(avail, count);
                pos += take;
                count -= take;
                if (pos >= seg.length) {
                    advance();
                }
            }
        }
    }
}
