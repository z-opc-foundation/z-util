package com.zifang.util.doc.pptx;

import com.zifang.util.doc.DocBlock;
import com.zifang.util.doc.DocDocument;
import com.zifang.util.doc.DocFormat;
import com.zifang.util.doc.DocFormatDetector;
import com.zifang.util.doc.DocParseException;
import com.zifang.util.doc.DocReader;
import com.zifang.util.doc.DocSpan;
import com.zifang.util.doc.xml.XmlSupport;
import com.zifang.util.doc.zip.ZipSupport;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PowerPoint 2007+（pptx）读取器。零第三方依赖：JDK Zip 解包 + DOM 解析。
 * 覆盖：按 sldIdLst 顺序的幻灯片、形状文本与加粗/斜体/删除线、表格、图片（走 slide rels → media）、备注页。
 */
public final class PptxDocReader implements DocReader {

    private static final String NS_REL = "http://schemas.openxmlformats.org/officeDocument/2006/relationships";

    @Override
    public DocFormat format() {
        return DocFormat.PPTX;
    }

    @Override
    public boolean supports(byte[] data) {
        return DocFormatDetector.detectZipContainer(data) == DocFormat.PPTX;
    }

    @Override
    public DocDocument read(byte[] data) throws IOException {
        Map<String, byte[]> entries = ZipSupport.read(data);
        byte[] presentationXml = entries.get("ppt/presentation.xml");
        if (presentationXml == null) {
            throw new DocParseException("不是 pptx：缺少 ppt/presentation.xml");
        }
        Map<String, String> presRels = readRels(entries.get("ppt/_rels/presentation.xml.rels"));

        Document presentation = XmlSupport.parse(presentationXml);
        List<DocBlock.Slides.Slide> slides = new ArrayList<>();
        int index = 1;
        for (Element sldId : XmlSupport.descendants(presentation.getDocumentElement(), "sldId")) {
            String target = presRels.get(XmlSupport.attrNs(sldId, NS_REL, "id"));
            if (target == null) {
                continue;
            }
            String path = normalize("ppt/", target);
            byte[] slideXml = entries.get(path);
            if (slideXml == null) {
                continue;
            }
            String relsPath = relsPathOf(path);
            Map<String, String> slideRels = readRels(entries.get(relsPath));
            slides.add(readSlide(index++, slideXml, slideRels, entries));
        }

        DocDocument doc = new DocDocument();
        doc.add(new DocBlock.Slides(slides));
        return doc;
    }

    private DocBlock.Slides.Slide readSlide(int index, byte[] slideXml, Map<String, String> slideRels,
                                           Map<String, byte[]> entries) throws IOException {
        Document slide = XmlSupport.parse(slideXml);
        Element root = slide.getDocumentElement();
        Element spTree = XmlSupport.child(root, "spTree");
        if (spTree == null) {
            List<Element> found = XmlSupport.descendants(root, "spTree");
            spTree = found.isEmpty() ? null : found.get(0);
        }

        List<DocBlock> blocks = new ArrayList<>();
        String title = null;
        for (Element shape : spTree == null ? List.<Element>of() : childElements(spTree)) {
            String name = XmlSupport.localName(shape);
            switch (name) {
                case "sp" -> {
                    String shapeTitle = shapeTitle(shape);
                    List<DocBlock> shapeBlocks = textBlocks(shape);
                    if (shapeTitle != null && !shapeTitle.isEmpty()) {
                        if (title == null) {
                            title = shapeTitle;
                            break;
                        }
                    }
                    blocks.addAll(shapeBlocks);
                }
                case "graphicFrame" -> blocks.addAll(tableBlocks(shape));
                case "pic" -> {
                    DocBlock.Image image = image(shape, slideRels, entries);
                    if (image != null) {
                        blocks.add(image);
                    }
                }
                case "grpSp" -> {
                    for (Element inner : childElements(shape)) {
                        String innerName = XmlSupport.localName(inner);
                        if ("sp".equals(innerName)) {
                            blocks.addAll(textBlocks(inner));
                        } else if ("graphicFrame".equals(innerName)) {
                            blocks.addAll(tableBlocks(inner));
                        }
                    }
                }
                default -> {
                    // 其它形状类型（连接线、图表框等）忽略
                }
            }
        }

        for (Map.Entry<String, String> rel : slideRels.entrySet()) {
            if (!rel.getValue().contains("notesSlide")) {
                continue;
            }
            byte[] notesXml = entries.get(normalize("ppt/slides/", rel.getValue()));
            if (notesXml == null) {
                continue;
            }
            String note = notesText(notesXml);
            if (!note.isEmpty()) {
                blocks.add(new DocBlock.Paragraph(List.of(DocSpan.plain("[备注] " + note))));
            }
        }
        return new DocBlock.Slides.Slide(index, title, blocks);
    }

    private List<Element> childElements(Element parent) {
        List<Element> result = new ArrayList<>();
        org.w3c.dom.NodeList nodes = parent.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node instanceof Element element) {
                result.add(element);
            }
        }
        return result;
    }

    /** 形状里的段落文本。 */
    private List<DocBlock> textBlocks(Element shape) {
        List<DocBlock> blocks = new ArrayList<>();
        Element txBody = XmlSupport.child(shape, "txBody");
        if (txBody == null) {
            return blocks;
        }
        for (Element paragraph : XmlSupport.children(txBody, "p")) {
            List<DocSpan> spans = paragraphSpans(paragraph);
            String text = plain(spans);
            if (!text.isEmpty()) {
                blocks.add(new DocBlock.Paragraph(spans));
            }
        }
        return blocks;
    }

    /** 段落内的 run：加粗/斜体/删除线按 a:rPr 判定。 */
    private List<DocSpan> paragraphSpans(Element paragraph) {
        List<DocSpan> spans = new ArrayList<>();
        for (Element run : XmlSupport.children(paragraph, "r")) {
            String text = XmlSupport.text(XmlSupport.child(run, "t"));
            if (text == null || text.isEmpty()) {
                continue;
            }
            Element rPr = XmlSupport.child(run, "rPr");
            boolean bold = "1".equals(XmlSupport.attr(rPr, "b"));
            boolean italic = "1".equals(XmlSupport.attr(rPr, "i"));
            boolean strike = rPr != null && XmlSupport.attr(rPr, "strike") != null
                    && !XmlSupport.attr(rPr, "strike").equals("noStrike");
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
            spans.add(span);
        }
        if (spans.isEmpty()) {
            String text = XmlSupport.text(paragraph);
            if (text != null && !text.isEmpty()) {
                spans.add(DocSpan.plain(text));
            }
        }
        return spans;
    }

    private String plain(List<DocSpan> spans) {
        StringBuilder sb = new StringBuilder();
        for (DocSpan span : spans) {
            sb.append(span.text());
        }
        return sb.toString();
    }

    /** 标题占位符（ph type=title/ctrTitle）的文字。 */
    private String shapeTitle(Element shape) {
        Element nvSpPr = XmlSupport.child(shape, "nvSpPr");
        Element nvPr = XmlSupport.child(nvSpPr, "nvPr");
        Element ph = XmlSupport.child(nvPr, "ph");
        String type = XmlSupport.attr(ph, "type");
        if (!"title".equals(type) && !"ctrTitle".equals(type)) {
            return null;
        }
        Element txBody = XmlSupport.child(shape, "txBody");
        if (txBody == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (Element paragraph : XmlSupport.children(txBody, "p")) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(XmlSupport.text(paragraph).trim());
        }
        return sb.toString().trim();
    }

    /** a:tbl 表格。 */
    private List<DocBlock> tableBlocks(Element frame) {
        List<DocBlock> blocks = new ArrayList<>();
        for (Element tbl : XmlSupport.descendants(frame, "tbl")) {
            List<DocBlock.Table.Row> rows = new ArrayList<>();
            int rowIndex = 0;
            for (Element tr : XmlSupport.children(tbl, "tr")) {
                List<DocBlock.Table.Cell> cells = new ArrayList<>();
                for (Element tc : XmlSupport.children(tr, "tc")) {
                    List<DocBlock> cellBlocks = new ArrayList<>();
                    Element txBody = XmlSupport.child(tc, "txBody");
                    if (txBody != null) {
                        for (Element paragraph : XmlSupport.children(txBody, "p")) {
                            List<DocSpan> spans = paragraphSpans(paragraph);
                            String text = plain(spans);
                            if (!text.isEmpty()) {
                                cellBlocks.add(new DocBlock.Paragraph(spans));
                            }
                        }
                    }
                    if (cellBlocks.isEmpty()) {
                        cellBlocks.add(new DocBlock.Paragraph(""));
                    }
                    cells.add(new DocBlock.Table.Cell(cellBlocks, 1, 1));
                }
                if (!cells.isEmpty()) {
                    rows.add(new DocBlock.Table.Row(cells, rowIndex == 0));
                }
                rowIndex++;
            }
            if (!rows.isEmpty()) {
                blocks.add(new DocBlock.Table(rows));
            }
            break;
        }
        return blocks;
    }

    /** 图片：p:pic → a:blip r:embed → slide rels → media。 */
    private DocBlock.Image image(Element pic, Map<String, String> slideRels, Map<String, byte[]> entries) {
        Element blip = null;
        for (Element candidate : XmlSupport.descendants(pic, "blip")) {
            blip = candidate;
            break;
        }
        if (blip == null) {
            return null;
        }
        String relId = XmlSupport.attrNs(blip, NS_REL, "embed");
        if (relId == null) {
            relId = XmlSupport.attrNs(blip, NS_REL, "link");
        }
        String target = relId == null ? null : slideRels.get(relId);
        if (target == null) {
            return null;
        }
        byte[] bytes = entries.get(normalize("ppt/slides/", target));
        if (bytes == null) {
            return null;
        }
        String name = target.substring(target.lastIndexOf('/') + 1);
        Integer width = null;
        Integer height = null;
        Element ext = null;
        for (Element candidate : XmlSupport.descendants(pic, "ext")) {
            ext = candidate;
            break;
        }
        if (ext != null) {
            long cx = parseLong(XmlSupport.attr(ext, "cx"));
            long cy = parseLong(XmlSupport.attr(ext, "cy"));
            if (cx > 0 && cy > 0) {
                width = (int) (cx / 9525);
                height = (int) (cy / 9525);
            }
        }
        return new DocBlock.Image(name, mimeOf(name), bytes, width, height);
    }

    private String notesText(byte[] notesXml) throws IOException {
        Document doc = XmlSupport.parse(notesXml);
        Element root = doc.getDocumentElement();
        StringBuilder sb = new StringBuilder();
        for (Element shape : XmlSupport.descendants(root, "sp")) {
            Element nvSpPr = XmlSupport.child(shape, "nvSpPr");
            Element nvPr = XmlSupport.child(nvSpPr, "nvPr");
            Element ph = XmlSupport.child(nvPr, "ph");
            String type = XmlSupport.attr(ph, "type");
            if ("sldNum".equals(type)) {
                continue;
            }
            Element txBody = XmlSupport.child(shape, "txBody");
            if (txBody == null) {
                continue;
            }
            for (Element paragraph : XmlSupport.children(txBody, "p")) {
                String text = XmlSupport.text(paragraph).trim();
                if (!text.isEmpty()) {
                    if (sb.length() > 0) {
                        sb.append(' ');
                    }
                    sb.append(text);
                }
            }
        }
        return sb.toString().trim();
    }

    private Map<String, String> readRels(byte[] xml) throws IOException {
        Map<String, String> rels = new HashMap<>();
        if (xml == null) {
            return rels;
        }
        Document doc = XmlSupport.parse(xml);
        for (Element rel : XmlSupport.children(doc.getDocumentElement(), "Relationship")) {
            String id = XmlSupport.attr(rel, "Id");
            String target = XmlSupport.attr(rel, "Target");
            if (id != null && target != null) {
                rels.put(id, target);
            }
        }
        return rels;
    }

    /** 把关系目标解析成包内绝对路径：base 是所属部件目录，"../" 逐级回退。 */
    private static String normalize(String base, String target) {
        String t = target.startsWith("/") ? target.substring(1) : base + target;
        Deque<String> stack = new ArrayDeque<>();
        for (String part : t.split("/")) {
            if (part.isEmpty() || ".".equals(part)) {
                continue;
            }
            if ("..".equals(part)) {
                stack.pollLast();
                continue;
            }
            stack.addLast(part);
        }
        return String.join("/", stack);
    }

    /** "ppt/slides/slide1.xml" → "ppt/slides/_rels/slide1.xml.rels"。 */
    private static String relsPathOf(String partPath) {
        int slash = partPath.lastIndexOf('/');
        String dir = partPath.substring(0, slash);
        String file = partPath.substring(slash + 1);
        return dir + "/_rels/" + file + ".rels";
    }

    private static String mimeOf(String name) {
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
        if (lower.endsWith(".svg")) {
            return "image/svg+xml";
        }
        if (lower.endsWith(".emf")) {
            return "image/x-emf";
        }
        if (lower.endsWith(".wmf")) {
            return "image/x-wmf";
        }
        return "application/octet-stream";
    }

    private static long parseLong(String value) {
        try {
            return value == null ? 0 : Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
