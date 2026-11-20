package com.zifang.util.doc.odf;

import com.zifang.util.doc.DocBlock;
import com.zifang.util.doc.DocDocument;
import com.zifang.util.doc.DocFormat;
import com.zifang.util.doc.DocReader;
import com.zifang.util.doc.DocSpan;
import org.w3c.dom.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * OpenDocument 演示文稿（.odp）读取器。零第三方依赖：JDK Zip + DOM。
 * 覆盖：draw:page 逐页、标题占位符（presentation:class=title）、文本框与自定义形状文本、
 * 组合形状、图片、备注页（presentation:notes）。
 */
public final class OdpDocReader implements DocReader {

    @Override
    public DocFormat format() {
        return DocFormat.ODP;
    }

    @Override
    public boolean supports(byte[] data) {
        try {
            return OdfSupport.MIME_PRESENTATION.equals(OdfSupport.mimetype(OdfSupport.entries(data)));
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public DocDocument read(byte[] data) throws IOException {
        Map<String, byte[]> entries = OdfSupport.entries(data);
        Element contentRoot = OdfSupport.contentRoot(entries);
        Map<String, OdfSupport.Style> styles = OdfSupport.parseStyles(entries, contentRoot);
        Element body = OdfSupport.bodyPart(contentRoot, "presentation");

        List<DocBlock.Slides.Slide> slides = new ArrayList<>();
        int index = 1;
        if (body != null) {
            for (Element page : OdfSupport.children(body, "page")) {
                slides.add(readPage(index++, page, styles, entries));
            }
        }
        DocDocument doc = new DocDocument();
        doc.add(new DocBlock.Slides(slides));
        // master-page 页眉页脚：[页眉]/[页脚] Quote 块挂在幻灯片后
        for (OdfSupport.MasterHeaderFooter master : OdfSupport.masterHeaderFooters(entries, styles)) {
            for (String line : master.header()) {
                doc.add(new DocBlock.Quote(List.of(DocSpan.plain("[页眉] " + line))));
            }
            for (String line : master.footer()) {
                doc.add(new DocBlock.Quote(List.of(DocSpan.plain("[页脚] " + line))));
            }
        }
        return doc;
    }

    private DocBlock.Slides.Slide readPage(int index, Element page, Map<String, OdfSupport.Style> styles,
                                          Map<String, byte[]> entries) {
        List<DocBlock> blocks = new ArrayList<>();
        String title = null;
        for (Element shape : OdfSupport.childElements(page)) {
            String name = OdfSupport.localName(shape);
            switch (name) {
                case "notes" -> {
                    String note = notesText(shape, styles, entries);
                    if (!note.isEmpty()) {
                        blocks.add(new DocBlock.Paragraph(List.of(DocSpan.plain("[备注] " + note))));
                    }
                }
                case "frame", "custom-shape", "g" -> {
                    String className = OdfSupport.attr(shape, "class");
                    List<DocBlock> shapeBlocks = new ArrayList<>();
                    OdfSupport.appendTextBlocks(shape, shapeBlocks, styles, entries, 0);
                    if ("title".equals(className) && title == null) {
                        title = firstText(shapeBlocks);
                        continue;
                    }
                    blocks.addAll(shapeBlocks);
                }
                default -> {
                    // 动画、时间轴等非内容元素忽略
                }
            }
        }
        return new DocBlock.Slides.Slide(index, title, blocks);
    }

    /** 备注页文本。 */
    private String notesText(Element notes, Map<String, OdfSupport.Style> styles,
                              Map<String, byte[]> entries) {
        StringBuilder sb = new StringBuilder();
        for (Element frame : OdfSupport.children(notes, "frame")) {
            List<DocBlock> blocks = new ArrayList<>();
            OdfSupport.appendTextBlocks(frame, blocks, styles, entries, 0);
            for (DocBlock block : blocks) {
                if (block instanceof DocBlock.Paragraph paragraph) {
                    String text = plain(paragraph.spans());
                    if (!text.isEmpty()) {
                        if (sb.length() > 0) {
                            sb.append(' ');
                        }
                        sb.append(text);
                    }
                }
            }
        }
        return sb.toString().trim();
    }

    private String firstText(List<DocBlock> blocks) {
        for (DocBlock block : blocks) {
            if (block instanceof DocBlock.Paragraph paragraph) {
                String text = plain(paragraph.spans()).trim();
                if (!text.isEmpty()) {
                    return text;
                }
            }
        }
        return null;
    }

    private String plain(List<DocSpan> spans) {
        StringBuilder sb = new StringBuilder();
        for (DocSpan span : spans) {
            sb.append(span.text());
        }
        return sb.toString();
    }
}
