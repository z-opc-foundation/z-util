package com.zifang.util.doc.word;

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
public class WordDocReader implements DocReader {

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
        WordBinaryParser.checkVersion(wordDoc);
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
        DocStyleSheet styles = WordBinaryParser.styleSheet(wordDoc, table);
        List<WordBinaryParser.PapxEntry> papxIndex = WordBinaryParser.papxIndex(wordDoc, table);
        byte[] dataStream = cfb.readStream("Data");
        DocLists lists = WordBinaryParser.lists(wordDoc, table);
        emitParagraphs(doc, wordDoc, table, text, runs, styles, papxIndex, dataStream, lists);

        // 绘图数据里的图片（浮动图形、水印等）挂在正文之后
        for (DocPictures.Picture picture : DocPictures.fromDrawing(table, wordDoc,
                WordBinaryParser.fcDggInfo(wordDoc), WordBinaryParser.lcbDggInfo(wordDoc))) {
            doc.add(new DocBlock.Image(picture.name(), picture.mimeType(), picture.data(), null, null));
        }

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

    /**
     * 正文按段落切分（\r 段落 / 0x07 单元格与行尾 / 0x0C 分页 / 0x0B 换行），
     * 段内按 CHPX run 叠加粗斜删样式。
     *
     * <p>段落属性（PAPX）给出样式索引、列表层级与表格归属：
     * 样式名命中标题样式出 Heading，ilfo 非零出 ListItem，
     * 表内段落按 0x07 归并成单元格、行尾标记收拢成 Table。</p>
     */
    private void emitParagraphs(DocDocument doc, byte[] wordDoc, byte[] table, String text,
                                 List<WordBinaryParser.CharRun> runs, DocStyleSheet styles,
                                 List<WordBinaryParser.PapxEntry> papxIndex, byte[] dataStream,
                                 DocLists lists) {
        boolean[] visible = visibleMask(text);
        List<int[]> ranges = new ArrayList<>();
        List<Character> terminators = new ArrayList<>();
        int paraStart = 0;
        for (int cp = 0; cp < text.length(); cp++) {
            char c = text.charAt(cp);
            if (c == '\r' || c == '\u0007' || c == '\u000C' || c == '\u000B') {
                ranges.add(new int[]{paraStart, cp});
                terminators.add(c);
                paraStart = cp + 1;
            }
        }
        if (text.length() > paraStart) {
            ranges.add(new int[]{paraStart, text.length()});
            terminators.add('\0');
        }

        List<DocBlock.Table.Row> tableRows = new ArrayList<>();
        List<DocBlock.Table.Cell> rowCells = new ArrayList<>();
        List<DocBlock> cellBlocks = new ArrayList<>();
        TableMerger merger = new TableMerger();

        for (int i = 0; i < ranges.size(); i++) {
            int[] range = ranges.get(i);
            char terminator = terminators.get(i);
            WordBinaryParser.Papx papx = papxAt(wordDoc, table, styles, papxIndex, range[1]);
            List<DocSpan> spans = spansForRange(text, visible, runs, range[0], range[1]);
            String plain = cleanInline(text, visible, range[0], range[1]).strip();

            if (papx != null && papx.inTable()) {
                if (papx.rowEnd()) {
                    // 行尾标记：一行结束；连续行累积到同一张表，非表格段落出现时再落块
                    if (!cellBlocks.isEmpty()) {
                        rowCells.add(new DocBlock.Table.Cell(cellBlocks, 1, 1));
                        cellBlocks = new ArrayList<>();
                    }
                    if (!rowCells.isEmpty()) {
                        tableRows.add(merger.add(tableRows,
                                new DocBlock.Table.Row(rowCells, tableRows.isEmpty()), papx));
                        rowCells = new ArrayList<>();
                    }
                    continue;
                }
                if (!plain.isEmpty()) {
                    cellBlocks.add(new DocBlock.Paragraph(spans));
                    cellBlocks.addAll(inlinePictures(runs, range, dataStream));
                }
                if (terminator == '\u0007') {
                    rowCells.add(new DocBlock.Table.Cell(cellBlocks, 1, 1));
                    cellBlocks = new ArrayList<>();
                }
                continue;
            }

            if (!tableRows.isEmpty()) {
                doc.add(new DocBlock.Table(tableRows));
                tableRows = new ArrayList<>();
                merger = new TableMerger();
            }
            if (plain.isEmpty()) {
                continue;
            }
            int headingLevel = papx == null ? 0 : styles.headingLevel(papx.istd());
            if (headingLevel > 0) {
                doc.add(new DocBlock.Heading(headingLevel, spans));
            } else if (papx != null && papx.ilfo() != 0) {
                DocLists.Marker marker = lists.marker(papx.ilfo(), papx.ilvl());
                List<DocSpan> withLabel = marker.label() == null
                        ? spans
                        : withLabel(spans, marker.label());
                doc.add(new DocBlock.ListItem(marker.ordered(), papx.ilvl(), withLabel));
            } else {
                doc.add(new DocBlock.Paragraph(spans));
            }
            for (DocBlock image : inlinePictures(runs, range, dataStream)) {
                doc.add(image);
            }
        }
        if (!cellBlocks.isEmpty()) {
            rowCells.add(new DocBlock.Table.Cell(cellBlocks, 1, 1));
        }
        if (!rowCells.isEmpty()) {
            tableRows.add(new DocBlock.Table.Row(rowCells, tableRows.isEmpty()));
        }
        if (!tableRows.isEmpty()) {
            doc.add(new DocBlock.Table(tableRows));
        }
    }

    /**
     * 纵向合并消解：TC80 的 fVertMerge=1 且 fVertRestart=0 是续接上一行的合并单元格（跳过不输出，
     * 锚点格 rowspan 加一）；个别写出器给续接格也带 fVertRestart，内容为空且上方有未闭合合并时
     * 同样按续接处理。合并锚点跨行保持，连续多行并成一格。
     */
    private static final class TableMerger {

        /** 列 → 合并锚点位置 {表内行下标, 行内 kept 列表下标}。 */
        private final java.util.Map<Integer, int[]> openRuns = new java.util.HashMap<>();

        DocBlock.Table.Row add(List<DocBlock.Table.Row> tableRows, DocBlock.Table.Row raw,
                               WordBinaryParser.Papx papx) {
            boolean[] merged = papx.vMerge();
            boolean[] restart = papx.vRestart();
            List<DocBlock.Table.Cell> cells = raw.cells();
            List<DocBlock.Table.Cell> kept = new ArrayList<>();
            java.util.Map<Integer, int[]> nextOpen = new java.util.HashMap<>(openRuns);
            int col = 0;
            for (int i = 0; i < cells.size(); i++) {
                DocBlock.Table.Cell cell = cells.get(i);
                boolean mergeFlag = merged != null && i < merged.length && merged[i];
                boolean restartFlag = restart != null && i < restart.length && restart[i];
                boolean continuation = mergeFlag && openRuns.containsKey(col)
                        && (!restartFlag || cellText(cell).isBlank());
                if (continuation) {
                    int[] loc = openRuns.get(col);
                    List<DocBlock.Table.Cell> anchorRow = tableRows.get(loc[0]).cells();
                    DocBlock.Table.Cell start = anchorRow.get(loc[1]);
                    anchorRow.set(loc[1], new DocBlock.Table.Cell(start.blocks(), start.colspan(),
                            start.rowspan() + 1));
                    nextOpen.put(col, loc);
                    col += cell.colspan();
                    continue;
                }
                kept.add(cell);
                if (mergeFlag && restartFlag) {
                    nextOpen.put(col, new int[]{tableRows.size(), kept.size() - 1});
                } else if (!mergeFlag) {
                    nextOpen.remove(col);
                }
                col += cell.colspan();
            }
            openRuns.clear();
            openRuns.putAll(nextOpen);
            return new DocBlock.Table.Row(kept, raw.header());
        }
    }

    private static String cellText(DocBlock.Table.Cell cell) {
        StringBuilder sb = new StringBuilder();
        for (DocBlock block : cell.blocks()) {
            if (block instanceof DocBlock.Paragraph p) {
                for (DocSpan span : p.spans()) {
                    sb.append(span.text());
                }
            }
        }
        return sb.toString();
    }

    /** 列表编号/符号作为首个 span 注入，正文照旧。 */
    private static List<DocSpan> withLabel(List<DocSpan> body, String label) {
        List<DocSpan> result = new ArrayList<>();
        result.add(DocSpan.plain(label + " "));
        result.addAll(body);
        return result;
    }

    /**
     * 段落属性：按段落结束字符的 FC 定位 PAPX，再叠加样式表里继承来的属性。
     */
    private WordBinaryParser.Papx papxAt(byte[] wordDoc, byte[] table, DocStyleSheet styles,
                                         List<WordBinaryParser.PapxEntry> papxIndex, int cp) {
        if (papxIndex.isEmpty()) {
            return null;
        }
        int fc = WordBinaryParser.cpToFc(wordDoc, table, cp);
        if (fc < 0) {
            return null;
        }
        WordBinaryParser.Papx papx = WordBinaryParser.papxAt(papxIndex, fc);
        if (papx == null) {
            return null;
        }
        WordBinaryParser.Papx stylePapx = styles.papxOf(papx.istd());
        return stylePapx == null ? papx : papx.mergeOver(stylePapx);
    }

    /** 段落里的内联图片（CHPX sprmCPicLocation 指向 Data 流）。 */
    private List<DocBlock> inlinePictures(List<WordBinaryParser.CharRun> runs,
                                          int[] range, byte[] dataStream) {
        List<DocBlock> images = new ArrayList<>();
        if (dataStream == null) {
            return images;
        }
        for (WordBinaryParser.CharRun run : runs) {
            if (run.picOffset() < 0 || run.cpEnd() <= range[0] || run.cpStart() >= range[1]) {
                continue;
            }
            DocPictures.Picture picture = DocPictures.fromInline(dataStream, run.picOffset());
            if (picture != null) {
                images.add(new DocBlock.Image(picture.name(), picture.mimeType(), picture.data(), null, null));
            }
        }
        return images;
    }

    /**
     * 逐字符可见性：域（0x13 域开始 / 0x14 分隔符 / 0x15 域结束）里的指令文本整段丢弃，
     * 只保留分隔符之后的显示结果，避免 HYPERLINK、页码、TOC 等域指令混进正文。
     * 域可以跨 run 与跨段落，所以可见性按整篇文本一次算好。
     */
    static boolean[] visibleMask(String text) {
        boolean[] visible = new boolean[text.length()];
        Deque<Boolean> fieldStack = new ArrayDeque<>();
        boolean inInstruction = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '\u0013' -> {
                    fieldStack.push(inInstruction);
                    inInstruction = true;
                    visible[i] = false;
                }
                case '\u0014' -> {
                    inInstruction = false;
                    visible[i] = false;
                }
                case '\u0015' -> {
                    inInstruction = fieldStack.isEmpty() ? false : fieldStack.pop();
                    visible[i] = false;
                }
                case '\u0001', '\u0002', '\u0004', '\u0005', '\u0008', '\u001F' -> visible[i] = false;
                default -> visible[i] = !inInstruction;
            }
        }
        return visible;
    }

    /** 区间内可见文本；0x1E（非断行连字符）转成 '-'。 */
    private String cleanInline(String text, boolean[] visible, int from, int to) {
        StringBuilder sb = new StringBuilder(to - from);
        for (int i = from; i < to && i < text.length(); i++) {
            if (!visible[i]) {
                continue;
            }
            char c = text.charAt(i);
            sb.append(c == '\u001E' ? '-' : c);
        }
        return sb.toString();
    }

    /** 取 [cpStart, cpEnd) 的带格式 span 序列（逐 run 清理控制符）；无格式信息时降级为纯文本单 span。 */
    private List<DocSpan> spansForRange(String text, boolean[] visible,
                                        List<WordBinaryParser.CharRun> runs,
                                        int cpStart, int cpEnd) {
        List<DocSpan> spans = new ArrayList<>();
        if (runs.isEmpty()) {
            String plain = cleanInline(text, visible, cpStart, cpEnd).strip();
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
            String chunk = cleanInline(text, visible, from, to);
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
            String plain = cleanInline(text, visible, cpStart, cpEnd).strip();
            if (!plain.isEmpty()) {
                spans.add(DocSpan.plain(plain));
            }
        }
        return spans;
    }

    /** \r 段落 / 0x07 单元格 / 字段与对象控制符清理（纯文本版本）。 */
    static List<String> paragraphs(String raw) {
        boolean[] visible = visibleMask(raw);
        StringBuilder cleaned = new StringBuilder(raw.length());
        for (int i = 0; i < raw.length(); i++) {
            if (!visible[i]) {
                continue;
            }
            char c = raw.charAt(i);
            switch (c) {
                case '\r', '\u0007', '\u000C', '\u000B' -> cleaned.append('\n');
                case '\u001E' -> cleaned.append('-');
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
