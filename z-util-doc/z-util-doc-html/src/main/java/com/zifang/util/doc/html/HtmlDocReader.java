package com.zifang.util.doc.html;

import com.zifang.util.doc.DocBlock;
import com.zifang.util.doc.DocDocument;
import com.zifang.util.doc.DocFormat;
import com.zifang.util.doc.DocFormatDetector;
import com.zifang.util.doc.DocReader;
import com.zifang.util.doc.DocSpan;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * HTML 读取器。零第三方依赖：字符级标签扫描 + 宽容闭合（不要求标签配对），
 * 覆盖标题/段落/内联样式（b i s code a）/列表（ul·ol 嵌套）/表格（th 表头、colspan·rowspan）/
 * 引用/pre 代码块（language-xx）/图片占位/实体解码（命名+十进制+十六进制）/meta 提取。
 * script、style、template、svg、math、head（除 title/meta）整段跳过。
 */
public final class HtmlDocReader implements DocReader {

    private static final Set<String> VOID = Set.of("area", "base", "br", "col", "embed", "hr",
            "img", "input", "link", "meta", "param", "source", "track", "wbr");

    private static final Set<String> RAW_TEXT = Set.of("script", "style", "template", "svg", "math");

    private static final Set<String> BLOCK_CLOSES_PARAGRAPH = Set.of(
            "p", "div", "section", "article", "header", "footer", "aside", "main", "nav",
            "figure", "figcaption", "address", "details", "summary", "fieldset", "form",
            "h1", "h2", "h3", "h4", "h5", "h6",
            "ul", "ol", "li", "table", "thead", "tbody", "tfoot", "tr", "td", "th", "caption",
            "blockquote", "pre", "hr", "dl", "dt", "dd");

    private static final Map<String, String> ENTITIES = Map.ofEntries(
            Map.entry("amp", "&"), Map.entry("lt", "<"), Map.entry("gt", ">"),
            Map.entry("quot", "\""), Map.entry("apos", "'"), Map.entry("nbsp", "\u00a0"),
            Map.entry("copy", "\u00a9"), Map.entry("reg", "\u00ae"), Map.entry("trade", "\u2122"),
            Map.entry("hellip", "\u2026"), Map.entry("mdash", "\u2014"), Map.entry("ndash", "\u2013"),
            Map.entry("lsquo", "\u2018"), Map.entry("rsquo", "\u2019"),
            Map.entry("ldquo", "\u201c"), Map.entry("rdquo", "\u201d"),
            Map.entry("laquo", "\u00ab"), Map.entry("raquo", "\u00bb"),
            Map.entry("deg", "\u00b0"), Map.entry("middot", "\u00b7"),
            Map.entry("times", "\u00d7"), Map.entry("divide", "\u00f7"), Map.entry("plusmn", "\u00b1"),
            Map.entry("sup2", "\u00b2"), Map.entry("sup3", "\u00b3"),
            Map.entry("frac12", "\u00bd"), Map.entry("frac14", "\u00bc"), Map.entry("frac34", "\u00be"),
            Map.entry("euro", "\u20ac"), Map.entry("pound", "\u00a3"), Map.entry("yen", "\u00a5"),
            Map.entry("cent", "\u00a2"), Map.entry("sect", "\u00a7"), Map.entry("para", "\u00b6"),
            Map.entry("bull", "\u2022"), Map.entry("dagger", "\u2020"), Map.entry("Dagger", "\u2021"),
            Map.entry("larr", "\u2190"), Map.entry("rarr", "\u2192"),
            Map.entry("uarr", "\u2191"), Map.entry("darr", "\u2193"), Map.entry("harr", "\u2194"),
            Map.entry("infin", "\u221e"), Map.entry("ne", "\u2260"),
            Map.entry("le", "\u2264"), Map.entry("ge", "\u2265"),
            Map.entry("eacute", "\u00e9"), Map.entry("egrave", "\u00e8"),
            Map.entry("ntilde", "\u00f1"), Map.entry("uuml", "\u00fc"), Map.entry("auml", "\u00e4"),
            Map.entry("ouml", "\u00f6"), Map.entry("szlig", "\u00df"), Map.entry("ccedil", "\u00e7"));

    @Override
    public DocFormat format() {
        return DocFormat.HTML;
    }

    @Override
    public boolean supports(byte[] data) {
        return DocFormatDetector.isHtml(data);
    }

    @Override
    public DocDocument read(byte[] data) throws IOException {
        DocDocument doc = new DocDocument();
        new Parser(decode(data), doc).parse();
        return doc;
    }

    /** BOM 优先，其次 meta charset 声明，最后 UTF-8（坏字节替换不抛错）。 */
    private static String decode(byte[] data) {
        if (data.length >= 3 && (data[0] & 0xFF) == 0xEF && (data[1] & 0xFF) == 0xBB
                && (data[2] & 0xFF) == 0xBF) {
            return new String(data, 3, data.length - 3, StandardCharsets.UTF_8);
        }
        String probe = new String(data, 0, Math.min(data.length, 4096), StandardCharsets.ISO_8859_1);
        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("charset\\s*=\\s*[\"']?([\\w-]+)", java.util.regex.Pattern.CASE_INSENSITIVE)
                .matcher(probe);
        if (matcher.find()) {
            try {
                return new String(data, Charset.forName(matcher.group(1)));
            } catch (Exception ignore) {
                // 声明了不认识的 charset，落回 UTF-8
            }
        }
        return new String(data, StandardCharsets.UTF_8);
    }

    /** 解析状态机。 */
    private static final class Parser {

        private final String src;
        private final DocDocument doc;
        private int pos;

        private final List<DocSpan> spans = new ArrayList<>();
        private int boldDepth;
        private int italicDepth;
        private int strikeDepth;
        private int monoDepth;
        private final Deque<String> links = new ArrayDeque<>();

        private boolean inHead;
        private boolean inTitle;
        private final StringBuilder titleText = new StringBuilder();

        private int headingLevel;

        private final Deque<Boolean> listStack = new ArrayDeque<>();
        private boolean inLi;

        private boolean inQuote;
        private final List<List<DocSpan>> quoteParas = new ArrayList<>();

        private boolean inCell;
        private boolean rowHasTh;
        private int cellColspan = 1;
        private int cellRowspan = 1;
        private final List<DocBlock.Table.Cell> rowCells = new ArrayList<>();
        private final List<DocBlock> cellBlocks = new ArrayList<>();
        private final List<DocBlock.Table.Row> tableRows = new ArrayList<>();

        private boolean inPre;
        private String preLanguage;
        private final StringBuilder preText = new StringBuilder();

        Parser(String src, DocDocument doc) {
            this.src = src;
            this.doc = doc;
        }

        void parse() {
            while (pos < src.length()) {
                char c = src.charAt(pos);
                if (inPre) {
                    readPreChar();
                    continue;
                }
                if (c == '<') {
                    readTag();
                    continue;
                }
                if (c == '&') {
                    String entity = readEntity();
                    if (entity != null) {
                        appendText(entity);
                    } else {
                        appendText("&");
                        pos++;
                    }
                    continue;
                }
                if (c == '\r' || c == '\n') {
                    appendText(" ");
                    pos++;
                    continue;
                }
                appendText(String.valueOf(c));
                pos++;
            }
            finish();
        }

        private void finish() {
            closeCell();
            flushRow();
            emitTable();
            closeItem();
            flushQuote();
            flushParagraph();
            if (inPre && preText.length() > 0) {
                doc.add(new DocBlock.CodeBlock(preLanguage, preText.toString()));
            }
            if (!titleText.isEmpty()) {
                doc.getMeta().setTitle(titleText.toString().strip());
            }
        }

        /* ---------- 标签 ---------- */

        private void readTag() {
            if (src.startsWith("<!--", pos)) {
                int end = src.indexOf("-->", pos + 4);
                pos = end < 0 ? src.length() : end + 3;
                return;
            }
            if (src.startsWith("<!", pos)) {
                int end = src.indexOf('>', pos);
                pos = end < 0 ? src.length() : end + 1;
                return;
            }
            if (src.startsWith("</", pos)) {
                int end = src.indexOf('>', pos);
                if (end < 0) {
                    pos = src.length();
                    return;
                }
                String name = src.substring(pos + 2, end).strip().toLowerCase();
                pos = end + 1;
                closeTag(name);
                return;
            }
            int nameStart = pos + 1;
            int i = nameStart;
            while (i < src.length() && isNameChar(src.charAt(i))) {
                i++;
            }
            if (i == nameStart) {
                appendText("<");
                pos++;
                return;
            }
            String name = src.substring(nameStart, i).toLowerCase();
            Map<String, String> attrs = readAttrs();
            openTag(name, attrs);
        }

        private Map<String, String> readAttrs() {
            Map<String, String> attrs = new LinkedHashMap<>();
            while (pos < src.length()) {
                char c = src.charAt(pos);
                if (c == '>') {
                    pos++;
                    return attrs;
                }
                if (c == '/' && pos + 1 < src.length() && src.charAt(pos + 1) == '>') {
                    pos += 2;
                    attrs.put("/selfclosed", "1");
                    return attrs;
                }
                if (Character.isWhitespace(c)) {
                    pos++;
                    continue;
                }
                int nameStart = pos;
                while (pos < src.length() && src.charAt(pos) != '=' && src.charAt(pos) != '>'
                        && src.charAt(pos) != '/' && !Character.isWhitespace(src.charAt(pos))) {
                    pos++;
                }
                String attrName = src.substring(nameStart, pos).toLowerCase();
                String value = "";
                if (pos < src.length() && src.charAt(pos) == '=') {
                    pos++;
                    if (pos < src.length() && (src.charAt(pos) == '"' || src.charAt(pos) == '\'')) {
                        char quote = src.charAt(pos);
                        pos++;
                        int end = src.indexOf(quote, pos);
                        if (end < 0) {
                            value = src.substring(pos);
                            pos = src.length();
                        } else {
                            value = src.substring(pos, end);
                            pos = end + 1;
                        }
                    } else {
                        int end = pos;
                        while (end < src.length() && !Character.isWhitespace(src.charAt(end))
                                && src.charAt(end) != '>') {
                            end++;
                        }
                        value = src.substring(pos, end);
                        pos = end;
                    }
                }
                if (!attrName.isEmpty()) {
                    attrs.put(attrName, value);
                }
            }
            return attrs;
        }

        private void openTag(String name, Map<String, String> attrs) {
            boolean selfClosed = attrs.containsKey("/selfclosed");
            // 块级开标签先断前文（必须在置状态之前，否则 <h1> 置级后立即被空 flush 清零）
            if (BLOCK_CLOSES_PARAGRAPH.contains(name)) {
                flushParagraph();
            }
            switch (name) {
                case "meta" -> readMeta(attrs);
                case "title" -> inTitle = true;
                case "head" -> inHead = true;
                case "br" -> appendText("\n");
                case "hr" -> handleHr();
                case "img" -> handleImg(attrs);
                case "a" -> links.push(attrs.getOrDefault("href", "").strip());
                case "blockquote" -> beginQuote();
                case "pre" -> beginPre(attrs);
                case "table" -> beginTable();
                case "tr" -> beginRow();
                case "td" -> beginCell(attrs, false);
                case "th" -> beginCell(attrs, true);
                case "ul" -> listStack.push(false);
                case "ol" -> listStack.push(true);
                case "li" -> beginItem();
                case "b", "strong" -> boldDepth++;
                case "i", "em" -> italicDepth++;
                case "s", "strike", "del" -> strikeDepth++;
                case "code", "tt", "kbd", "samp" -> monoDepth++;
                default -> {
                    if (name.length() == 2 && name.charAt(0) == 'h' && name.charAt(1) >= '1'
                            && name.charAt(1) <= '6') {
                        beginHeading(name.charAt(1) - '0');
                    }
                }
            }
            if (RAW_TEXT.contains(name)) {
                skipRawText(name);
                return;
            }
            if (selfClosed || VOID.contains(name)) {
                closeTag(name);
            }
        }

        private void closeTag(String name) {
            switch (name) {
                case "title" -> {
                    inTitle = false;
                    doc.getMeta().setTitle(titleText.toString().strip());
                }
                case "head" -> inHead = false;
                case "a" -> links.pollLast();
                case "b", "strong" -> boldDepth = Math.max(0, boldDepth - 1);
                case "i", "em" -> italicDepth = Math.max(0, italicDepth - 1);
                case "s", "strike", "del" -> strikeDepth = Math.max(0, strikeDepth - 1);
                case "code", "tt", "kbd", "samp" -> monoDepth = Math.max(0, monoDepth - 1);
                case "blockquote" -> endQuote();
                case "pre" -> endPre();
                case "tr" -> endRow();
                case "td", "th" -> closeCell();
                case "table" -> endTable();
                case "ul", "ol" -> {
                    closeItem();
                    listStack.pollLast();
                }
                case "li" -> closeItem();
                case "p" -> flushParagraph();
                default -> {
                    if (name.length() == 2 && name.charAt(0) == 'h' && name.charAt(1) >= '1'
                            && name.charAt(1) <= '6') {
                        endHeading();
                    }
                }
            }
        }

        private void skipRawText(String name) {
            String close = "</" + name;
            int at = pos;
            while (at < src.length()) {
                int idx = src.toLowerCase().indexOf(close, at);
                if (idx < 0) {
                    pos = src.length();
                    return;
                }
                int end = src.indexOf('>', idx);
                if (end < 0) {
                    pos = src.length();
                    return;
                }
                // 确认 close 后面跟的是 '>' 或空白（排除 "</scriptx" 这类假匹配）
                if (end == idx + close.length()
                        || Character.isWhitespace(src.charAt(idx + close.length()))) {
                    pos = end + 1;
                    return;
                }
                at = idx + 1;
            }
        }

        private void readMeta(Map<String, String> attrs) {
            String name = attrs.getOrDefault("name", attrs.getOrDefault("property", ""));
            String content = attrs.get("content");
            if (content != null && !content.isBlank()) {
                switch (name.toLowerCase()) {
                    case "author" -> doc.getMeta().setAuthor(content.strip());
                    case "keywords" -> doc.getMeta().setKeywords(content.strip());
                    case "description" -> doc.getMeta().setSubject(content.strip());
                    case "generator" -> doc.getMeta().setProducer(content.strip());
                    default -> {
                    }
                }
            }
            if (attrs.get("charset") != null) {
                doc.getMeta().putExtra("charset", attrs.get("charset"));
            }
        }

        /* ---------- 标题 ---------- */

        private void beginHeading(int level) {
            flushParagraph();
            headingLevel = level;
        }

        private void endHeading() {
            flushParagraph();
        }

        /* ---------- 列表 ---------- */

        private void beginItem() {
            closeItem();
            inLi = true;
        }

        private void closeItem() {
            if (!inLi) {
                return;
            }
            inLi = false;
            if (hasText(spans)) {
                boolean ordered = !listStack.isEmpty() && listStack.peek();
                doc.add(new DocBlock.ListItem(ordered, Math.max(0, listStack.size() - 1),
                        merge(spans)));
            }
            spans.clear();
        }

        /* ---------- 表格 ---------- */

        private void beginTable() {
            closeCell();
            flushRow();
            emitTable();
        }

        private void beginRow() {
            closeCell();
            flushRow();
            rowHasTh = false;
        }

        private void beginCell(Map<String, String> attrs, boolean th) {
            closeCell();
            flushParagraph();
            inCell = true;
            rowHasTh = rowHasTh || th;
            cellColspan = parseInt(attrs.get("colspan"), 1);
            cellRowspan = parseInt(attrs.get("rowspan"), 1);
        }

        private void closeCell() {
            if (!inCell) {
                return;
            }
            inCell = false;
            if (hasText(spans)) {
                cellBlocks.add(new DocBlock.Paragraph(merge(spans)));
            }
            spans.clear();
            if (!cellBlocks.isEmpty()) {
                rowCells.add(new DocBlock.Table.Cell(new ArrayList<>(cellBlocks),
                        cellColspan, cellRowspan));
            }
            cellBlocks.clear();
            cellColspan = 1;
            cellRowspan = 1;
        }

        private void endRow() {
            closeCell();
            flushRow();
        }

        /** 把累积的行落成 Row；header 行 = 行内出现过 th。 */
        private void flushRow() {
            if (!rowCells.isEmpty()) {
                tableRows.add(new DocBlock.Table.Row(new ArrayList<>(rowCells), rowHasTh));
            }
            rowCells.clear();
            rowHasTh = false;
        }

        private void endTable() {
            closeCell();
            flushRow();
            emitTable();
        }

        private void emitTable() {
            if (!tableRows.isEmpty()) {
                doc.add(new DocBlock.Table(new ArrayList<>(tableRows)));
            }
            tableRows.clear();
        }

        /* ---------- 引用 ---------- */

        private void beginQuote() {
            flushParagraph();
            inQuote = true;
            quoteParas.clear();
        }

        private void endQuote() {
            flushParagraph();
            inQuote = false;
            if (quoteParas.isEmpty()) {
                return;
            }
            List<DocSpan> combined = new ArrayList<>();
            for (int i = 0; i < quoteParas.size(); i++) {
                if (i > 0) {
                    combined.add(DocSpan.plain("\n"));
                }
                combined.addAll(quoteParas.get(i));
            }
            doc.add(new DocBlock.Quote(merge(combined)));
            quoteParas.clear();
        }

        private void flushQuote() {
            inQuote = false;
        }

        /* ---------- pre ---------- */

        private void beginPre(Map<String, String> attrs) {
            flushParagraph();
            inPre = true;
            preText.setLength(0);
            preLanguage = null;
            String cls = attrs.getOrDefault("class", "");
            java.util.regex.Matcher matcher = java.util.regex.Pattern
                    .compile("language-([\\w+-]+)").matcher(cls);
            if (matcher.find()) {
                preLanguage = matcher.group(1);
            }
        }

        private void readPreChar() {
            if (src.length() - pos >= 5 && src.substring(pos, pos + 5).equalsIgnoreCase("</pre")) {
                int end = src.indexOf('>', pos);
                pos = end < 0 ? src.length() : end + 1;
                endPre();
                return;
            }
            char c = src.charAt(pos);
            if (c == '&') {
                String entity = readEntity();
                preText.append(entity != null ? entity : "&");
                if (entity == null) {
                    pos++;
                }
                return;
            }
            if (c == '<') {
                // pre 内标签只吸收不入文；language-* 从 code 等内层标签的 class 里补收
                int end = src.indexOf('>', pos);
                if (end < 0) {
                    pos = src.length();
                    return;
                }
                String tag = src.substring(pos, end + 1);
                java.util.regex.Matcher matcher = java.util.regex.Pattern
                        .compile("language-([\\w+-]+)").matcher(tag);
                if (matcher.find() && preLanguage == null) {
                    preLanguage = matcher.group(1);
                }
                pos = end + 1;
                return;
            }
            preText.append(c);
            pos++;
        }

        private void endPre() {
            if (inPre) {
                inPre = false;
                if (preText.length() > 0) {
                    doc.add(new DocBlock.CodeBlock(preLanguage,
                            preText.toString().replaceAll("\\n$", "")));
                }
                preText.setLength(0);
            }
        }

        /* ---------- hr / img ---------- */

        private void handleHr() {
            flushParagraph();
            if (!inQuote && !inCell && !inLi) {
                doc.add(new DocBlock.Paragraph("---"));
            }
        }

        private void handleImg(Map<String, String> attrs) {
            String srcAttr = attrs.getOrDefault("src", "");
            String alt = attrs.getOrDefault("alt", "").strip();
            String name = alt.isEmpty() ? basename(srcAttr) : alt;
            DocBlock.Image image = new DocBlock.Image(name, mimeByExtension(srcAttr), null,
                    parseIntOrNull(attrs.get("width")), parseIntOrNull(attrs.get("height")));
            if (inCell) {
                flushParagraph();
                cellBlocks.add(image);
            } else if (inLi || inQuote) {
                appendText("[" + (name.isEmpty() ? "图片" : name) + "]");
            } else {
                flushParagraph();
                doc.add(image);
            }
        }

        private static String basename(String url) {
            int at = url.lastIndexOf('/');
            String name = at < 0 ? url : url.substring(at + 1);
            int query = name.indexOf('?');
            return query < 0 ? name : name.substring(0, query);
        }

        private static String mimeByExtension(String url) {
            String lower = basename(url).toLowerCase();
            if (lower.endsWith(".png")) {
                return "image/png";
            }
            if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
                return "image/jpeg";
            }
            if (lower.endsWith(".gif")) {
                return "image/gif";
            }
            if (lower.endsWith(".svg")) {
                return "image/svg+xml";
            }
            if (lower.endsWith(".webp")) {
                return "image/webp";
            }
            if (lower.endsWith(".bmp")) {
                return "image/bmp";
            }
            return null;
        }

        /* ---------- 文本 ---------- */

        private void appendText(String text) {
            if ((inHead && !inTitle) || text.isEmpty()) {
                return;
            }
            if (inTitle) {
                titleText.append(text);
                return;
            }
            boolean bold = boldDepth > 0;
            boolean italic = italicDepth > 0;
            boolean strike = strikeDepth > 0;
            boolean mono = monoDepth > 0;
            String link = links.isEmpty() ? null : links.peek();
            DocSpan last = spans.isEmpty() ? null : spans.get(spans.size() - 1);
            if (last != null && last.bold() == bold && last.italic() == italic
                    && last.strike() == strike && last.mono() == mono
                    && java.util.Objects.equals(last.link(), link)) {
                spans.set(spans.size() - 1, new DocSpan(last.text() + text, bold, italic, mono,
                        strike, link));
            } else {
                spans.add(new DocSpan(text, bold, italic, mono, strike, link));
            }
        }

        /** 段落落块：按上下文进 引用 / 单元格 / 列表项 / 标题 / 普通段落。 */
        private void flushParagraph() {
            boolean blank = !hasText(spans);
            if (inCell) {
                if (!blank) {
                    cellBlocks.add(new DocBlock.Paragraph(merge(spans)));
                }
                spans.clear();
                return;
            }
            if (inQuote) {
                if (!blank) {
                    quoteParas.add(merge(spans));
                }
                spans.clear();
                return;
            }
            if (inLi) {
                if (!blank) {
                    boolean ordered = !listStack.isEmpty() && listStack.peek();
                    doc.add(new DocBlock.ListItem(ordered, Math.max(0, listStack.size() - 1),
                            merge(spans)));
                }
                spans.clear();
                inLi = false;
                return;
            }
            if (headingLevel > 0) {
                if (!blank) {
                    doc.add(new DocBlock.Heading(headingLevel, merge(spans)));
                }
                headingLevel = 0;
                spans.clear();
                return;
            }
            if (!blank) {
                // 表格没闭合就被普通段落打断：先落表，保住文档顺序
                emitTable();
                doc.add(new DocBlock.Paragraph(merge(spans)));
            }
            spans.clear();
        }

        /* ---------- 工具 ---------- */

        private static boolean hasText(List<DocSpan> spans) {
            for (DocSpan span : spans) {
                if (!span.text().isBlank()) {
                    return true;
                }
            }
            return false;
        }

        /** 相邻同样式的 span 合并，再整体裁掉首尾空白。 */
        private static List<DocSpan> merge(List<DocSpan> input) {
            List<DocSpan> result = new ArrayList<>();
            for (DocSpan span : input) {
                String text = span.text();
                if (result.isEmpty()) {
                    text = text.replaceFirst("^\\s+", "");
                }
                if (!result.isEmpty()) {
                    DocSpan last = result.get(result.size() - 1);
                    if (last.bold() == span.bold() && last.italic() == span.italic()
                            && last.strike() == span.strike() && last.mono() == span.mono()
                            && java.util.Objects.equals(last.link(), span.link())) {
                        result.set(result.size() - 1, new DocSpan(last.text() + text, span.bold(),
                                span.italic(), span.mono(), span.strike(), span.link()));
                        continue;
                    }
                }
                result.add(new DocSpan(text, span.bold(), span.italic(), span.mono(),
                        span.strike(), span.link()));
            }
            if (!result.isEmpty()) {
                DocSpan last = result.get(result.size() - 1);
                if (last.text().isBlank()) {
                    result.remove(result.size() - 1);
                } else {
                    result.set(result.size() - 1,
                            new DocSpan(last.text().replaceFirst("\\s+$", ""), last.bold(),
                                    last.italic(), last.mono(), last.strike(), last.link()));
                }
            }
            return result;
        }

        /** '&' 后的实体；不认识返回 null（'&' 按字面量处理）。 */
        private String readEntity() {
            int semi = src.indexOf(';', pos);
            if (semi < 0 || semi - pos > 12) {
                return null;
            }
            String body = src.substring(pos + 1, semi);
            if (body.isEmpty()) {
                return null;
            }
            if (body.charAt(0) == '#') {
                try {
                    int code = body.length() > 1 && (body.charAt(1) == 'x' || body.charAt(1) == 'X')
                            ? Integer.parseInt(body.substring(2), 16)
                            : Integer.parseInt(body.substring(1));
                    if (code > 0 && code <= Character.MAX_CODE_POINT) {
                        pos = semi + 1;
                        char[] chars = Character.toChars(code);
                        return new String(chars);
                    }
                } catch (NumberFormatException ignore) {
                    return null;
                }
                return null;
            }
            String named = ENTITIES.get(body);
            if (named != null) {
                pos = semi + 1;
            }
            return named;
        }

        private static boolean isNameChar(char c) {
            return Character.isLetterOrDigit(c) || c == '-' || c == '_' || c == ':';
        }

        private static int parseInt(String value, int fallback) {
            Integer parsed = parseIntOrNull(value);
            return parsed == null ? fallback : parsed;
        }

        private static Integer parseIntOrNull(String value) {
            if (value == null || value.isBlank()) {
                return null;
            }
            try {
                return Integer.parseInt(value.strip());
            } catch (NumberFormatException e) {
                return null;
            }
        }
    }
}
