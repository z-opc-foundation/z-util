package com.zifang.util.doc.doc;

import com.zifang.util.doc.DocParseException;

import com.zifang.util.doc.DocBlock;
import com.zifang.util.doc.DocDocument;
import com.zifang.util.doc.DocFormat;
import com.zifang.util.doc.DocFormatDetector;
import com.zifang.util.doc.DocReader;
import com.zifang.util.doc.DocSpan;
import com.zifang.util.doc.ole.CfbReader;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/** 二进制 Word（.doc, Word 97-2003）读取器：CFB 容器 + FIB/piece table/CHPX，纯 JDK。 */
public class DocDocReader implements DocReader {

    private static final String WORD_STREAM = "WordDocument";

    @Override
    public DocFormat format() {
        return DocFormat.DOC;
    }

    @Override
    public boolean supports(byte[] data) {
        if (!DocFormatDetector.isOle2(data)) {
            return false;
        }
        try {
            return new CfbReader(data).hasStream(WORD_STREAM);
        } catch (DocParseException e) {
            return false;
        }
    }

    @Override
    public DocDocument read(byte[] data) throws IOException {
        CfbReader cfb = new CfbReader(data);
        byte[] wordDoc = cfb.readStream(WORD_STREAM);
        if (wordDoc == null) {
            throw new IOException("not a Word binary document (no WordDocument stream)");
        }
        DocDocument doc = new DocDocument();
        fillMeta(doc, cfb);

        boolean use1 = WordBinaryParser.usesTable1(wordDoc);
        byte[] table = cfb.readStream(use1 ? "1Table" : "0Table");
        if (table == null) {
            table = cfb.readStream(use1 ? "0Table" : "1Table");
        }

        int ccpText = WordBinaryParser.ccpText(wordDoc);
        int ccpHdd = WordBinaryParser.ccpHdd(wordDoc);
        int ccpFtn = WordBinaryParser.ccpFtn(wordDoc);
        int ccpEdn = WordBinaryParser.ccpEdn(wordDoc);
        int ccpAtn = WordBinaryParser.ccpAtn(wordDoc);

        List<WordBinaryParser.CharRun> runs =
                WordBinaryParser.charRuns(wordDoc, table, ccpText);
        String text = WordBinaryParser.extractRange(wordDoc, table, 0, ccpText);
        emitParagraphs(doc, text, runs);

        // 页眉页脚故事：按规范顺序 [奇/偶页眉, 奇/偶页脚, 首页眉, 首页脚] 近似标注。
        int hddStart = ccpText + ccpFtn;
        List<int[]> stories = WordBinaryParser.headerStoryRanges(wordDoc, table, hddStart, ccpHdd);
        for (int i = 0; i < stories.size(); i++) {
            String label = (i / 2) % 2 == 0 ? "[页眉] " : "[页脚] ";
            String storyText = WordBinaryParser.extractRange(wordDoc, table, stories.get(i)[0], stories.get(i)[1]);
            for (String paragraph : paragraphs(storyText)) {
                if (!paragraph.isBlank()) {
                    doc.add(new DocBlock.Quote(List.of(DocSpan.plain(label + paragraph))));
                }
            }
        }

        // 脚注 / 批注 / 尾注文本（规范 CP 顺序：text→ftn→hdd→mcr→atn→edn）。
        int atnStart = ccpText + ccpFtn + ccpHdd;
        int ednStart = atnStart + ccpAtn;
        emitSubdoc(doc, wordDoc, table, "[脚注] ", ccpText, ccpFtn);
        emitSubdoc(doc, wordDoc, table, "[尾注] ", ednStart, ccpEdn);
        emitSubdoc(doc, wordDoc, table, "[批注] ", atnStart, ccpAtn);
        return doc;
    }

    /** 子文档文本 → Quote 块。 */
    private void emitSubdoc(DocDocument doc, byte[] wordDoc, byte[] table,
                            String label, int cpStart, int ccp) {
        if (ccp <= 0) {
            return;
        }
        String text = WordBinaryParser.extractRange(wordDoc, table, cpStart, cpStart + ccp);
        for (String paragraph : paragraphs(text)) {
            if (!paragraph.isBlank()) {
                doc.add(new DocBlock.Quote(List.of(DocSpan.plain(label + paragraph))));
            }
        }
    }

    /** 正文按段落（\r/0x07/0x0C/0x0B 分隔）切分，段内按 CHPX run 叠加粗斜删样式。 */
    private void emitParagraphs(DocDocument doc, String text, List<WordBinaryParser.CharRun> runs) {
        List<int[]> ranges = new ArrayList<>();
        int paraStart = 0;
        for (int cp = 0; cp < text.length(); cp++) {
            char c = text.charAt(cp);
            if (c == '\r' || c == '\u0007' || c == '\u000C' || c == '\u000B') {
                if (cp > paraStart) {
                    ranges.add(new int[]{paraStart, cp});
                }
                paraStart = cp + 1;
            }
        }
        if (text.length() > paraStart) {
            ranges.add(new int[]{paraStart, text.length()});
        }
        for (int[] range : ranges) {
            if (!cleanInline(text, range[0], range[1]).isBlank()) {
                doc.add(new DocBlock.Paragraph(spansForRange(text, runs, range[0], range[1])));
            }
        }
    }

    /**
     * 区间内控制字符清理：0x1E→'-'，字段/引用/对象控制符剔除。
     * 域（0x13 域开始 / 0x14 分隔符 / 0x15 域结束）里的指令文本整段丢弃，
     * 只保留分隔符之后的显示结果，避免 HYPERLINK、页码、TOC 等域指令混进正文。
     */
    private String cleanInline(String text, int from, int to) {
        StringBuilder sb = new StringBuilder(to - from);
        Deque<Boolean> fieldStack = new ArrayDeque<>();
        boolean inInstruction = false;
        for (int i = from; i < to && i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '\u0013' -> {
                    fieldStack.push(inInstruction);
                    inInstruction = true;
                }
                case '\u0014' -> inInstruction = false;
                case '\u0015' -> inInstruction = fieldStack.isEmpty() ? false : fieldStack.pop();
                case '\u001E' -> {
                    if (!inInstruction) {
                        sb.append('-');
                    }
                }
                case '\u0001', '\u0002', '\u0004', '\u0005', '\u0008', '\u001F' -> {
                }
                default -> {
                    if (!inInstruction) {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.toString();
    }

    /** 取 [cpStart, cpEnd) 的带格式 span 序列（逐 run 清理控制符）；无格式信息时降级为纯文本单 span。 */
    private List<DocSpan> spansForRange(String text, List<WordBinaryParser.CharRun> runs,
                                        int cpStart, int cpEnd) {
        List<DocSpan> spans = new ArrayList<>();
        if (runs.isEmpty()) {
            String plain = cleanInline(text, cpStart, cpEnd).strip();
            if (!plain.isEmpty()) {
                spans.add(DocSpan.plain(plain));
            }
            return spans;
        }
        for (WordBinaryParser.CharRun run : runs) {
            int from = Math.max(run.cpStart(), cpStart);
            int to = Math.min(run.cpEnd(), cpEnd);
            if (from >= to) {
                continue;
            }
            String chunk = cleanInline(text, from, to);
            if (chunk.isEmpty()) {
                continue;
            }
            DocSpan span = DocSpan.plain(chunk);
            if (run.bold()) {
                span = span.withBold();
            }
            if (run.italic()) {
                span = span.withItalic();
            }
            if (run.strike()) {
                span = span.withStrike();
            }
            spans.add(span);
        }
        if (spans.isEmpty()) {
            String plain = cleanInline(text, cpStart, cpEnd).strip();
            if (!plain.isEmpty()) {
                spans.add(DocSpan.plain(plain));
            }
        }
        return spans;
    }

    /** \r 段落 / 0x07 单元格 / 字段与对象控制符清理（纯文本版本）。 */
    static List<String> paragraphs(String raw) {
        StringBuilder cleaned = new StringBuilder(raw.length());
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            switch (c) {
                case '\r', '\u0007', '\u000C', '\u000B' -> cleaned.append('\n');
                case '\u001E' -> cleaned.append('-');
                case '\u0001', '\u0002', '\u0004', '\u0005', '\u0008',
                        '\u0013', '\u0014', '\u0015', '\u001F' -> {
                }
                default -> cleaned.append(c);
            }
        }
        List<String> out = new ArrayList<>();
        for (String part : cleaned.toString().split("\n+")) {
            String p = part.strip();
            if (!p.isEmpty()) {
                out.add(p);
            }
        }
        return out;
    }

    private void fillMeta(DocDocument doc, CfbReader cfb) {
        for (CfbReader.Entry e : cfb.getEntries()) {
            if (e.isStream() && e.name.startsWith("\u0005")) {
                byte[] s = cfb.readStream(e.name);
                if (s != null) {
                    SummaryInfoParser.parse(s, doc.getMeta());
                }
            }
        }
    }
}
