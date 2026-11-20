package com.zifang.util.doc.odf;

import com.zifang.util.doc.DocBlock;
import com.zifang.util.doc.DocSpan;
import com.zifang.util.doc.xml.XmlSupport;
import com.zifang.util.doc.zip.ZipSupport;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** ODF 共用：容器解包、content.xml 遍历、段落文本与样式、图片提取。 */
final class OdfSupport {

    static final String MIME_TEXT = "application/vnd.oasis.opendocument.text";
    static final String MIME_SPREADSHEET = "application/vnd.oasis.opendocument.spreadsheet";
    static final String MIME_PRESENTATION = "application/vnd.oasis.opendocument.presentation";

    private OdfSupport() {
    }

    static Map<String, byte[]> entries(byte[] data) throws IOException {
        return ZipSupport.read(data);
    }

    static String mimetype(Map<String, byte[]> entries) {
        byte[] raw = entries.get("mimetype");
        return raw == null ? null : new String(raw, java.nio.charset.StandardCharsets.US_ASCII).trim();
    }

    /** content.xml 的根元素。 */
    static Element contentRoot(Map<String, byte[]> entries) throws IOException {
        byte[] xml = entries.get("content.xml");
        if (xml == null) {
            return null;
        }
        return XmlSupport.parse(xml).getDocumentElement();
    }

    /** office:body 下的指定子元素（text / spreadsheet / presentation）。 */
    static Element bodyPart(Element contentRoot, String localName) {
        if (contentRoot == null) {
            return null;
        }
        Element body = XmlSupport.child(contentRoot, "body");
        return body == null ? null : XmlSupport.child(body, localName);
    }

    static List<Element> children(Element parent, String localName) {
        return XmlSupport.children(parent, localName);
    }

    /** 全部直接子元素（保持文档顺序）。 */
    static List<Element> childElements(Element parent) {
        List<Element> result = new ArrayList<>();
        if (parent == null) {
            return result;
        }
        NodeList nodes = parent.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                result.add((Element) node);
            }
        }
        return result;
    }

    static String localName(Element element) {
        String name = element.getLocalName();
        return name != null ? name : element.getTagName();
    }

    static Element child(Element parent, String localName) {
        return XmlSupport.child(parent, localName);
    }

    /** 按 local-name 取属性（忽略命名空间前缀）。 */
    static String attr(Element element, String localName) {
        return element == null ? null : XmlSupport.attr(element, localName);
    }

    static int intAttr(Element element, String localName, int fallback) {
        String value = attr(element, localName);
        if (value == null) {
            return fallback;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    /** 元素内直接文本。 */
    static String text(Element element) {
        return element == null ? "" : XmlSupport.text(element);
    }

    /**
     * 段落内文本：处理 text:span（样式）、text:s（n 个空格）、text:tab、text:line-break、
     * text:a（超链接）、以及纯文本节点。
     */
    static List<DocSpan> paragraphSpans(Element paragraph, Map<String, Style> styles) {
        List<DocSpan> spans = new ArrayList<>();
        collectSpans(paragraph, styles, spans, null, false, false, false);
        return merge(spans);
    }

    private static void collectSpans(Element element, Map<String, Style> styles, List<DocSpan> out,
                                     String link, boolean bold, boolean italic, boolean strike) {
        NodeList nodes = element.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            switch (node.getNodeType()) {
                case Node.TEXT_NODE, Node.CDATA_SECTION_NODE -> {
                    String text = node.getNodeValue();
                    if (text != null && !text.isEmpty()) {
                        out.add(span(text, bold, italic, strike, link));
                    }
                }
                case Node.ELEMENT_NODE -> {
                    Element child = (Element) node;
                    String name = XmlSupport.localName(child);
                    switch (name) {
                        case "span" -> {
                            Style style = styles == null ? null : styles.get(attr(child, "style-name"));
                            boolean spanBold = bold || (style != null && style.bold);
                            boolean spanItalic = italic || (style != null && style.italic);
                            boolean spanStrike = strike || (style != null && style.strike);
                            collectSpans(child, styles, out, link, spanBold, spanItalic, spanStrike);
                        }
                        case "a" -> {
                            String href = attr(child, "href");
                            collectSpans(child, styles, out, href != null ? href : link, bold, italic, strike);
                        }
                        case "s" -> {
                            int count = intAttr(child, "c", 1);
                            out.add(span(" ".repeat(Math.max(0, count)), bold, italic, strike, link));
                        }
                        case "tab" -> out.add(span("\t", bold, italic, strike, link));
                        case "line-break" -> out.add(span("\n", bold, italic, strike, link));
                        case "note", "bookmark", "bookmark-start", "bookmark-end",
                                "meta", "annotation" -> {
                        }
                        default -> collectSpans(child, styles, out, link, bold, italic, strike);
                    }
                }
                default -> {
                }
            }
        }
    }

    private static DocSpan span(String text, boolean bold, boolean italic, boolean strike, String link) {
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
        if (link != null) {
            span = span.withLink(link);
        }
        return span;
    }

    /** 相邻同样式片段合并。 */
    static List<DocSpan> merge(List<DocSpan> input) {
        List<DocSpan> result = new ArrayList<>();
        for (DocSpan span : input) {
            if (!result.isEmpty()) {
                DocSpan last = result.get(result.size() - 1);
                if (last.bold() == span.bold() && last.italic() == span.italic()
                        && last.strike() == span.strike() && last.mono() == span.mono()
                        && Objects.equals(last.link(), span.link())) {
                    result.set(result.size() - 1, new DocSpan(last.text() + span.text(),
                            last.bold(), last.italic(), last.mono(), last.strike(), last.link()));
                    continue;
                }
            }
            result.add(span);
        }
        return result;
    }

    /**
     * 样式表：style:name → (bold, italic, strike)。
     * 名称来源：styles.xml 的 office:styles 与 content.xml 的 office:automatic-styles；
     * 字符样式可经 style:parent-style-name 继承。
     */
    static Map<String, Style> parseStyles(Map<String, byte[]> entries, Element contentRoot) {
        Map<String, Raw> raw = new HashMap<>();
        byte[] xml = entries.get("styles.xml");
        if (xml != null) {
            try {
                collectStyles(XmlSupport.parse(xml).getDocumentElement(), raw);
            } catch (IOException e) {
                // 样式表损坏时按无样式处理
            }
        }
        if (contentRoot != null) {
            collectStyles(contentRoot, raw);
        }
        Map<String, Style> resolved = new HashMap<>();
        for (String name : raw.keySet()) {
            resolved.put(name, resolve(name, raw, new HashMap<>()));
        }
        return resolved;
    }

    private static void collectStyles(Element root, Map<String, Raw> out) {
        for (Element style : XmlSupport.descendants(root, "style")) {
            String name = attr(style, "name");
            if (name == null || out.containsKey(name)) {
                continue;
            }
            Element properties = propertiesOf(style);
            Raw value = new Raw();
            value.parent = attr(style, "parent-style-name");
            if (properties != null) {
                value.bold = "bold".equals(attr(properties, "font-weight"))
                        || "bolder".equals(attr(properties, "font-weight"));
                value.italic = "italic".equals(attr(properties, "font-style"))
                        || "oblique".equals(attr(properties, "font-style"));
                value.strike = attr(properties, "text-line-through-style") != null
                        && !"none".equals(attr(properties, "text-line-through-style"));
            }
            out.put(name, value);
        }
    }

    /** style:style 的属性子元素（style:text-properties / style:paragraph-properties 等）。 */
    private static Element propertiesOf(Element style) {
        for (Element child : childElements(style)) {
            String name = localName(child);
            if (name.endsWith("-properties")) {
                return child;
            }
        }
        return null;
    }

    private static Style resolve(String name, Map<String, Raw> raw, Map<String, Style> cache) {
        Style cached = cache.get(name);
        if (cached != null) {
            return cached;
        }
        Raw value = raw.get(name);
        if (value == null) {
            return new Style(false, false, false);
        }
        Style parent = value.parent == null ? null : resolve(value.parent, raw, cache);
        Style style = new Style(
                value.bold || (parent != null && parent.bold),
                value.italic || (parent != null && parent.italic),
                value.strike || (parent != null && parent.strike));
        cache.put(name, style);
        return style;
    }

    /**
     * 文本容器 → 块。识别 p / h / list / table / frame（图片与文本框）/ 组合形状。
     * 供 ODT 正文与 ODP 形状共用。
     */
    static void appendTextBlocks(Element container, List<DocBlock> out,
                                 Map<String, Style> styles, Map<String, byte[]> entries, int listLevel) {
        if (container == null) {
            return;
        }
        for (Element node : childElements(container)) {
            switch (localName(node)) {
                case "p" -> {
                    List<DocSpan> spans = paragraphSpans(node, styles);
                    if (!spans.isEmpty()) {
                        out.add(new DocBlock.Paragraph(spans));
                    }
                }
                case "h" -> {
                    List<DocSpan> spans = paragraphSpans(node, styles);
                    if (!spans.isEmpty()) {
                        int level = Math.min(6, Math.max(1, intAttr(node, "outline-level", 1)));
                        out.add(new DocBlock.Heading(level, spans));
                    }
                }
                case "list" -> {
                    for (Element item : children(node, "list-item")) {
                        appendListItem(item, out, styles, entries, listLevel);
                    }
                }
                case "list-item" -> appendListItem(node, out, styles, entries, listLevel);
                case "table" -> out.add(readTable(node, styles));
                case "section", "text-box", "custom-shape", "g", "frame" ->
                        appendTextBlocks(node, out, styles, entries, listLevel);
                case "image" -> {
                    DocBlock.Image image = image(node, entries);
                    if (image != null) {
                        out.add(image);
                    }
                }
                case "notes" -> {
                    // 备注单独处理，见 ODP 读取器
                }
                default -> {
                    // 其它元素（动画、脚本等）忽略
                }
            }
        }
    }

    private static void appendListItem(Element item, List<DocBlock> out, Map<String, Style> styles,
                                       Map<String, byte[]> entries, int level) {
        for (Element paragraph : children(item, "p")) {
            List<DocSpan> spans = paragraphSpans(paragraph, styles);
            if (!spans.isEmpty()) {
                out.add(new DocBlock.ListItem(false, level, spans));
            }
        }
        for (Element paragraph : children(item, "h")) {
            List<DocSpan> spans = paragraphSpans(paragraph, styles);
            if (!spans.isEmpty()) {
                out.add(new DocBlock.ListItem(false, level, spans));
            }
        }
        for (Element nested : children(item, "list")) {
            for (Element inner : children(nested, "list-item")) {
                appendListItem(inner, out, styles, entries, level + 1);
            }
        }
    }

    /** 表格：table:table → table:table-row → table:table-cell。 */
    static DocBlock.Table readTable(Element table, Map<String, Style> styles) {
        List<DocBlock.Table.Row> rows = new ArrayList<>();
        for (Element row : children(table, "table-row")) {
            List<DocBlock.Table.Cell> cells = new ArrayList<>();
            for (Element cell : children(row, "table-cell")) {
                List<DocBlock> blocks = new ArrayList<>();
                for (Element paragraph : children(cell, "p")) {
                    List<DocSpan> spans = paragraphSpans(paragraph, styles);
                    if (!spans.isEmpty()) {
                        blocks.add(new DocBlock.Paragraph(spans));
                    }
                }
                if (blocks.isEmpty()) {
                    blocks.add(new DocBlock.Paragraph(""));
                }
                cells.add(new DocBlock.Table.Cell(blocks, 1, 1));
            }
            if (!cells.isEmpty()) {
                rows.add(new DocBlock.Table.Row(cells, rows.isEmpty()));
            }
        }
        return new DocBlock.Table(rows);
    }

    /** 图片：draw:image 的 xlink:href 指向包内条目。 */
    static DocBlock.Image image(Element drawImage, Map<String, byte[]> entries) {
        String href = attr(drawImage, "href");
        if (href == null) {
            return null;
        }
        String path = href.startsWith("./") ? href.substring(2) : href;
        byte[] bytes = entries.get(path);
        if (bytes == null) {
            return null;
        }
        return new DocBlock.Image(path.substring(path.lastIndexOf('/') + 1), mimeOf(path), bytes, null, null);
    }

    static String mimeOf(String path) {
        String lower = path.toLowerCase();
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
        if (lower.endsWith(".bmp")) {
            return "image/bmp";
        }
        return "application/octet-stream";
    }

    /** 文本样式。 */
    static final class Style {
        final boolean bold;
        final boolean italic;
        final boolean strike;

        Style(boolean bold, boolean italic, boolean strike) {
            this.bold = bold;
            this.italic = italic;
            this.strike = strike;
        }
    }

    /** 样式表原始项（保留父样式引用）。 */
    private static final class Raw {
        String parent;
        boolean bold;
        boolean italic;
        boolean strike;
    }
}
