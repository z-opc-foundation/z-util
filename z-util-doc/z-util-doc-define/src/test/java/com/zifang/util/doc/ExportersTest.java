package com.zifang.util.doc;

import com.zifang.util.doc.export.MarkdownExporter;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExportersTest {

    private DocDocument sample() {
        DocDocument doc = new DocDocument();
        doc.getMeta().setTitle("t");
        doc.add(new DocBlock.Heading(1, List.of(DocSpan.plain("标题"))));
        doc.add(new DocBlock.Paragraph(List.of(DocSpan.plain("普通"), DocSpan.plain("粗").withBold(),
                DocSpan.plain("斜").withItalic(), DocSpan.plain("码").withMono())));
        doc.add(new DocBlock.ListItem(false, 0, List.of(DocSpan.plain("a"))));
        doc.add(new DocBlock.ListItem(true, 0, List.of(DocSpan.plain("b"))));
        doc.add(new DocBlock.Quote(List.of(DocSpan.plain("q"))));
        doc.add(new DocBlock.CodeBlock("java", "int x = 1;"));
        doc.add(DocBlock.Table.of(List.of(list("A", "B"), list("1", "2"))));
        doc.add(new DocBlock.Image("pic.png", "image/png", new byte[]{1, 2}, 8, 8));
        return doc;
    }

    private List<String> list(String... a) {
        return java.util.Arrays.asList(a);
    }

    @Test
    void markdownCoversAllBlockTypes() {
        String md = new MarkdownExporter().export(sample());
        assertTrue(md.contains("# 标题"));
        assertTrue(md.contains("普通**粗***斜*`码`"));
        assertTrue(md.contains("- a"));
        assertTrue(md.contains("1. b"));
        assertTrue(md.contains("> q"));
        assertTrue(md.contains("```java"));
        assertTrue(md.contains("| A | B |"));
        assertTrue(md.contains("| 1 | 2 |"));
        assertTrue(md.contains("![pic.png]()"));
    }

    @Test
    void markdownEscapePipesInCells() {
        DocDocument doc = new DocDocument();
        doc.add(DocBlock.Table.of(List.of(list("a|b", "c"))));
        String md = new MarkdownExporter().export(doc);
        assertTrue(md.contains("a\\|b"));
    }

    @Test
    void plainTextStripsStyles() {
        String text = new com.zifang.util.doc.export.PlainTextExporter().export(sample());
        assertTrue(text.contains("普通粗斜码"));
        assertTrue(text.contains("A\tB"));
        assertTrue(text.contains("[image: pic.png]"));
    }

    @Test
    void htmlRendersHeadingsAndTable() {
        String html = new com.zifang.util.doc.export.HtmlExporter().export(sample());
        assertTrue(html.contains("<h1>标题</h1>"));
        assertTrue(html.contains("<strong>粗</strong>"));
        assertTrue(html.contains("<table>"));
        assertTrue(html.contains("<img"));
        assertTrue(html.contains("data:image/png;base64,AQI="));
    }

    @Test
    void converterFacadeWorks() {
        assertEquals(new MarkdownExporter().export(sample()), DocConverter.toMarkdown(sample()));
    }

    @Test
    void sheetRendersAsTypedTable() {
        DocDocument doc = new DocDocument();
        doc.add(new DocBlock.Sheet("Sheet1", List.of(
                new DocBlock.Sheet.SheetRow(1, List.of(
                        DocBlock.Sheet.SheetCell.of(0, DocBlock.Sheet.CellType.STRING, "name"),
                        DocBlock.Sheet.SheetCell.of(1, DocBlock.Sheet.CellType.NUMBER, "42"))),
                new DocBlock.Sheet.SheetRow(2, List.of(
                        DocBlock.Sheet.SheetCell.of(0, DocBlock.Sheet.CellType.STRING, "tot"),
                        DocBlock.Sheet.SheetCell.formula(1, "SUM(B1:B1)", "42")))), false));

        String md = new MarkdownExporter().export(doc);
        assertTrue(md.contains("### Sheet1"), md);
        assertTrue(md.contains("| name | 42 |"), md);
        assertTrue(md.contains("| tot | 42 |"), md);

        String html = new com.zifang.util.doc.export.HtmlExporter().export(doc);
        assertTrue(html.contains("<h3>Sheet1</h3>"), html);
        assertTrue(html.contains("data-type=\"NUMBER\""), html);
        assertTrue(html.contains("data-formula=\"SUM(B1:B1)\""), html);

        String text = new com.zifang.util.doc.export.PlainTextExporter().export(doc);
        assertTrue(text.contains("name\t42"), text);
    }

    @Test
    void slidesRenderAsSections() {
        DocDocument doc = new DocDocument();
        doc.add(new DocBlock.Slides(List.of(
                new DocBlock.Slides.Slide(1, "封面", List.of(new DocBlock.Paragraph("hello"))),
                new DocBlock.Slides.Slide(2, null, List.of(new DocBlock.Paragraph("body"))))));

        String md = new MarkdownExporter().export(doc);
        assertTrue(md.contains("## 封面"), md);
        assertTrue(md.contains("## Slide 2"), md);

        String html = new com.zifang.util.doc.export.HtmlExporter().export(doc);
        assertTrue(html.contains("<section class=\"slide\">"), html);
        assertTrue(html.contains("<h2>封面</h2>"), html);
    }
}
