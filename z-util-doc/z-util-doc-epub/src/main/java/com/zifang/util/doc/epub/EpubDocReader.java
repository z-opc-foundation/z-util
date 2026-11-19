package com.zifang.util.doc.epub;

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
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/** EPUB 读取器：纯 JDK 解析 container.xml → OPF → 按 spine 顺序读 XHTML，零第三方依赖。 */
public class EpubDocReader implements DocReader {

    /** 块级 XHTML 标签：行内收集在这些标签处被冲刷成 Paragraph。 */
    private static final java.util.Set<String> BLOCK_TAGS = java.util.Set.of(
            "p", "h1", "h2", "h3", "h4", "h5", "h6", "li", "blockquote", "pre", "table",
            "div", "ul", "ol", "section", "article", "figure", "header", "footer", "tr", "td", "th");

    @Override
    public DocFormat format() {
        return DocFormat.EPUB;
    }

    @Override
    public boolean supports(byte[] data) {
        return DocFormatDetector.isZip(data) && DocFormatDetector.detectZipContainer(data) == DocFormat.EPUB;
    }

    @Override
    public DocDocument read(byte[] data) throws IOException {
        Map<String, byte[]> entries = readZip(data);
        DocDocument doc = new DocDocument();
        String opfPath = findOpfPath(entries);
        if (opfPath == null) {
            return doc;
        }
        Element opf = parse(opfPath != null ? entries.get(opfPath) : null, null);
        if (opf == null) {
            return doc;
        }
        String baseDir = opfPath.contains("/") ? opfPath.substring(0, opfPath.lastIndexOf('/') + 1) : "";
        Map<String, String> hrefs = new LinkedHashMap<>();
        List<String> spine = new ArrayList<>();
        for (Node node : elements(opf)) {
            switch (local(node)) {
                case "metadata" -> readMetadata((Element) node, doc);
                case "manifest" -> {
                    for (Node item : elements(node)) {
                        if (local(item).equals("item")) {
                            String id = attr((Element) item, "id");
                            String href = attr((Element) item, "href");
                            if (id != null && href != null) {
                                hrefs.put(id, href);
                            }
                        }
                    }
                }
                case "spine" -> {
                    for (Node itemref : elements(node)) {
                        if (local(itemref).equals("itemref")) {
                            String idref = attr((Element) itemref, "idref");
                            if (idref != null) {
                                spine.add(idref);
                            }
                        }
                    }
                }
                default -> {
                }
            }
        }
        for (String idref : spine) {
            String href = hrefs.get(idref);
            if (href == null) {
                continue;
            }
            byte[] xhtml = entries.get(baseDir + href);
            if (xhtml == null) {
                continue;
            }
            Element root = parse(xhtml, "html");
            if (root == null) {
                continue;
            }
            Element body = firstByLocal(root, "body");
            if (body == null) {
                continue;
            }
            int before = doc.getBlocks().size();
            readBlock(body, doc, entries, baseDir, 0, false, new ArrayList<>());
            // 章节首块不是 Heading 时补插章节标题（xhtml 的 title 或文件名）
            String title = chapterTitle(root);
            if (title != null && doc.getBlocks().size() > before
                    && !(doc.getBlocks().get(before) instanceof DocBlock.Heading)) {
                doc.getBlocks().add(before, new DocBlock.Heading(1, title));
            }
        }
        return doc;
    }

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

    /** META-INF/container.xml → rootfile 的 full-path。 */
    private String findOpfPath(Map<String, byte[]> entries) {
        Element container = parse(entries.get("META-INF/container.xml"), "container");
        if (container == null) {
            return null;
        }
        Element rootfiles = firstByLocal(container, "rootfiles");
        if (rootfiles == null) {
            return null;
        }
        for (Node rootfile : elements(rootfiles)) {
            if (local(rootfile).equals("rootfile")) {
                return attr((Element) rootfile, "full-path");
            }
        }
        return null;
    }

    /** OPF metadata：title/creator/subject/date；publisher 进 extra。 */
    private void readMetadata(Element metadata, DocDocument doc) {
        for (Node node : elements(metadata)) {
            String text = node.getTextContent().trim();
            switch (local(node)) {
                case "title" -> doc.getMeta().setTitle(text);
                case "creator" -> doc.getMeta().setAuthor(text);
                case "subject" -> doc.getMeta().setSubject(text);
                case "publisher" -> doc.getMeta().putExtra("publisher", text);
                case "date" -> parseDate(text).ifPresent(d -> doc.getMeta().setCreationDate(d));
                default -> {
                }
            }
        }
    }

    private Optional<OffsetDateTime> parseDate(String text) {
        try {
            return Optional.of(OffsetDateTime.parse(text.trim()));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * 递归遍历块级结构：行内文本累积为 span，遇块级标签冲刷成 Paragraph；
     * h1..h6/li/blockquote/pre/table/img 各自映射到对应 DocBlock。
     */
    private void readBlock(Element element, DocDocument doc, Map<String, byte[]> entries,
                           String baseDir, int listLevel, boolean ordered, List<DocSpan> inline) {
        String tag = local(element);
        switch (tag) {
            case "h1", "h2", "h3", "h4", "h5", "h6" -> {
                flushInline(doc, inline);
                doc.add(new DocBlock.Heading(tag.charAt(1) - '0', inlineText(element)));
            }
            case "p" -> {
                collectInline(element, inline);
                flushInline(doc, inline);
            }
            case "li" -> {
                flushInline(doc, inline);
                collectInline(element, inline);
                List<DocSpan> spans = new ArrayList<>(inline);
                inline.clear();
                doc.add(new DocBlock.ListItem(ordered, listLevel, spans.isEmpty()
                        ? List.of(DocSpan.plain("")) : spans));
                // li 内嵌套列表单独成块，层级递增
                for (Node child : elements(element)) {
                    if (local(child).equals("ol") || local(child).equals("ul")) {
                        readList((Element) child, doc, entries, baseDir, listLevel + 1, local(child).equals("ol"));
                    }
                }
            }
            case "ul", "ol" -> {
                flushInline(doc, inline);
                readList(element, doc, entries, baseDir, listLevel, tag.equals("ol"));
            }
            case "blockquote" -> {
                flushInline(doc, inline);
                collectInline(element, inline);
                doc.add(new DocBlock.Quote(new ArrayList<>(inline)));
                inline.clear();
            }
            case "pre" -> {
                flushInline(doc, inline);
                doc.add(new DocBlock.CodeBlock(codeLanguage(element), element.getTextContent()));
            }
            case "table" -> {
                flushInline(doc, inline);
                doc.add(readTable(element, entries, baseDir));
            }
            case "img" -> {
                DocBlock.Image image = readImage(element, entries, baseDir);
                if (image != null) {
                    flushInline(doc, inline);
                    doc.add(image);
                }
            }
            default -> {
                boolean flushBefore = BLOCK_TAGS.contains(tag);
                if (flushBefore) {
                    flushInline(doc, inline);
                }
                for (Node child : elements(element)) {
                    readBlock((Element) child, doc, entries, baseDir, listLevel, ordered, inline);
                }
                if (flushBefore) {
                    flushInline(doc, inline);
                }
            }
        }
    }

    private void readList(Element list, DocDocument doc, Map<String, byte[]> entries,
                          String baseDir, int level, boolean ordered) {
        for (Node child : elements(list)) {
            if (local(child).equals("li")) {
                readBlock((Element) child, doc, entries, baseDir, level, ordered, new ArrayList<>());
            }
        }
    }

    private void flushInline(DocDocument doc, List<DocSpan> inline) {
        if (inline.isEmpty()) {
            return;
        }
        String text = inline.stream().map(DocSpan::text).reduce("", String::concat);
        if (!text.isBlank()) {
            doc.add(new DocBlock.Paragraph(new ArrayList<>(inline)));
        }
        inline.clear();
    }

    /** 递归收集行内文本：em/i→italic、strong/b→bold、code→mono、a→link。 */
    private void collectInline(Element element, List<DocSpan> spans) {
        collectInline(element, spans, false, false, false, null);
    }

    private void collectInline(Element element, List<DocSpan> spans,
                               boolean bold, boolean italic, boolean mono, String link) {
        String tag = local(element);
        switch (tag) {
            case "em", "i" -> italic = true;
            case "strong", "b" -> bold = true;
            case "code", "kbd", "samp" -> mono = true;
            case "a" -> link = attr(element, "href") != null ? attr(element, "href") : link;
            default -> {
            }
        }
        for (Node node : nodeListOf(element)) {
            if (node.getNodeType() == Node.TEXT_NODE) {
                String text = node.getTextContent();
                if (!text.isEmpty()) {
                    DocSpan span = DocSpan.plain(text);
                    if (bold) {
                        span = span.withBold();
                    }
                    if (italic) {
                        span = span.withItalic();
                    }
                    if (mono) {
                        span = span.withMono();
                    }
                    if (link != null) {
                        span = span.withLink(link);
                    }
                    spans.add(span);
                }
            } else if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element child = (Element) node;
                if (local(child).equals("img")) {
                    continue;
                }
                if (local(child).equals("br")) {
                    spans.add(DocSpan.plain("\n"));
                    continue;
                }
                collectInline(child, spans, bold, italic, mono, link);
            }
        }
    }

    /** 元素自身文本（无样式差异，用于标题等纯文本场景）。 */
    private List<DocSpan> inlineText(Element element) {
        List<DocSpan> spans = new ArrayList<>();
        collectInline(element, spans);
        return spans;
    }

    /** pre/code 的 class="language-xxx" → 语言名。 */
    private String codeLanguage(Element pre) {
        String cls = attr(pre, "class");
        if (cls == null && firstByLocal(pre, "code") != null) {
            cls = attr(firstByLocal(pre, "code"), "class");
        }
        if (cls != null) {
            for (String token : cls.split("\\s+")) {
                if (token.startsWith("language-")) {
                    return token.substring("language-".length());
                }
            }
        }
        return null;
    }

    /** table → tr/td/th；th 行 header=true；colspan/rowspan 属性。 */
    private DocBlock.Table readTable(Element table, Map<String, byte[]> entries, String baseDir) {
        List<DocBlock.Table.Row> rows = new ArrayList<>();
        for (Node tr : elements(table)) {
            if (!local(tr).equals("tr")) {
                continue;
            }
            boolean header = false;
            List<DocBlock.Table.Cell> cells = new ArrayList<>();
            for (Node tc : elements(tr)) {
                String tag = local(tc);
                if (!tag.equals("td") && !tag.equals("th")) {
                    continue;
                }
                header |= tag.equals("th");
                List<DocBlock> blocks = new ArrayList<>();
                String text = ((Element) tc).getTextContent().trim();
                if (!text.isEmpty()) {
                    blocks.add(new DocBlock.Paragraph(text));
                }
                cells.add(new DocBlock.Table.Cell(blocks.isEmpty() ? List.of(new DocBlock.Paragraph("")) : blocks,
                        Math.max(1, intVal(attr((Element) tc, "colspan"), 1)),
                        Math.max(1, intVal(attr((Element) tc, "rowspan"), 1))));
            }
            if (!cells.isEmpty()) {
                rows.add(new DocBlock.Table.Row(cells, header));
            }
        }
        return new DocBlock.Table(rows);
    }

    /** img src 相对路径 → zip 字节；取不到跳过。 */
    private DocBlock.Image readImage(Element img, Map<String, byte[]> entries, String baseDir) {
        String src = attr(img, "src");
        if (src == null || src.isBlank()) {
            return null;
        }
        String path = baseDir + src;
        byte[] bytes = entries.get(path);
        if (bytes == null) {
            return null;
        }
        String name = src.substring(src.lastIndexOf('/') + 1);
        return new DocBlock.Image(name, guessMime(name), bytes, null, null);
    }

    private String chapterTitle(Element html) {
        Element title = firstByLocal(html, "title");
        if (title != null && !title.getTextContent().isBlank()) {
            return title.getTextContent().trim();
        }
        Element h1 = firstByLocal(html, "h1");
        return h1 == null ? null : h1.getTextContent().trim();
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
        return "application/octet-stream";
    }

    /** 命名空间无关 DOM 解析；禁 DTD/外部实体。 */
    private Element parse(byte[] xml, String expectLocal) {
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
            Document d = f.newDocumentBuilder().parse(new InputSource(new ByteArrayInputStream(xml)));
            Element root = d.getDocumentElement();
            if (expectLocal != null && (root == null || !local(root).equals(expectLocal))) {
                return null;
            }
            return root;
        } catch (Exception e) {
            return null;
        }
    }

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

    private List<Node> nodeListOf(Node node) {
        List<Node> result = new ArrayList<>();
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            result.add(children.item(i));
        }
        return result;
    }

    private Element firstByLocal(Node root, String local) {
        if (local(root).equals(local)) {
            return (Element) root;
        }
        for (Node child : elements(root)) {
            Element found = firstByLocal(child, local);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    private String local(Node node) {
        String name = node.getLocalName();
        return name != null ? name : node.getNodeName();
    }

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
}
