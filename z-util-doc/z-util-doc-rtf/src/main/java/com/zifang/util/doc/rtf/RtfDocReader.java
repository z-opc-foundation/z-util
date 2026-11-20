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
            "fonttbl", "colortbl", "stylesheet", "info", "pict", "object", "themedata",
            "colorschememapping", "latentstyles", "datastore", "listtable", "listoverridetable",
            "rsidtbl", "generator", "filetbl", "xmlnstbl", "mmathPr", "wgrffmtfilter",
            "pntext", "pn", "falt", "shppict", "nonshppict", "bkmkstart", "bkmkend",
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
    }

    private static final class Parser {

        private final String src;
        private final DocDocument doc;
        private int pos;

        /** 字符样式状态栈（分组嵌套）。 */
        private final Deque<State> states = new ArrayDeque<>();
        private State state = new State();
        private final List<DocSpan> spans = new ArrayList<>();

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
                    pushGroup();
                    continue;
                }
                if (c == '}') {
                    pos++;
                    popGroup();
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
        }

        private State copyOf(State s) {
            State copy = new State();
            copy.bold = s.bold;
            copy.italic = s.italic;
            copy.strike = s.strike;
            copy.destinationSkip = s.destinationSkip;
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
                case "fonttbl", "colortbl", "stylesheet", "info", "pict", "object", "themedata",
                        "colorschememapping", "latentstyles", "datastore", "listtable",
                        "listoverridetable", "rsidtbl", "generator", "filetbl", "xmlnstbl",
                        "mmathPr", "wgrffmtfilter", "pntext", "pn", "falt", "shppict",
                        "nonshppict", "company", "operator", "chatn" -> state.destinationSkip = true;
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

        private void flushParagraph() {
            if (spans.isEmpty()) {
                return;
            }
            StringBuilder text = new StringBuilder();
            for (DocSpan span : spans) {
                text.append(span.text());
            }
            if (!text.toString().isBlank()) {
                doc.add(new DocBlock.Paragraph(merge(spans)));
            }
            spans.clear();
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
