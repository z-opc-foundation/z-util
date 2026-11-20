package com.zifang.util.doc.odf;

import com.zifang.util.doc.DocBlock;
import com.zifang.util.doc.DocDocument;
import com.zifang.util.doc.DocFormat;
import com.zifang.util.doc.DocSpan;
import com.zifang.util.doc.zip.ZipSupport;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OdfDocReaderTest {

    private static final String NS =
            "xmlns:office=\"urn:oasis:names:tc:opendocument:xmlns:office:1.0\" "
                    + "xmlns:text=\"urn:oasis:names:tc:opendocument:xmlns:text:1.0\" "
                    + "xmlns:style=\"urn:oasis:names:tc:opendocument:xmlns:style:1.0\" "
                    + "xmlns:fo=\"urn:oasis:names:tc:opendocument:xmlns:xsl-fo-compatible:1.0\" "
                    + "xmlns:table=\"urn:oasis:names:tc:opendocument:xmlns:table:1.0\" "
                    + "xmlns:draw=\"urn:oasis:names:tc:opendocument:xmlns:drawing:1.0\" "
                    + "xmlns:presentation=\"urn:oasis:names:tc:opendocument:xmlns:presentation:1.0\" "
                    + "xmlns:xlink=\"http://www.w3.org/1999/xlink\" "
                    + "office:version=\"1.2\"";

    private byte[] odf(String mime, String bodyXml) throws IOException {
        String content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<office:document-content " + NS + ">"
                + "<office:body>" + bodyXml + "</office:body>"
                + "</office:document-content>";
        Map<String, byte[]> entries = new LinkedHashMap<>();
        entries.put("mimetype", mime.getBytes(StandardCharsets.US_ASCII));
        entries.put("content.xml", content.getBytes(StandardCharsets.UTF_8));
        return ZipSupport.write(entries);
    }

    private static String plain(List<DocSpan> spans) {
        StringBuilder sb = new StringBuilder();
        for (DocSpan span : spans) {
            sb.append(span.text());
        }
        return sb.toString();
    }

    private byte[] corpus(String name) throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/corpus/" + name)) {
            assertNotNull(in, name);
            return in.readAllBytes();
        }
    }

    @Test
    void odtReadsHeadingsParagraphsAndLists() throws Exception {
        byte[] data = odf(OdfSupport.MIME_TEXT,
                "<office:text>"
                        + "<text:h text:outline-level=\"2\">章节</text:h>"
                        + "<text:p>正文一</text:p>"
                        + "<text:list>"
                        + "<text:list-item><text:p>甲</text:p>"
                        + "<text:list><text:list-item><text:p>甲一</text:p></text:list-item></text:list>"
                        + "</text:list-item>"
                        + "<text:list-item><text:p>乙</text:p></text:list-item>"
                        + "</text:list>"
                        + "</office:text>");
        DocDocument doc = new OdtDocReader().read(data);
        List<DocBlock> blocks = doc.getBlocks();
        assertEquals(5, blocks.size(), blocks.toString());
        assertTrue(blocks.get(0) instanceof DocBlock.Heading, blocks.toString());
        assertEquals(2, ((DocBlock.Heading) blocks.get(0)).level());
        assertEquals("章节", plain(((DocBlock.Heading) blocks.get(0)).spans()));
        assertEquals("正文一", plain(((DocBlock.Paragraph) blocks.get(1)).spans()));
        assertEquals("甲", plain(((DocBlock.ListItem) blocks.get(2)).spans()));
        assertEquals(0, ((DocBlock.ListItem) blocks.get(2)).level());
        assertEquals("甲一", plain(((DocBlock.ListItem) blocks.get(3)).spans()));
        assertEquals(1, ((DocBlock.ListItem) blocks.get(3)).level());
    }

    @Test
    void odtAppliesAutomaticStyleFormatting() throws Exception {
        byte[] data = odf(OdfSupport.MIME_TEXT,
                "<office:automatic-styles>"
                        + "<style:style style:name=\"B1\" style:family=\"text\">"
                        + "<style:text-properties fo:font-weight=\"bold\"/></style:style>"
                        + "<style:style style:name=\"I1\" style:family=\"text\">"
                        + "<style:text-properties fo:font-style=\"italic\"/></style:style>"
                        + "</office:automatic-styles>"
                        + "<office:text><text:p>普通"
                        + "<text:span text:style-name=\"B1\">粗</text:span>"
                        + "<text:span text:style-name=\"I1\">斜</text:span>"
                        + "</text:p></office:text>");
        DocDocument doc = new OdtDocReader().read(data);
        List<DocSpan> spans = ((DocBlock.Paragraph) doc.getBlocks().get(0)).spans();
        assertEquals("普通粗斜", plain(spans));
        assertTrue(spans.stream().anyMatch(s -> s.text().equals("粗") && s.bold()), spans.toString());
        assertTrue(spans.stream().anyMatch(s -> s.text().equals("斜") && s.italic()), spans.toString());
    }

    @Test
    void odtReadsMasterPageHeaderFooter() throws Exception {
        DocDocument doc = new OdtDocReader().read(corpus("header-footer.odt"));
        List<String> quotes = new ArrayList<>();
        for (DocBlock block : doc.getBlocks()) {
            if (block instanceof DocBlock.Quote quote) {
                quotes.add(plain(quote.spans()));
            }
        }
        assertTrue(quotes.contains("[页眉] 章节页眉 加粗"), quotes.toString());
        assertTrue(quotes.contains("[页眉] 第二行页眉"), quotes.toString());
        assertTrue(quotes.contains("[页脚] 第 1 页"), quotes.toString());
        // 正文不受影响
        assertTrue(doc.toPlainText().contains("Plain text"), doc.toPlainText());
        // master-page 里没有页眉页脚内容的文件不产出 Quote
        DocDocument plainDoc = new OdtDocReader().read(corpus("simplestyles.odt"));
        assertTrue(plainDoc.getBlocks().stream().noneMatch(b -> b instanceof DocBlock.Quote));
    }

    @Test
    void odtReadsTable() throws Exception {
        byte[] data = odf(OdfSupport.MIME_TEXT,
                "<office:text><table:table table:name=\"T\">"
                        + "<table:table-row>"
                        + "<table:table-cell><text:p>A</text:p></table:table-cell>"
                        + "<table:table-cell><text:p>B</text:p></table:table-cell>"
                        + "</table:table-row>"
                        + "<table:table-row>"
                        + "<table:table-cell><text:p>1</text:p></table:table-cell>"
                        + "<table:table-cell><text:p>2</text:p></table:table-cell>"
                        + "</table:table-row>"
                        + "</table:table></office:text>");
        DocDocument doc = new OdtDocReader().read(data);
        DocBlock.Table table = (DocBlock.Table) doc.getBlocks().get(0);
        assertEquals(2, table.rows().size());
        assertTrue(table.rows().get(0).header());
        assertEquals(2, table.rows().get(0).cells().size());
    }

    @Test
    void odsReadsTypedCellsAndFormulas() throws Exception {
        byte[] data = odf(OdfSupport.MIME_SPREADSHEET,
                "<office:spreadsheet>"
                        + "<table:table table:name=\"Sheet1\">"
                        + "<table:table-row>"
                        + "<table:table-cell office:value-type=\"float\" office:value=\"3\"><text:p>3</text:p></table:table-cell>"
                        + "<table:table-cell office:value-type=\"string\"><text:p>abc</text:p></table:table-cell>"
                        + "<table:table-cell office:value-type=\"boolean\" office:boolean-value=\"true\"><text:p>TRUE</text:p></table:table-cell>"
                        + "</table:table-row>"
                        + "<table:table-row>"
                        + "<table:table-cell table:formula=\"of:=SUM([.A1:.B1])\" office:value-type=\"float\" office:value=\"3\"><text:p>3</text:p></table:table-cell>"
                        + "<table:table-cell office:value-type=\"date\" office:date-value=\"2026-11-20\"><text:p>2026-11-20</text:p></table:table-cell>"
                        + "<table:table-cell office:value-type=\"time\" office:time-value=\"PT06H34M00S\"><text:p>06:34</text:p></table:table-cell>"
                        + "</table:table-row>"
                        + "</table:table></office:spreadsheet>");
        DocDocument doc = new OdsDocReader().read(data);
        DocBlock.Sheet sheet = (DocBlock.Sheet) doc.getBlocks().get(0);
        assertEquals("Sheet1", sheet.name());
        assertEquals(2, sheet.rows().size());
        List<DocBlock.Sheet.SheetCell> first = sheet.rows().get(0).cells();
        assertEquals(3, first.size());
        assertEquals(DocBlock.Sheet.CellType.NUMBER, first.get(0).type());
        assertEquals("3", first.get(0).text());
        assertEquals(DocBlock.Sheet.CellType.STRING, first.get(1).type());
        assertEquals("abc", first.get(1).text());
        assertEquals(DocBlock.Sheet.CellType.BOOLEAN, first.get(2).type());
        assertEquals("true", first.get(2).text());

        List<DocBlock.Sheet.SheetCell> second = sheet.rows().get(1).cells();
        assertEquals(DocBlock.Sheet.CellType.FORMULA, second.get(0).type());
        assertEquals("=SUM([.A1:.B1])", second.get(0).formula());
        assertEquals(DocBlock.Sheet.CellType.DATE, second.get(1).type());
        assertEquals("2026-11-20", second.get(1).text());
        assertEquals(DocBlock.Sheet.CellType.TIME, second.get(2).type());
    }

    @Test
    void odsExpandsRepeatedRowsAndSkipsPadding() throws Exception {
        byte[] data = odf(OdfSupport.MIME_SPREADSHEET,
                "<office:spreadsheet><table:table table:name=\"S\">"
                        + "<table:table-row table:number-rows-repeated=\"3\">"
                        + "<table:table-cell office:value-type=\"float\" office:value=\"1\"><text:p>1</text:p></table:table-cell>"
                        + "</table:table-row>"
                        + "<table:table-row table:number-rows-repeated=\"100\">"
                        + "<table:table-cell/></table:table-row>"
                        + "</table:table></office:spreadsheet>");
        DocDocument doc = new OdsDocReader().read(data);
        DocBlock.Sheet sheet = (DocBlock.Sheet) doc.getBlocks().get(0);
        assertEquals(3, sheet.rows().size());
        assertEquals(1, sheet.rows().get(0).index());
        assertEquals(3, sheet.rows().get(2).index());
    }

    @Test
    void odpReadsSlidesWithTitleAndNotes() throws Exception {
        byte[] data = odf(OdfSupport.MIME_PRESENTATION,
                "<office:presentation>"
                        + "<draw:page draw:name=\"p1\">"
                        + "<draw:frame presentation:class=\"title\"><draw:text-box>"
                        + "<text:p>标题</text:p></draw:text-box></draw:frame>"
                        + "<draw:frame><draw:text-box><text:p>内容</text:p></draw:text-box></draw:frame>"
                        + "<presentation:notes><draw:frame><draw:text-box>"
                        + "<text:p>备注内容</text:p></draw:text-box></draw:frame></presentation:notes>"
                        + "</draw:page>"
                        + "<draw:page draw:name=\"p2\">"
                        + "<draw:custom-shape><text:p>第二页</text:p></draw:custom-shape>"
                        + "</draw:page>"
                        + "</office:presentation>");
        DocDocument doc = new OdpDocReader().read(data);
        DocBlock.Slides slides = (DocBlock.Slides) doc.getBlocks().get(0);
        assertEquals(2, slides.slides().size());
        DocBlock.Slides.Slide first = slides.slides().get(0);
        assertEquals(1, first.index());
        assertEquals("标题", first.title());
        assertEquals(2, first.blocks().size());
        assertTrue(first.blocks().get(1) instanceof DocBlock.Paragraph);
        assertEquals("[备注] 备注内容", plain(((DocBlock.Paragraph) first.blocks().get(1)).spans()));
        assertEquals("第二页", plain(((DocBlock.Paragraph) slides.slides().get(1).blocks().get(0)).spans()));
    }

    @Test
    void detectsFormatsByMimetype() throws Exception {
        assertTrue(new OdtDocReader().supports(corpus("simplelist.odt")));
        assertTrue(new OdsDocReader().supports(corpus("pythagoras.ods")));
        assertTrue(new OdpDocReader().supports(corpus("ol.odp")));
        assertFalse(new OdsDocReader().supports(corpus("simplelist.odt")));
        assertFalse(new OdtDocReader().supports(corpus("empty.ods")));
    }

    @Test
    void readsCorpusFiles() throws Exception {
        DocDocument odt = new OdtDocReader().read(corpus("twolevellist.odt"));
        assertFalse(odt.getBlocks().isEmpty());

        DocDocument table = new OdtDocReader().read(corpus("simpletable.odt"));
        assertTrue(table.getBlocks().stream().anyMatch(b -> b instanceof DocBlock.Table));

        DocDocument ods = new OdsDocReader().read(corpus("pythagoras.ods"));
        List<DocBlock.Sheet> sheets = ods.getBlocks().stream()
                .filter(b -> b instanceof DocBlock.Sheet)
                .map(b -> (DocBlock.Sheet) b)
                .toList();
        assertEquals(3, sheets.size());
        DocBlock.Sheet first = sheets.get(0);
        assertEquals("Sheet1", first.name());
        assertEquals(4, first.rows().size());
        assertEquals("5", first.rows().get(2).cells().get(0).text());
        assertEquals("=SQRT([.A1]*[.A1]+[.A2]*[.A2])", first.rows().get(2).cells().get(0).formula());
        // pythagoras.ods 的 master-page 带打印页眉页脚（sheet-name / 页码字段按占位文本输出）
        assertTrue(ods.getBlocks().stream().anyMatch(b -> b instanceof DocBlock.Quote));

        DocDocument odp = new OdpDocReader().read(corpus("ol.odp"));
        DocBlock.Slides slides = (DocBlock.Slides) odp.getBlocks().get(0);
        assertEquals(1, slides.slides().size());
        assertEquals("OL", slides.slides().get(0).title());
        assertFalse(slides.slides().get(0).blocks().isEmpty());
    }

    @Test
    void formatsAreDeclared() {
        assertEquals(DocFormat.ODT, new OdtDocReader().format());
        assertEquals(DocFormat.ODS, new OdsDocReader().format());
        assertEquals(DocFormat.ODP, new OdpDocReader().format());
    }
}
