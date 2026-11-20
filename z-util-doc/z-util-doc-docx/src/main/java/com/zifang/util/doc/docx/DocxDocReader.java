package com.zifang.util.doc.docx;

import com.zifang.util.doc.DocBlock;
import com.zifang.util.doc.DocDocument;
import com.zifang.util.doc.DocFormat;
import com.zifang.util.doc.DocFormatDetector;
import com.zifang.util.doc.DocReader;
import com.zifang.util.doc.DocSpan;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * DOCX 读取器：纯 JDK 解压 + DOM 解析 OOXML，零第三方依赖，禁 DTD/外部实体防 XXE。
 * 命名空间无关（按 local-name 遍历），兼容 MS Word / WPS / LibreOffice / Google Docs 导出。
 * 覆盖：正文递归遍历（sdt/mc:AlternateContent/smartTag/ins 解包）、表格 gridSpan+vMerge 合并、
 * numbering 完整计数与 lvlText 渲染、styles.xml+outlineLvl 标题判定、页眉页脚、脚注尾注、
 * 文本框内容提取、drawing/pict/object 三路图片。
 */
public class DocxDocReader implements DocReader {

    /** 样式名到标题级别：heading N / HeadingN / Überschrift N / 标题 N / 纯数字 1-9。 */
    private static final Pattern HEADING_STYLE = Pattern.compile(
            "^(heading|berschrift|tulo)\s*([1-9])$|^(标题)\s*([1-9])$", Pattern.CASE_INSENSITIVE);

    @Override
    public DocFormat format() {
        return DocFormat.DOCX;
    }

    @Override
    public boolean supports(byte[] data) {
        return DocFormatDetector.isZip(data) && DocFormatDetector.detectZipContainer(data) == DocFormat.DOCX;
    }

    @Override
    public DocDocument read(byte[] data) throws IOException {
        Map<String, byte[]> entries = readZip(data);
        Ctx ctx = new Ctx();
        ctx.rels = parseRels(entries.get("word/_rels/document.xml.rels"));
        ctx.styles = parseStyles(entries.get("word/styles.xml"));
        ctx.numbering = parseNumbering(entries.get("word/numbering.xml"));
        ctx.notes = parseNotes(entries.get("word/footnotes.xml"), "footnote");
        ctx.notes.putAll(parseNotes(entries.get("word/endnotes.xml"), "endnote"));
        ctx.comments = parseComments(entries.get("word/comments.xml"));
        ctx.entries = entries;
        DocDocument doc = new DocDocument();
        ctx.doc = doc;
        Element root = documentElement(entries.get("word/document.xml"), "document");
        if (root != null) {
            Element body = childByLocal(root, "body");
            if (body != null) {
                emitBlocks(body, ctx);
            }
        }
        emitHeadersFooters(ctx);
        emitUsedNotes(ctx);
        readCoreMetadata(entries.get("docProps/core.xml"), doc);
        return doc;
    }

    // ---------- 正文递归遍历 ----------

    /** 块级容器递归：p/tbl 之外解包 sdt、AlternateContent、smartTag/ins/customXml 等包装层。 */
    private void emitBlocks(Element container, Ctx ctx) {
        for (Node node : elements(container)) {
            switch (local(node)) {
                case "p" -> ctx.doc.getBlocks().addAll(buildParagraph((Element) node, ctx));
                case "tbl" -> ctx.doc.getBlocks().add(readTable((Element) node, ctx));
                case "sdt" -> {
                    Element content = descendantByLocal(node, "sdtContent");
                    if (content != null) {
                        emitBlocks(content, ctx);
                    }
                }
                case "smartTag", "ins", "customXml", "sdtContent" -> emitBlocks((Element) node, ctx);
                case "AlternateContent" -> {
                    Element choice = childByLocal(node, "Choice");
                    Element fallback = choice == null ? childByLocal(node, "Fallback") : null;
                    Element picked = choice != null ? choice : fallback;
                    if (picked != null) {
                        emitBlocks(picked, ctx);
                    }
                }
                case "sectPr" -> captureHeaderFooterRefs((Element) node, ctx);
                default -> {
                }
            }
        }
    }

    /**
     * 段落 → 0..n 个块：Paragraph/Heading/ListItem 之外，图片与文本框内容按序产出。
     * pPr 取 pStyle/numPr/outlineLvl；run 内容递归穿 hyperlink/smartTag/ins/fldSimple/sdt。
     */
    private List<DocBlock> buildParagraph(Element p, Ctx ctx) {
        List<DocSpan> spans = new ArrayList<>();
        List<DocBlock> extra = new ArrayList<>();
        String styleId = null;
        String numId = null;
        int listLevel = 0;
        Integer outline = null;
        for (Node node : elements(p)) {
            if (local(node).equals("pPr")) {
                for (Node pp : elements(node)) {
                    switch (local(pp)) {
                        case "pStyle" -> styleId = attr((Element) pp, "val");
                        case "outlineLvl" -> outline = intVal(attr((Element) pp, "val"), -1);
                        case "numPr" -> {
                            numId = attrOfChild((Element) pp, "numId");
                            listLevel = intVal(attrOfChild((Element) pp, "ilvl"), 0);
                        }
                        default -> {
                        }
                    }
                }
            }
        }
        walkRuns(p, spans, extra, ctx, null);
        List<DocBlock> commentBlocks = commentBlocks(p, ctx);
        if (spans.isEmpty() && extra.isEmpty() && commentBlocks.isEmpty()) {
            return List.of();
        }
        int heading = outline != null && outline >= 0 && outline <= 8
                ? outline + 1
                : ctx.styles.headingLevel(styleId);
        List<DocBlock> out = new ArrayList<>(extra);
        if (heading > 0 && !text(spans).isBlank()) {
            out.add(new DocBlock.Heading(heading, spans));
        } else if (numId != null) {
            Numbering.Marker marker = ctx.numbering.next(numId, listLevel);
            out.add(new DocBlock.ListItem(marker.ordered(), listLevel, markerSpans(marker, spans)));
        } else if (!spans.isEmpty()) {
            out.add(new DocBlock.Paragraph(spans));
        }
        out.addAll(commentBlocks);
        return out;
    }

    /** 段落里引用到的批注：以 Quote 块输出，每条批注只输出一次。 */
    private List<DocBlock> commentBlocks(Element p, Ctx ctx) {
        List<DocBlock> blocks = new ArrayList<>();
        for (Element ref : descendantsByLocal(p, "commentReference")) {
            String id = attr(ref, "id");
            if (id == null) {
                continue;
            }
            Comment comment = ctx.comments.get(id);
            if (comment == null || !ctx.emittedComments.add(id)) {
                continue;
            }
            String label = comment.author == null || comment.author.isBlank()
                    ? "[批注] "
                    : "[批注 " + comment.author + "] ";
            blocks.add(new DocBlock.Quote(List.of(DocSpan.plain(label + comment.text))));
        }
        return blocks;
    }

    /** 编号标记作为首 span 注入（真实 lvlText 渲染，如 "2.3.1"）；无标记原样返回。 */
    private List<DocSpan> markerSpans(Numbering.Marker marker, List<DocSpan> spans) {
        if (marker.text() == null || marker.text().isBlank()) {
            return spans;
        }
        List<DocSpan> result = new ArrayList<>();
        result.add(DocSpan.plain(marker.text() + " "));
        result.addAll(spans);
        return result;
    }

    /** 行内内容递归：穿 hyperlink/smartTag/ins/fldSimple/sdt/AlternateContent，跳过删除与域代码。 */
    private void walkRuns(Element container, List<DocSpan> spans, List<DocBlock> extra,
                          Ctx ctx, String link) {
        for (Node node : elements(container)) {
            switch (local(node)) {
                case "r" -> run((Element) node, spans, extra, ctx, link);
                case "hyperlink" -> {
                    Element e = (Element) node;
                    String anchor = attr(e, "anchor");
                    String target = anchor != null && !anchor.isBlank()
                            ? "#" + anchor
                            : ctx.rels.get(attr(e, "id"));
                    walkRuns(e, spans, extra, ctx, target != null ? target : link);
                }
                case "smartTag", "ins", "sdtContent", "fldSimple" -> walkRuns((Element) node, spans, extra, ctx, link);
                case "del" -> deletedText((Element) node, spans);
                case "drawing", "pict", "object" -> media(node, spans, extra, ctx);
                case "sdt" -> {
                    Element content = descendantByLocal(node, "sdtContent");
                    if (content != null) {
                        walkRuns(content, spans, extra, ctx, link);
                    }
                }
                case "AlternateContent" -> {
                    Element choice = childByLocal(node, "Choice");
                    Element fallback = choice == null ? childByLocal(node, "Fallback") : null;
                    if (choice != null || fallback != null) {
                        walkRuns(choice != null ? choice : fallback, spans, extra, ctx, link);
                    }
                }
                case "Choice", "Fallback" -> walkRuns((Element) node, spans, extra, ctx, link);
                default -> {
                }
            }
        }
    }

    /** 单 run：rPr 叠加样式；t/tab/br/cr/连字符/脚注引用/图片三类路径。 */
    private void run(Element r, List<DocSpan> spans, List<DocBlock> extra, Ctx ctx, String link) {
        boolean bold = false;
        boolean italic = false;
        boolean strike = false;
        boolean mono = false;
        for (Node pr : elements(r)) {
            if (!local(pr).equals("rPr")) {
                continue;
            }
            for (Node n : elements(pr)) {
                switch (local(n)) {
                    case "b" -> bold = onOff((Element) n);
                    case "i" -> italic = onOff((Element) n);
                    case "strike", "dstrike" -> {
                        if (onOff((Element) n)) {
                            strike = true;
                        }
                    }
                    case "rFonts" -> mono = isMonoFont(attr((Element) n, "ascii"),
                            attr((Element) n, "hAnsi"), attr((Element) n, "eastAsia"));
                    default -> {
                    }
                }
            }
        }
        for (Node node : elements(r)) {
            switch (local(node)) {
                case "t" -> addSpan(spans, node.getTextContent(), bold, italic, strike, mono, link);
                case "tab" -> addSpan(spans, "\t", bold, italic, strike, mono, link);
                case "br", "cr" -> addSpan(spans, "\n", bold, italic, strike, mono, link);
                case "noBreakHyphen", "softHyphen" -> addSpan(spans, "-", bold, italic, strike, mono, link);
                case "footnoteReference" -> noteRef(attr((Element) node, "id"), ctx, spans, false);
                case "endnoteReference" -> noteRef(attr((Element) node, "id"), ctx, spans, true);
                case "drawing", "pict", "object" -> media(node, spans, extra, ctx);
                case "AlternateContent", "sdt", "smartTag", "ins" -> {
                    Element pick = local(node).equals("AlternateContent")
                            ? (childByLocal(node, "Choice") != null
                               ? childByLocal(node, "Choice") : childByLocal(node, "Fallback"))
                            : (local(node).equals("sdt") ? descendantByLocal(node, "sdtContent")
                               : (Element) node);
                    if (pick != null) {
                        walkRuns(pick, spans, extra, ctx, link);
                    }
                }
                default -> {
                }
            }
        }
    }

    /** 修订标记里的删除文本：保留内容并加删除线，不静默丢弃。 */
    private void deletedText(Element del, List<DocSpan> spans) {
        for (Node run : elements(del)) {
            if (!local(run).equals("r")) {
                continue;
            }
            for (Node t : elements((Element) run)) {
                if (local(t).equals("delText")) {
                    addSpan(spans, t.getTextContent(), false, false, true, false, null);
                }
            }
        }
    }

    private void noteRef(String id, Ctx ctx, List<DocSpan> spans, boolean endnote) {
        if (id == null) {
            return;
        }
        Note note = ctx.notes.get(endnote ? "e" + id : id);
        if (note != null) {
            note.used = true;
            addSpan(spans, (endnote ? "[^e" : "[^") + id + "]", false, false, false, false, null);
        }
    }

    /** 图片/对象：优先提取文本框内容；否则走 blip:embed / v:imagedata 的 rel 解析。 */
    private void media(Node node, List<DocSpan> spans, List<DocBlock> extra, Ctx ctx) {
        Element txbx = descendantByLocal(node, "txbxContent");
        if (txbx != null) {
            for (Node child : elements(txbx)) {
                if (local(child).equals("p")) {
                    extra.addAll(buildParagraph((Element) child, ctx));
                } else if (local(child).equals("tbl")) {
                    extra.add(readTable((Element) child, ctx));
                }
            }
            if (txbx.getFirstChild() != null) {
                return;
            }
        }
        String relId = null;
        Element blip = descendantByLocal(node, "blip");
        if (blip != null) {
            relId = attr(blip, "embed");
        }
        if (relId == null) {
            Element imagedata = descendantByLocal(node, "imagedata");
            if (imagedata != null) {
                relId = attr(imagedata, "id");
            }
        }
        if (relId == null) {
            return;
        }
        String target = ctx.rels.get(relId);
        if (target == null) {
            return;
        }
        String path = target.startsWith("/") ? target.substring(1) : "word/" + target;
        byte[] bytes = ctx.entries.get(path);
        if (bytes == null) {
            return;
        }
        String name = target.substring(target.lastIndexOf('/') + 1);
        extra.add(new DocBlock.Image(name, guessMime(name), bytes, imageSize(node)[0], imageSize(node)[1]));
    }

    /** wp:extent 或 a:ext 的 cx/cy（EMU）→ 像素；缺失返回 [null, null]。 */
    private Integer[] imageSize(Node node) {
        Element extent = descendantByLocal(node, "extent");
        if (extent == null) {
            extent = descendantByLocal(node, "ext");
        }
        if (extent == null) {
            return new Integer[]{null, null};
        }
        try {
            long cx = Long.parseLong(attr(extent, "cx"));
            long cy = Long.parseLong(attr(extent, "cy"));
            return new Integer[]{(int) (cx / 9525), (int) (cy / 9525)};
        } catch (NumberFormatException e) {
            return new Integer[]{null, null};
        }
    }

    private void addSpan(List<DocSpan> spans, String text, boolean bold, boolean italic,
                         boolean strike, boolean mono, String link) {
        if (text == null || text.isEmpty()) {
            return;
        }
        DocSpan span = DocSpan.plain(text);
        if (bold) {
            span = span.withBold();
        }
        if (italic) {
            span = span.withItalic();
        }
        if (strike) {
            span = span.withStrike();
        }
        if (mono) {
            span = span.withMono();
        }
        if (link != null) {
            span = span.withLink(link);
        }
        spans.add(span);
    }

    // ---------- 表格 ----------

    /**
     * 表格：tr → Row；tcPr gridSpan → colspan、vMerge → rowspan（跨行合并续格并入首格）；
     * trPr tblHeader 标记表头行，全表无标记时首行作表头；单元格内嵌套表格递归。
     */
    private DocBlock.Table readTable(Element tbl, Ctx ctx) {
        List<List<DocBlock.Table.Cell>> rowCells = new ArrayList<>();
        List<boolean[]> rowHeader = new ArrayList<>();
        boolean anyHeaderFlag = false;
        Map<Integer, int[]> vMergeOpeners = new HashMap<>();
        for (Node tr : elements(tbl)) {
            if (!local(tr).equals("tr")) {
                continue;
            }
            List<DocBlock.Table.Cell> cells = new ArrayList<>();
            boolean headerFlag = false;
            int gridCol = 0;
            for (Node trPrNode : elements(tr)) {
                if (local(trPrNode).equals("trPr")
                        && descendantByLocal(trPrNode, "tblHeader") != null) {
                    headerFlag = true;
                }
            }
            anyHeaderFlag |= headerFlag;
            for (Node tc : elements(tr)) {
                if (!local(tc).equals("tc")) {
                    continue;
                }
                int colspan = 1;
                boolean vMergeContinue = false;
                for (Node tcPrNode : elements(tc)) {
                    if (!local(tcPrNode).equals("tcPr")) {
                        continue;
                    }
                    Element tcPr = (Element) tcPrNode;
                    colspan = Math.max(1, intVal(attrOfChild(tcPr, "gridSpan"), 1));
                    Element vMerge = childByLocal(tcPr, "vMerge");
                    if (vMerge != null) {
                        String val = attr(vMerge, "val");
                        vMergeContinue = val == null || !val.equals("restart");
                    }
                }
                if (vMergeContinue) {
                    int[] pos = vMergeOpeners.get(gridCol);
                    if (pos != null) {
                        List<DocBlock.Table.Cell> openerRow = rowCells.get(pos[0]);
                        openerRow.set(pos[1], openerRow.get(pos[1]).growRowspan());
                    }
                    gridCol += colspan;
                    continue;
                }
                List<DocBlock> blocks = new ArrayList<>();
                for (Node child : elements(tc)) {
                    switch (local(child)) {
                        case "p" -> blocks.addAll(buildParagraph((Element) child, ctx));
                        case "tbl" -> blocks.add(readTable((Element) child, ctx));
                        case "sdt" -> {
                            Element content = descendantByLocal(child, "sdtContent");
                            if (content != null) {
                                emitBlocksInto(content, blocks, ctx);
                            }
                        }
                        default -> {
                        }
                    }
                }
                if (blocks.isEmpty()) {
                    blocks.add(new DocBlock.Paragraph(""));
                }
                DocBlock.Table.Cell cell = new DocBlock.Table.Cell(blocks, colspan, 1);
                for (int c = gridCol; c < gridCol + colspan; c++) {
                    vMergeOpeners.put(c, new int[]{rowCells.size(), cells.size()});
                }
                cells.add(cell);
                gridCol += colspan;
            }
            rowCells.add(cells);
            rowHeader.add(new boolean[]{headerFlag});
        }
        List<DocBlock.Table.Row> rows = new ArrayList<>();
        for (int i = 0; i < rowCells.size(); i++) {
            boolean header = rowHeader.get(i)[0] || (!anyHeaderFlag && i == 0);
            rows.add(new DocBlock.Table.Row(rowCells.get(i), header));
        }
        return new DocBlock.Table(rows);
    }

    private void emitBlocksInto(Element container, List<DocBlock> sink, Ctx ctx) {
        for (Node node : elements(container)) {
            switch (local(node)) {
                case "p" -> sink.addAll(buildParagraph((Element) node, ctx));
                case "tbl" -> sink.add(readTable((Element) node, ctx));
                case "sdt" -> {
                    Element content = descendantByLocal(node, "sdtContent");
                    if (content != null) {
                        emitBlocksInto(content, sink, ctx);
                    }
                }
                default -> {
                }
            }
        }
    }

    // ---------- 页眉页脚 / 脚注尾注 ----------

    /** 从 sectPr 收集 headerReference/footerReference（type → r:id）。 */
    private void captureHeaderFooterRefs(Element sectPr, Ctx ctx) {
        for (Node node : elements(sectPr)) {
            String l = local(node);
            if (l.equals("headerReference") || l.equals("footerReference")) {
                String type = attr((Element) node, "type");
                String id = attr((Element) node, "id");
                if (id != null) {
                    ctx.headerFooterRefs.add(l.equals("headerReference") ? "header " : "footer ");
                    ctx.headerFooterRefs.set(ctx.headerFooterRefs.size() - 1,
                            (l.equals("headerReference") ? "header:" : "footer:") + type + ":" + id);
                }
            }
        }
    }

    /** 页眉页脚按引用顺序解析为 Quote 块，追加在正文之后。 */
    private void emitHeadersFooters(Ctx ctx) {
        for (String ref : ctx.headerFooterRefs) {
            String[] parts = ref.split(":", 3);
            byte[] xml = ctx.entries.get(resolveTarget(parts[2], ctx));
            if (xml == null) {
                continue;
            }
            Element root = documentElement(xml, null);
            if (root == null) {
                continue;
            }
            boolean first = true;
            for (Node p : elements(root)) {
                if (!local(p).equals("p")) {
                    continue;
                }
                List<DocBlock> blocks = buildParagraph((Element) p, ctx);
                for (DocBlock b : blocks) {
                    if (b instanceof DocBlock.Paragraph para) {
                        String text = text(para.spans());
                        if (text.isBlank()) {
                            continue;
                        }
                        if (first) {
                            text = (parts[0].equals("header") ? "[页眉] " : "[页脚] ") + text;
                            first = false;
                        }
                        ctx.doc.getBlocks().add(new DocBlock.Quote(List.of(DocSpan.plain(text))));
                    }
                }
            }
        }
    }

    private String resolveTarget(String relId, Ctx ctx) {
        String target = ctx.rels.get(relId);
        if (target == null) {
            return null;
        }
        return target.startsWith("/") ? target.substring(1) : "word/" + target;
    }

    /** footnotes/endnotes：id → Note（跳过分隔符类），供引用处与文末汇总。 */
    private Map<String, Note> parseNotes(byte[] xml, String kind) {
        Map<String, Note> notes = new LinkedHashMap<>();
        Element root = documentElement(xml, null);
        if (root == null) {
            return notes;
        }
        String tag = kind.equals("footnote") ? "footnote" : "endnote";
        for (Node node : elements(root)) {
            if (!local(node).equals(tag)) {
                continue;
            }
            Element e = (Element) node;
            String type = attr(e, "type");
            if (type != null && (type.equals("separator") || type.equals("continuationSeparator"))) {
                continue;
            }
            String id = attr(e, "id");
            if (id == null) {
                continue;
            }
            StringBuilder text = new StringBuilder();
            for (Node p : elements(e)) {
                if (local(p).equals("p")) {
                    List<DocBlock> blocks = buildParagraph((Element) p, ctx());
                    for (DocBlock b : blocks) {
                        text.append(blockText(b)).append('\n');
                    }
                }
            }
            notes.put((kind.equals("footnote") ? "" : "e") + id,
                    new Note(id, text.toString().strip()));
        }
        return notes;
    }

    /** comments.xml：批注 id → 作者 / 缩写 / 时间 / 正文。 */
    private Map<String, Comment> parseComments(byte[] xml) {
        Map<String, Comment> comments = new LinkedHashMap<>();
        Element root = documentElement(xml, null);
        if (root == null) {
            return comments;
        }
        for (Node node : elements(root)) {
            if (!local(node).equals("comment")) {
                continue;
            }
            Element e = (Element) node;
            String id = attr(e, "id");
            if (id == null) {
                continue;
            }
            StringBuilder text = new StringBuilder();
            for (Node p : elements(e)) {
                if (local(p).equals("p")) {
                    for (DocBlock b : buildParagraph((Element) p, ctx())) {
                        text.append(blockText(b)).append(' ');
                    }
                }
            }
            comments.put(id, new Comment(id, attr(e, "author"), attr(e, "initials"), attr(e, "date"),
                    text.toString().strip()));
        }
        return comments;
    }

    /** parseNotes 需要一个 Ctx 来构建段落（编号/样式都还没生效，用独立轻量 Ctx）。 */
    private Ctx ctx() {
        Ctx c = new Ctx();
        c.rels = Map.of();
        c.styles = new Styles();
        c.numbering = new Numbering();
        c.entries = Map.of();
        c.doc = new DocDocument();
        return c;
    }

    /** 引用过的脚注尾注以 Quote 块汇总到文末。 */
    private void emitUsedNotes(Ctx ctx) {
        for (Note note : ctx.notes.values()) {
            if (note.used && !note.text.isBlank()) {
                ctx.doc.getBlocks().add(new DocBlock.Quote(List.of(DocSpan.plain(note.text))));
            }
        }
    }

    // ---------- styles.xml ----------

    /** styles.xml：styleId → (name, outlineLvl)；标题判定优先 outlineLvl，其次样式名。 */
    private Styles parseStyles(byte[] xml) {
        Styles styles = new Styles();
        Element root = documentElement(xml, null);
        if (root == null) {
            return styles;
        }
        for (Node node : elements(root)) {
            if (!local(node).equals("style")) {
                continue;
            }
            Element e = (Element) node;
            String id = attr(e, "styleId");
            if (id == null) {
                continue;
            }
            String name = attrOfChild(e, "name");
            Integer outline = null;
            for (Node child : elements(e)) {
                if (local(child).equals("pPr")) {
                    for (Node pp : elements(child)) {
                        if (local(pp).equals("outlineLvl")) {
                            outline = intVal(attr((Element) pp, "val"), -1);
                        }
                    }
                }
            }
            styles.byId.put(id, new StyleInfo(name, outline));
        }
        return styles;
    }

    // ---------- numbering.xml ----------

    /**
     * numbering.xml 完整模型：abstractNum（各 ilvl 的 numFmt/lvlText/start）+ num 映射与
     * startOverride；遍历时按文档顺序推进计数器，渲染 %1.%2 形式的真实编号。
     */
    private Numbering parseNumbering(byte[] xml) {
        Numbering numbering = new Numbering();
        Element root = documentElement(xml, null);
        if (root == null) {
            return numbering;
        }
        for (Node node : elements(root)) {
            switch (local(node)) {
                case "abstractNum" -> {
                    String abstractId = attr((Element) node, "abstractNumId");
                    Map<Integer, Numbering.Level> levels = new HashMap<>();
                    for (Node lvl : elements(node)) {
                        if (!local(lvl).equals("lvl")) {
                            continue;
                        }
                        Element le = (Element) lvl;
                        int ilvl = intVal(attr(le, "ilvl"), 0);
                        String fmt = attrOfChild(le, "numFmt");
                        String text = attrOfChild(le, "lvlText");
                        int start = intVal(attrOfChild(le, "start"), 1);
                        levels.put(ilvl, new Numbering.Level(fmt, text, start));
                    }
                    numbering.abstracts.put(abstractId, levels);
                }
                case "num" -> {
                    String numId = attr((Element) node, "numId");
                    String abstractId = attrOfChild((Element) node, "abstractNumId");
                    if (numId == null || abstractId == null) {
                        continue;
                    }
                    Map<Integer, Integer> overrides = new HashMap<>();
                    for (Node o : elements(node)) {
                        if (!local(o).equals("lvlOverride")) {
                            continue;
                        }
                        int olvl = intVal(attr((Element) o, "ilvl"), 0);
                        Integer startOverride = null;
                        for (Node so : elements(o)) {
                            if (local(so).equals("startOverride")) {
                                startOverride = intVal(attr((Element) so, "val"), 1);
                            }
                        }
                        if (startOverride != null) {
                            overrides.put(olvl, startOverride);
                        }
                    }
                    numbering.nums.put(numId, new Numbering.Num(abstractId, overrides));
                }
                default -> {
                }
            }
        }
        return numbering;
    }

    // ---------- 元数据 ----------

    /** docProps/core.xml 元数据：title/creator/subject/keywords/created/modified。 */
    private void readCoreMetadata(byte[] xml, DocDocument doc) {
        Element root = documentElement(xml, null);
        if (root == null) {
            return;
        }
        for (Node node : elements(root)) {
            String text = node.getTextContent();
            switch (local(node)) {
                case "title" -> doc.getMeta().setTitle(text);
                case "creator" -> doc.getMeta().setAuthor(text);
                case "lastModifiedBy" -> doc.getMeta().setCreator(text);
                case "subject" -> doc.getMeta().setSubject(text);
                case "keywords" -> doc.getMeta().setKeywords(text);
                case "created" -> parseDate(text).ifPresent(doc.getMeta()::setCreationDate);
                case "modified" -> parseDate(text).ifPresent(doc.getMeta()::setModDate);
                default -> {
                }
            }
        }
    }

    private java.util.Optional<OffsetDateTime> parseDate(String text) {
        try {
            return java.util.Optional.of(OffsetDateTime.parse(text.trim()));
        } catch (Exception e) {
            return java.util.Optional.empty();
        }
    }

    // ---------- 工具 ----------

    private Map<String, byte[]> readZip(byte[] data) throws IOException {
        Map<String, byte[]> entries = new HashMap<>();
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(data))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    entries.put(entry.getName(), zip.readAllBytes());
                }
                zip.closeEntry();
            }
        }
        return entries;
    }

    /** rels：Id → Target。 */
    private Map<String, String> parseRels(byte[] xml) {
        Map<String, String> rels = new HashMap<>();
        Element root = documentElement(xml, null);
        if (root == null) {
            return rels;
        }
        for (Node node : elements(root)) {
            if (local(node).equals("Relationship")) {
                Element e = (Element) node;
                String id = attr(e, "Id");
                String target = attr(e, "Target");
                if (id != null && target != null) {
                    rels.put(id, target);
                }
            }
        }
        return rels;
    }

    /** 样式名 → 标题级别；Title/标题 → 1；非标题返回 0。styleId 缺信息时回退按 id 判定。 */
    private int headingLevel(String styleId, Styles styles) {
        return styles.headingLevel(styleId);
    }

    /** w:val 开关语义：val 为 false/0/none/off 视为关，缺省为开。 */
    private boolean onOff(Element e) {
        String val = attr(e, "val");
        return val == null || !(val.equalsIgnoreCase("false") || val.equals("0")
                || val.equalsIgnoreCase("none") || val.equalsIgnoreCase("off"));
    }

    private boolean isMonoFont(String... names) {
        for (String name : names) {
            if (name == null) {
                continue;
            }
            String lower = name.toLowerCase().replace(" ", "");
            if (lower.contains("mono") || lower.contains("courier") || lower.contains("menlo")
                    || lower.contains("consolas") || lower.contains("sarasa")) {
                return true;
            }
        }
        return false;
    }

    private String guessMime(String name) {
        String lower = name.toLowerCase();
        if (lower.endsWith(".png")) {
            return "image/png";
        }
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        if (lower.endsWith(".gif")) {
            return "image/gif";
        }
        if (lower.endsWith(".bmp")) {
            return "image/bmp";
        }
        if (lower.endsWith(".emf")) {
            return "image/emf";
        }
        if (lower.endsWith(".wmf")) {
            return "image/wmf";
        }
        return "application/octet-stream";
    }

    /** 命名空间无关的 DOM 解析；禁 DTD/外部实体。 */
    private Element documentElement(byte[] xml, String expectLocal) {
        if (xml == null || xml.length == 0) {
            return null;
        }
        try {
            DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
            f.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            f.setFeature("http://xml.org/sax/features/external-general-entities", false);
            f.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            f.setXIncludeAware(false);
            f.setExpandEntityReferences(false);
            f.setNamespaceAware(true);
            Document d = f.newDocumentBuilder()
                    .parse(new InputSource(new ByteArrayInputStream(xml)));
            Element root = d.getDocumentElement();
            if (root == null) {
                return null;
            }
            if (expectLocal != null && !local(root).equals(expectLocal)) {
                return null;
            }
            return root;
        } catch (Exception e) {
            return null;
        }
    }

    /** 直接子元素列表。 */
    private List<Node> elements(Node node) {
        List<Node> result = new ArrayList<>();
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                result.add(children.item(i));
            }
        }
        return result;
    }

    private Element childByLocal(Node node, String local) {
        for (Node child : elements(node)) {
            if (local(child).equals(local)) {
                return (Element) child;
            }
        }
        return null;
    }

    /** 深度优先找第一个 local-name 匹配的后代元素。 */
    private Element descendantByLocal(Node node, String local) {
        for (Node child : elements(node)) {
            if (local(child).equals(local)) {
                return (Element) child;
            }
            Element found = descendantByLocal(child, local);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    /** 深度优先收集所有 local-name 匹配的后代元素。 */
    private List<Element> descendantsByLocal(Node node, String local) {
        List<Element> result = new ArrayList<>();
        collectDescendants(node, local, result);
        return result;
    }

    private void collectDescendants(Node node, String local, List<Element> result) {
        for (Node child : elements(node)) {
            if (local(child).equals(local)) {
                result.add((Element) child);
            }
            collectDescendants(child, local, result);
        }
    }

    private String local(Node node) {
        String name = node.getLocalName();
        return name != null ? name : node.getNodeName();
    }

    /** 按 local-name 取属性（命名空间无关）。 */
    private String attr(Element e, String local) {
        NamedNodeMap attrs = e.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            Node a = attrs.item(i);
            String name = a.getLocalName() != null ? a.getLocalName() : a.getNodeName();
            if (name.equals(local)) {
                return a.getNodeValue();
            }
        }
        return null;
    }

    private String attrOfChild(Element parent, String local) {
        for (Node child : elements(parent)) {
            if (local(child).equals(local)) {
                return attr((Element) child, "val");
            }
        }
        return null;
    }

    private int intVal(String val, int dft) {
        if (val == null) {
            return dft;
        }
        try {
            return Integer.parseInt(val.trim());
        } catch (NumberFormatException e) {
            return dft;
        }
    }

    private String text(List<DocSpan> spans) {
        StringBuilder sb = new StringBuilder();
        for (DocSpan s : spans) {
            sb.append(s.text());
        }
        return sb.toString();
    }

    private String blockText(DocBlock block) {
        if (block instanceof DocBlock.Paragraph p) {
            return text(p.spans());
        }
        if (block instanceof DocBlock.Heading h) {
            return text(h.spans());
        }
        if (block instanceof DocBlock.ListItem li) {
            return text(li.spans());
        }
        if (block instanceof DocBlock.Quote q) {
            return text(q.spans());
        }
        return "";
    }

    // ---------- 解析期上下文 ----------

    private static final class Ctx {
        Map<String, String> rels = Map.of();
        Styles styles = new Styles();
        Numbering numbering = new Numbering();
        Map<String, byte[]> entries = Map.of();
        Map<String, Note> notes = new LinkedHashMap<>();
        Map<String, Comment> comments = new LinkedHashMap<>();
        Set<String> emittedComments = new LinkedHashSet<>();
        List<String> headerFooterRefs = new ArrayList<>();
        DocDocument doc;
    }

    /** 批注（word/comments.xml）。 */
    private static final class Comment {
        final String id;
        final String author;
        final String initials;
        final String date;
        final String text;

        Comment(String id, String author, String initials, String date, String text) {
            this.id = id;
            this.author = author;
            this.initials = initials;
            this.date = date;
            this.text = text;
        }
    }

    private static final class Note {
        final String id;
        final String text;
        boolean used;

        Note(String id, String text) {
            this.id = id;
            this.text = text;
        }
    }

    private static final class StyleInfo {
        final String name;
        final Integer outlineLvl;

        StyleInfo(String name, Integer outlineLvl) {
            this.name = name;
            this.outlineLvl = outlineLvl;
        }
    }

    /** styles.xml 索引：标题判定优先 outlineLvl，其次样式名（含中文"标题 N"）。 */
    static final class Styles {
        final Map<String, StyleInfo> byId = new HashMap<>();

        int headingLevel(String styleId) {
            if (styleId == null || styleId.isBlank()) {
                return 0;
            }
            if (styleId.equalsIgnoreCase("Title")) {
                return 1;
            }
            StyleInfo info = byId.get(styleId);
            if (info != null) {
                if (info.outlineLvl != null && info.outlineLvl >= 0 && info.outlineLvl <= 8) {
                    return info.outlineLvl + 1;
                }
                if (info.name != null) {
                    int level = byName(info.name);
                    if (level > 0) {
                        return level;
                    }
                }
            }
            return byName(styleId);
        }

        private int byName(String name) {
            if (name == null) {
                return 0;
            }
            String trimmed = name.trim();
            if (trimmed.equalsIgnoreCase("Title") || trimmed.equals("标题")) {
                return 1;
            }
            var m = HEADING_STYLE.matcher(trimmed);
            if (m.matches()) {
                for (int g = 2; g <= m.groupCount(); g++) {
                    if (m.group(g) != null && g % 2 == 0) {
                        return Integer.parseInt(m.group(g));
                    }
                }
            }
            return trimmed.matches("[1-9]") ? Integer.parseInt(trimmed) : 0;
        }
    }

    /** numbering 索引与计数器：文档顺序遍历时 next() 推进计数并渲染 lvlText。 */
    static final class Numbering {

        record Level(String numFmt, String lvlText, int start) {
        }

        record Num(String abstractId, Map<Integer, Integer> startOverrides) {
        }

        record Marker(boolean ordered, String text) {
        }

        final Map<String, Map<Integer, Level>> abstracts = new HashMap<>();
        final Map<String, Num> nums = new HashMap<>();
        private final Map<String, int[]> counters = new HashMap<>();

        Marker next(String numId, int ilvl) {
            Num num = nums.get(numId);
            if (num == null) {
                return new Marker(false, null);
            }
            Map<Integer, Level> levels = abstracts.getOrDefault(num.abstractId(), Map.of());
            Level level = levels.get(ilvl);
            if (level == null) {
                level = levels.get(ilvl > 0 ? Math.min(ilvl, levels.keySet().stream()
                        .mapToInt(Integer::intValue).max().orElse(0)) : 0);
            }
            if (level == null) {
                return new Marker(false, null);
            }
            if (level.numFmt() == null || level.numFmt().equals("bullet") || level.numFmt().equals("none")) {
                return new Marker(false, level.lvlText());
            }
            int[] counter = counters.computeIfAbsent(numId, k -> new int[9]);
            ilvl = Math.min(ilvl, counter.length - 1);
            if (counter[ilvl] == 0) {
                Integer override = num.startOverrides().get(ilvl);
                counter[ilvl] = override != null ? override : Math.max(1, level.start());
            } else {
                counter[ilvl]++;
            }
            for (int i = ilvl + 1; i < counter.length; i++) {
                counter[i] = 0;
            }
            // lvlText 缺失时不注入标记，保持导出器自身的编号行为。
            if (level.lvlText() == null || level.lvlText().isBlank()) {
                return new Marker(true, null);
            }
            return new Marker(true, render(level.lvlText(), counter, ilvl));
        }

        /** lvlText 的 %n 占位替换为各级计数；缺层级用 start 补。 */
        private String render(String lvlText, int[] counter, int ilvl) {
            if (lvlText == null || lvlText.isBlank()) {
                return String.valueOf(counter[ilvl]);
            }
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < lvlText.length(); i++) {
                char c = lvlText.charAt(i);
                if (c == '%' && i + 1 < lvlText.length()) {
                    int lvl = Character.digit(lvlText.charAt(++i), 10);
                    if (lvl >= 1 && lvl <= 9) {
                        int idx = lvl - 1;
                        int v = idx < counter.length && counter[idx] > 0
                                ? counter[idx]
                                : abstractStart(idx, counter, ilvl);
                        sb.append(v);
                        continue;
                    }
                    sb.append('%');
                }
                sb.append(c);
            }
            return sb.toString();
        }

        private int abstractStart(int idx, int[] counter, int ilvl) {
            // 引用更深层级（%2 出现在 ilvl=0 的罕见情况）时用 1 兜底。
            return 1;
        }

        boolean isOrdered(String numId, int ilvl) {
            return next(numId, ilvl).ordered();
        }
    }
}
