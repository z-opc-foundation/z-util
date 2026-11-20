package com.zifang.util.doc.rtf;

import com.zifang.util.doc.DocBlock;
import com.zifang.util.doc.DocDocument;
import com.zifang.util.doc.DocFormat;
import com.zifang.util.doc.DocFormatDetector;
import com.zifang.util.doc.DocReader;
import com.zifang.util.doc.DocSpan;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Set;

/**
 * RTF 读取器。零第三方依赖：字符级解析控制字与分组。
 * 覆盖：段落切分（\par / \pard / \line）、粗斜删除线（\b \i \strike）、Unicode 转义（U+ 形式）、
 * ANSI 十六进制转义（\'xx）、制表符、字体表/颜色表/样式表/信息组/图片等目的地跳过。
 */
public final class RtfDocReader implements DocReader {

    /** 需要整段跳过的目的地（控制字后跟这些目的地名）。 */
    private static final Set<String> SKIP_DESTINATIONS = Set.of(
            "fonttbl", "colortbl", "stylesheet", "info", "object", "themedata",
            "colorschememapping", "latentstyles", "datastore", "listtable", "listoverridetable",
            "rsidtbl", "generator", "filetbl", "xmlnstbl", "mmathPr", "wgrffmtfilter",
            "pn", "falt", "bkmkstart", "bkmkend",
            "atnid", "atnauthor", "annotation", "atrfstart", "atrfend", "atndate",
            "atnref", "atntime", "atnicn", "chatn", "company", "operator");

    @Override
    public DocFormat format() {
        return DocFormat.RTF;
    }

    @Override
    public boolean supports(byte[] data) {
        return DocFormatDetector.isRtf(data);
    }

    @Override
    public DocDocument read(byte[] data) throws IOException {
        String rtf = new String(data, StandardCharsets.ISO_8859_1);
        DocDocument doc = new DocDocument();
        Parser parser = new Parser(rtf, doc);
        parser.parse();
        return doc;
    }

    /** 解析状态：段落样式栈 + 跳过深度。 */
    private static final class State {
        boolean bold;
        boolean italic;
        boolean strike;
        boolean destinationSkip;
        /** 当前组是否在捕获列表标签（\listtext 目的地）。 */
        boolean listTextCapture;
    }

    private static final class Parser {

        private final String src;
        private final DocDocument doc;
        private int pos;

        /** 字符样式状态栈（分组嵌套）。 */
        private final Deque<State> states = new ArrayDeque<>();
        private State state = new State();
        private final List<DocSpan> spans = new ArrayList<>();

        /** 列表标签文本（\listtext 目的地里的编号/符号）与层级。 */
        private final StringBuilder listLabel = new StringBuilder();
        private int listLevel;

        /** 表格状态：正在收集的行、当前行的单元格、当前单元格的段落。 */
        private final List<DocBlock.Table.Row> tableRows = new ArrayList<>();
        private final List<DocBlock.Table.Cell> rowCells = new ArrayList<>();
        private final List<DocSpan> cellSpans = new ArrayList<>();
        private boolean inTableRow;

        /** 图片捕获状态：\pict 组内的十六进制/原始字节，组关闭时整体产出。 */
        private final java.io.ByteArrayOutputStream pictBytes = new java.io.ByteArrayOutputStream();
        private final List<DocBlock.Image> images = new ArrayList<>();
        private int pictNibble = -1;
        private String pictMime;
        /** 当前组嵌套深度与 \pict 捕获组的起始深度（-1 = 不在捕获中）。 */
        private int groupDepth;
        private int pictStartDepth = -1;

        Parser(String src, DocDocument doc) {
            this.src = src;
            this.doc = doc;
            states.push(state);
        }

        /** 进入分组：活动状态换成组内副本，'}' 时退栈恢复外层状态。 */
        private void pushGroup() {
            states.push(copyOf(state));
            state = states.peek();
        }

        private void popGroup() {
            if (states.size() > 1) {
                states.pop();
                state = states.peek();
            }
        }

        void parse() {
            while (pos < src.length()) {
                char c = src.charAt(pos);
                if (c == '{') {
                    pos++;
                    groupDepth++;
                    pushGroup();
                    continue;
                }
                if (c == '}') {
                    pos++;
                    groupDepth--;
                    popGroup();
                    if (pictStartDepth >= 0 && groupDepth < pictStartDepth) {
                        finalizePict();
                    }
                    continue;
                }
                if (c == '\\') {
                    readControl();
                    continue;
                }
                if (c == '\r' || c == '\n') {
                    pos++;
                    continue;
                }
                append(c);
                pos++;
            }
            flushParagraph();
            flushTable();
            if (pictStartDepth >= 0) {
                finalizePict();
            }
            for (DocBlock.Image image : images) {
                doc.add(image);
            }
        }

        private State copyOf(State s) {
            State copy = new State();
            copy.bold = s.bold;
            copy.italic = s.italic;
            copy.strike = s.strike;
            copy.destinationSkip = s.destinationSkip;
            copy.listTextCapture = s.listTextCapture;
            return copy;
        }

        private void readControl() {
            pos++; // 跳过反斜杠
            if (pos >= src.length()) {
                return;
            }
            char c = src.charAt(pos);
            if (c == '\'') {
                // \'xx ANSI 十六进制转义
                if (pos + 2 < src.length()) {
                    int value = hex(src.charAt(pos + 1)) * 16 + hex(src.charAt(pos + 2));
                    append((char) value);
                }
                pos += 3;
                return;
            }
            if (c == '*') {
                // \*\destination：未知目的地整组跳过
                state.destinationSkip = true;
                pos++;
                return;
            }
            if (c == '\\' || c == '{' || c == '}') {
                append(c);
                pos++;
                return;
            }
            if (!Character.isLetter(c)) {
                // 控制符号：\par \tab \line 等
                readSymbol(c);
                pos++;
                return;
            }
            int start = pos;
            while (pos < src.length() && Character.isLetter(src.charAt(pos))) {
                pos++;
            }
            String word = src.substring(start, pos);
            long param = readOptionalParam();
            // 控制字后的单个空格是分隔符，不算正文
            if (pos < src.length() && src.charAt(pos) == ' ') {
                pos++;
            }
            applyControl(word, param);
        }

        /** 控制符号（单字符，非字母开头）。 */
        private void readSymbol(char c) {
            switch (c) {
                case '\t' -> append("\t");
                case '\n' -> append("\n");
                case '~' -> append("\u00a0");
                case '_' -> append("-");
                case '-' -> {
                }
                case '\\' -> append("\\");
                case '{' -> append("{");
                case '}' -> append("}");
                default -> {
                }
            }
        }

        /** 控制字 + 可选数值参数。 */
        private void applyControl(String word, long param) {
            switch (word) {
                case "par" -> flushParagraph();
                case "trowd" -> beginRow();
                case "cell", "nestcell" -> endCell();
                case "row", "nestrow" -> endRow();
                case "intbl" -> inTableRow = true;
                case "ilvl" -> listLevel = (int) Math.max(0, param);
                case "listtext" -> state.listTextCapture = true;
                case "pntext" -> state.listTextCapture = true;
                case "line" -> append("\n");
                case "tab" -> append("\t");
                case "pard", "sect" -> {
                }
                case "plain" -> {
                    state.bold = false;
                    state.italic = false;
                    state.strike = false;
                }
                case "b" -> state.bold = param != 0;
                case "i" -> state.italic = param != 0;
                case "strike" -> state.strike = param != 0;
                case "ul" -> {
                }
                case "u" -> {
                    // U+ 转义后的替代字符要跳过
                    if (param == Long.MIN_VALUE) {
                        return;
                    }
                    long code = param < 0 ? param + 65536 : param;
                    append(String.valueOf((char) code));
                    if (pos < src.length() && src.charAt(pos) == '?') {
                        pos++;
                    }
                }
                case "uc" -> {
                }
                case "pict" -> beginPict();
                case "nonshppict" -> state.destinationSkip = true; // 旧格式回退副本，避免重复出图
                case "pngblip" -> pictMime = "image/png";
                case "jpegblip" -> pictMime = "image/jpeg";
                case "emfblip" -> pictMime = "image/x-emf";
                case "wmetafile" -> pictMime = "image/x-wmf";
                case "bin" -> {
                    // \binN：其后 N 字节是原始二进制（可能是图片数据）
                    if (param == Long.MIN_VALUE || param <= 0) {
                        return;
                    }
                    int end = (int) Math.min(src.length(), pos + param);
                    if (pictStartDepth >= 0 && state.destinationSkip) {
                        pos = end;
                        return;
                    }
                    if (pictStartDepth >= 0 && end > pos) {
                        byte[] raw = src.substring(pos, end).getBytes(StandardCharsets.ISO_8859_1);
                        pictBytes.write(raw, 0, raw.length);
                    }
                    pos = end;
                }
                case "fonttbl", "colortbl", "stylesheet", "info", "object", "themedata",
                        "colorschememapping", "latentstyles", "datastore", "listtable",
                        "listoverridetable", "rsidtbl", "generator", "filetbl", "xmlnstbl",
                        "mmathPr", "wgrffmtfilter", "pn", "falt",
                        "company", "operator", "chatn" -> state.destinationSkip = true;
                case "header", "footer" -> {
                }
                default -> {
                    if (SKIP_DESTINATIONS.contains(word)) {
                        state.destinationSkip = true;
                    }
                }
            }
        }

        /** 控制字后的可选数值参数（负号 + 数字）。 */
        private long readOptionalParam() {
            int start = pos;
            boolean negative = false;
            if (pos < src.length() && (src.charAt(pos) == '-' || src.charAt(pos) == '+')) {
                negative = src.charAt(pos) == '-';
                pos++;
            }
            int digitStart = pos;
            while (pos < src.length() && Character.isDigit(src.charAt(pos))) {
                pos++;
            }
            if (digitStart == pos) {
                pos = start;
                return Long.MIN_VALUE;
            }
            long value = Long.parseLong(src.substring(digitStart, pos));
            return negative ? -value : value;
        }

        private void append(char c) {
            append(String.valueOf(c));
        }

        private void append(String text) {
            if (state.destinationSkip || text.isEmpty()) {
                return;
            }
            if (pictStartDepth >= 0) {
                for (int i = 0; i < text.length(); i++) {
                    captureHex(text.charAt(i));
                }
                return;
            }
            if (state.listTextCapture) {
                listLabel.append(text);
                return;
            }
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                if (state.destinationSkip) {
                    return;
                }
                DocSpan span = DocSpan.plain(String.valueOf(c));
                if (state.bold) {
                    span = span.withBold();
                }
                if (state.italic) {
                    span = span.withItalic();
                }
                if (state.strike) {
                    span = span.withStrike();
                }
                spans.add(span);
            }
        }

        /** \pict 组开始：记住当前组深度，清空捕获缓冲与类型关键字。 */
        private void beginPict() {
            pictStartDepth = groupDepth;
            pictBytes.reset();
            pictNibble = -1;
            pictMime = null;
        }

        /** \pict 组结束：把捕获到的字节产出为图片块。 */
        private void finalizePict() {
            pictStartDepth = -1;
            byte[] data = pictBytes.toByteArray();
            pictBytes.reset();
            pictNibble = -1;
            if (data.length == 0) {
                return;
            }
            if (pictMime == null) {
                pictMime = sniff(data);
            }
            images.add(new DocBlock.Image("picture", pictMime, data, null, null));
            pictMime = null;
        }

        /** 十六进制流捕获：两位合成一字节，非 hex 字符丢弃当前半字节。 */
        private void captureHex(char c) {
            int value = hexDigit(c);
            if (value < 0) {
                pictNibble = -1;
                return;
            }
            if (pictNibble < 0) {
                pictNibble = value;
                return;
            }
            pictBytes.write(pictNibble * 16 + value);
            pictNibble = -1;
        }

        private static int hexDigit(char c) {
            if (c >= '0' && c <= '9') {
                return c - '0';
            }
            if (c >= 'a' && c <= 'f') {
                return c - 'a' + 10;
            }
            if (c >= 'A' && c <= 'F') {
                return c - 'A' + 10;
            }
            return -1;
        }

        /** 无 blip 关键字时按字节签名猜格式。 */
        private static String sniff(byte[] data) {
            if (data.length >= 4 && (data[0] & 0xFF) == 0x89 && (data[1] & 0xFF) == 'P'
                    && (data[2] & 0xFF) == 'N' && (data[3] & 0xFF) == 'G') {
                return "image/png";
            }
            if (data.length >= 3 && (data[0] & 0xFF) == 0xFF && (data[1] & 0xFF) == 0xD8
                    && (data[2] & 0xFF) == 0xFF) {
                return "image/jpeg";
            }
            if (data.length >= 4 && (data[0] & 0xFF) == 0xD8 && (data[1] & 0xFF) == 0xCD
                    && (data[2] & 0xFF) == 0xC6 && (data[3] & 0xFF) == 0x9A) {
                return "image/x-wmf";
            }
            if (data.length >= 40 && data[40] == ' ' && data[41] == 'E' && data[42] == 'M'
                    && data[43] == 'F') {
                return "image/x-emf";
            }
            return "image/x-wmf";
        }

        private void flushParagraph() {
            if (inTableRow) {
                // 表内段落属于当前单元格
                cellSpans.addAll(spans);
                spans.clear();
                return;
            }
            if (!spans.isEmpty()) {
                StringBuilder text = new StringBuilder();
                for (DocSpan span : spans) {
                    text.append(span.text());
                }
                if (!text.toString().isBlank()) {
                    flushTable();
                    String label = listLabel.toString().strip();
                    if (!label.isEmpty()) {
                        doc.add(new DocBlock.ListItem(!isBullet(label), listLevel,
                                withLabel(merge(spans), label)));
                    } else {
                        doc.add(new DocBlock.Paragraph(merge(spans)));
                    }
                }
                spans.clear();
            }
            listLabel.setLength(0);
            listLevel = 0;
        }

        /** 列表标签作为首个 span 注入，正文照旧。 */
        private List<DocSpan> withLabel(List<DocSpan> body, String label) {
            List<DocSpan> result = new ArrayList<>();
            result.add(DocSpan.plain(label + " "));
            result.addAll(body);
            return result;
        }

        /** 标签是项目符号还是编号。 */
        private static boolean isBullet(String label) {
            char c = label.isEmpty() ? ' ' : label.charAt(0);
            return c == '\u2022' || c == '\u25cf' || c == '\u25cb' || c == '\u00b7'
                    || c == '-' || c == '\u2013' || c == '\u2014' || c == '*' || c == 'o';
        }

        private void beginRow() {
            inTableRow = true;
            rowCells.clear();
            cellSpans.clear();
        }

        private void endCell() {
            cellSpans.addAll(spans);
            spans.clear();
            if (!cellSpans.isEmpty()) {
                rowCells.add(new DocBlock.Table.Cell(
                        List.of(new DocBlock.Paragraph(merge(cellSpans))), 1, 1));
            }
            cellSpans.clear();
        }

        private void endRow() {
            endCell();
            if (!rowCells.isEmpty()) {
                tableRows.add(new DocBlock.Table.Row(new ArrayList<>(rowCells), tableRows.isEmpty()));
            }
            rowCells.clear();
            inTableRow = false;
        }

        /** 累积的行合成一个表格块。 */
        private void flushTable() {
            if (tableRows.isEmpty()) {
                return;
            }
            doc.add(new DocBlock.Table(new ArrayList<>(tableRows)));
            tableRows.clear();
        }

        /** 相邻同样格式的 span 合并，避免每字符一个 span。 */
        private List<DocSpan> merge(List<DocSpan> input) {
            List<DocSpan> result = new ArrayList<>();
            for (DocSpan span : input) {
                if (!result.isEmpty()) {
                    DocSpan last = result.get(result.size() - 1);
                    if (last.bold() == span.bold() && last.italic() == span.italic()
                            && last.strike() == span.strike() && last.mono() == span.mono()
                            && last.link() == null && span.link() == null) {
                        result.set(result.size() - 1, new DocSpan(last.text() + span.text(),
                                last.bold(), last.italic(), last.mono(), last.strike(), last.link()));
                        continue;
                    }
                }
                result.add(span);
            }
            return result;
        }

        private static int hex(char c) {
            if (c >= '0' && c <= '9') {
                return c - '0';
            }
            if (c >= 'a' && c <= 'f') {
                return c - 'a' + 10;
            }
            if (c >= 'A' && c <= 'F') {
                return c - 'A' + 10;
            }
            return 0;
        }
    }
}
