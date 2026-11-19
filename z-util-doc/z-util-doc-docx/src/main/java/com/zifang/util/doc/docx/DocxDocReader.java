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
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/** DOCX 读取器：纯 JDK 解压 + DOM 解析 OOXML，零第三方依赖，禁 DTD/外部实体防 XXE。 */
public class DocxDocReader implements DocReader {

    /** 段落样式名到标题级别的启发式：heading N / HeadingN / berschrift N / 纯数字 1-9。 */
    private static final Pattern HEADING_STYLE = Pattern.compile("^(heading|berschrift)\\s*([1-9])$",
            Pattern.CASE_INSENSITIVE);

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
        Map<String, String> rels = parseRels(entries.get("word/_rels/document.xml.rels"));
        Numbering numbering = parseNumbering(entries.get("word/numbering.xml"));
        DocDocument doc = new DocDocument();
        Element body = documentElement(entries.get("word/document.xml"), "document");
        Element bodyRoot = null;
        if (body != null) {
            for (Node child : elements(body)) {
                if (local(child).equals("body")) {
                    bodyRoot = (Element) child;
                    break;
                }
            }
        }
        if (bodyRoot != null) {
            for (Node node : elements(bodyRoot)) {
                switch (local(node)) {
                    case "p" -> readParagraph((Element) node, doc, rels, numbering, entries, null);
                    case "tbl" -> doc.add(readTable((Element) node, rels, numbering, entries));
                    default -> {
                    }
                }
            }
        }
        readCoreMetadata(entries.get("docProps/core.xml"), doc);
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

    /** rels：Id → Target（如 rId2 → media/image1.png 或外部 URL）。 */
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

    /** numbering.xml：numId → (ilvl → numFmt)；缺失时返回空表（默认无序）。 */
    private Numbering parseNumbering(byte[] xml) {
        Numbering numbering = new Numbering();
        Element root = documentElement(xml, null);
        if (root == null) {
            return numbering;
        }
        for (Node node : elements(root)) {
            if (local(node).equals("abstractNum")) {
                String abstractId = attr((Element) node, "abstractNumId");
                Map<Integer, String> fmts = new HashMap<>();
                for (Node lvl : elements(node)) {
                    if (local(lvl).equals("lvl")) {
                        fmts.put(intVal(attr((Element) lvl, "ilvl"), 0), attrOfChild((Element) lvl, "numFmt"));
                    }
                }
                numbering.abstractFormats.put(abstractId, fmts);
            } else if (local(node).equals("num")) {
                String numId = attr((Element) node, "numId");
                String abstractId = attrOfChild((Element) node, "abstractNumId");
                if (numId != null && abstractId != null) {
                    numbering.numToAbstract.put(numId, abstractId);
                }
            }
        }
        return numbering;
    }

    /** 段落：样式 → Heading；numPr → ListItem；否则 Paragraph。图片（w:drawing/w:pict）独立成块。 */
    private void readParagraph(Element p, DocDocument doc, Map<String, String> rels,
                               Numbering numbering, Map<String, byte[]> entries, List<DocSpan> extra) {
        List<DocSpan> spans = new ArrayList<>(extra == null ? List.of() : extra);
        List<DocBlock.Image> images = new ArrayList<>();
        int heading = 0;
        boolean list = false;
        int listLevel = 0;
        boolean ordered = false;
        for (Node node : elements(p)) {
            switch (local(node)) {
                case "pPr" -> {
                    for (Node pp : elements(node)) {
                        switch (local(pp)) {
                            case "pStyle" -> heading = headingLevel(attr((Element) pp, "val"));
                            case "numPr" -> {
                                list = true;
                                listLevel = intVal(attrOfChild((Element) pp, "ilvl"), 0);
                                ordered = numbering.isOrdered(attrOfChild((Element) pp, "numId"), listLevel);
                            }
                            default -> {
                            }
                        }
                    }
                }
                case "r" -> collectRun((Element) node, spans, images, rels, entries, null);
                case "hyperlink" -> {
                    String link = rels.get(attrNS((Element) node, "id"));
                    for (Node r : elements(node)) {
                        if (local(r).equals("r")) {
                            collectRun((Element) r, spans, images, rels, entries, link);
                        }
                    }
                }
                default -> {
                }
            }
        }
        for (DocBlock.Image image : images) {
            doc.add(image);
        }
        if (spans.isEmpty() && images.isEmpty()) {
            return;
        }
        String text = spans.stream().map(DocSpan::text).reduce("", String::concat);
        if (heading > 0 && !text.isBlank()) {
            doc.add(new DocBlock.Heading(heading, spans));
        } else if (list) {
            doc.add(new DocBlock.ListItem(ordered, listLevel, spans));
        } else {
            doc.add(new DocBlock.Paragraph(spans));
        }
    }

    /** run 内收集文本/制表/换行/图片，叠加 rPr 样式与 hyperlink 链接。 */
    private void collectRun(Element r, List<DocSpan> spans, List<DocBlock.Image> images,
                            Map<String, String> rels, Map<String, byte[]> entries, String link) {
        boolean bold = false;
        boolean italic = false;
        boolean strike = false;
        for (Node pr : elements(r)) {
            if (!local(pr).equals("rPr")) {
                continue;
            }
            for (Node n : elements(pr)) {
                switch (local(n)) {
                    case "b" -> bold = onOff((Element) n);
                    case "i" -> italic = onOff((Element) n);
                    case "strike" -> strike = onOff((Element) n);
                    default -> {
                    }
                }
            }
        }
        for (Node node : elements(r)) {
            switch (local(node)) {
                case "t" -> addSpan(spans, node.getTextContent(), bold, italic, strike, link);
                case "tab" -> addSpan(spans, "\t", bold, italic, strike, link);
                case "br" -> addSpan(spans, "\n", bold, italic, strike, link);
                case "drawing", "pict" -> readImage(node, images, rels, entries);
                default -> {
                }
            }
        }
    }

    private void addSpan(List<DocSpan> spans, String text, boolean bold, boolean italic,
                         boolean strike, String link) {
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
        if (link != null) {
            span = span.withLink(link);
        }
        spans.add(span);
    }

    /** w:drawing / w:pict 内 a:blip 的 r:embed → rels → media 字节 → Image。 */
    private void readImage(Node container, List<DocBlock.Image> images,
                           Map<String, String> rels, Map<String, byte[]> entries) {
        String relId = findAttrByLocal(container, "embed");
        if (relId == null) {
            return;
        }
        String target = rels.get(relId);
        if (target == null) {
            return;
        }
        String path = target.startsWith("/") ? target.substring(1) : "word/" + target;
        byte[] bytes = entries.get(path);
        if (bytes == null) {
            return;
        }
        String name = target.substring(target.lastIndexOf('/') + 1);
        images.add(new DocBlock.Image(name, guessMime(name), bytes, null, null));
    }

    /** 表格：tr → Row，tc → Cell（段落作 blocks）；gridSpan → colspan；首行 header。 */
    private DocBlock.Table readTable(Element tbl, Map<String, String> rels,
                                     Numbering numbering, Map<String, byte[]> entries) {
        List<DocBlock.Table.Row> rows = new ArrayList<>();
        int rowIndex = 0;
        for (Node tr : elements(tbl)) {
            if (!local(tr).equals("tr")) {
                continue;
            }
            List<DocBlock.Table.Cell> cells = new ArrayList<>();
            for (Node tc : elements(tr)) {
                if (!local(tc).equals("tc")) {
                    continue;
                }
                List<DocBlock> blocks = new ArrayList<>();
                for (Node child : elements(tc)) {
                    if (local(child).equals("p")) {
                        DocDocument holder = new DocDocument();
                        readParagraph((Element) child, holder, rels, numbering, entries, null);
                        blocks.addAll(holder.getBlocks());
                    } else if (local(child).equals("tbl")) {
                        blocks.add(readTable((Element) child, rels, numbering, entries));
                    }
                }
                int colspan = 1;
                for (Node tcPr : elements(tc)) {
                    if (local(tcPr).equals("tcPr")) {
                        colspan = Math.max(1, intVal(attrOfChild((Element) tcPr, "gridSpan"), 1));
                    }
                }
                if (blocks.isEmpty()) {
                    blocks.add(new DocBlock.Paragraph(""));
                }
                cells.add(new DocBlock.Table.Cell(blocks, colspan, 1));
            }
            if (!cells.isEmpty()) {
                rows.add(new DocBlock.Table.Row(cells, rowIndex == 0));
                rowIndex++;
            }
        }
        return new DocBlock.Table(rows);
    }

    /** docProps/core.xml 元数据：title/creator/subject/keywords/lastModifiedBy/created。 */
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
                case "created" -> parseDate(text).ifPresent(d -> doc.getMeta().setCreationDate(d));
                case "modified" -> parseDate(text).ifPresent(d -> doc.getMeta().setModDate(d));
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

    /** 样式名 → 标题级别；Title → 1；非标题返回 0。 */
    private int headingLevel(String style) {
        if (style == null || style.isBlank()) {
            return 0;
        }
        if (style.equalsIgnoreCase("Title")) {
            return 1;
        }
        var m = HEADING_STYLE.matcher(style.trim());
        if (m.matches()) {
            return Integer.parseInt(m.group(2));
        }
        return style.matches("[1-9]") ? Integer.parseInt(style) : 0;
    }

    /** w:val 开关语义：val 为 false/0/none/off 视为关，缺省为开。 */
    private boolean onOff(Element e) {
        String val = attr(e, "val");
        return val == null || !(val.equalsIgnoreCase("false") || val.equals("0")
                || val.equalsIgnoreCase("none") || val.equalsIgnoreCase("off"));
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
            if (root == null || (expectLocal != null && !local(root).equals(expectLocal))) {
                return expectLocal == null ? root : null;
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

    private String local(Node node) {
        String name = node.getLocalName();
        return name != null ? name : node.getNodeName();
    }

    private String attr(Element e, String local) {
        return attrNS(e, local);
    }

    /** 按属性 local-name 取值（命名空间无关）。 */
    private String attrNS(Element e, String local) {
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

    /** 任意深度找 local-name 匹配的属性（如 a:blip 的 r:embed）。 */
    private String findAttrByLocal(Node node, String local) {
        String hit = node instanceof Element e ? attrNS(e, local) : null;
        if (hit != null) {
            return hit;
        }
        for (Node child : elements(node)) {
            String found = findAttrByLocal(child, local);
            if (found != null) {
                return found;
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

    /** numbering 索引：numId → abstractNumId → (ilvl → numFmt)。 */
    private static final class Numbering {

        private final Map<String, String> numToAbstract = new LinkedHashMap<>();
        private final Map<String, Map<Integer, String>> abstractFormats = new HashMap<>();

        boolean isOrdered(String numId, int ilvl) {
            String abstractId = numToAbstract.get(numId);
            if (abstractId == null) {
                return false;
            }
            String fmt = abstractFormats.getOrDefault(abstractId, Map.of()).get(ilvl);
            return fmt != null && !fmt.equals("bullet");
        }
    }
}
