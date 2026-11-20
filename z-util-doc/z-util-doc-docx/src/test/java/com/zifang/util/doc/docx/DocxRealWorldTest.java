package com.zifang.util.doc.docx;

import com.zifang.util.doc.DocBlock;
import com.zifang.util.doc.DocDocument;
import com.zifang.util.doc.DocSpan;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;

/** 真实厂商语料回归 + 新特性（vMerge/sdt/outlineLvl/多级编号/脚注/页眉页脚/文本框）单测。 */
class DocxRealWorldTest {

    private final DocxDocReader reader = new DocxDocReader();

    private byte[] corpus(String name) throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/corpus/" + name)) {
            assertNotNull(in, "missing corpus file " + name);
            return readAll(in);
        }
    }

    private static byte[] readAll(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int n;
        while ((n = in.read(buf)) > 0) {
            out.write(buf, 0, n);
        }
        return out.toByteArray();
    }

    private static String allText(DocDocument doc) {
        StringBuilder sb = new StringBuilder();
        for (DocBlock b : doc.getBlocks()) {
            sb.append(blockText(b)).append('\n');
        }
        return sb.toString();
    }

    private static String blockText(DocBlock b) {
        if (b instanceof DocBlock.Paragraph p) {
            return spans(p.spans());
        }
        if (b instanceof DocBlock.Heading h) {
            return spans(h.spans());
        }
        if (b instanceof DocBlock.ListItem li) {
            return spans(li.spans());
        }
        if (b instanceof DocBlock.Quote q) {
            return spans(q.spans());
        }
        if (b instanceof DocBlock.Table t) {
            StringBuilder sb = new StringBuilder();
            for (DocBlock.Table.Row r : t.rows()) {
                for (DocBlock.Table.Cell c : r.cells()) {
                    for (DocBlock inner : c.blocks()) {
                        sb.append(blockText(inner)).append(' ');
                    }
                    sb.append('|');
                }
                sb.append('\n');
            }
            return sb.toString();
        }
        return "";
    }

    private static String spans(List<DocSpan> spans) {
        StringBuilder sb = new StringBuilder();
        for (DocSpan s : spans) {
            sb.append(s.text());
        }
        return sb.toString();
    }

    // ---------- 真实语料回归 ----------

    @Test
    void allCorpusFilesParseWithoutError() throws Exception {
        String[] files = {"comments.docx", "embedded-style-map.docx", "endnotes.docx",
                "external-picture.docx", "footnote-hyperlink.docx", "footnotes.docx",
                "simple-list.docx", "single-paragraph.docx", "strict-format.docx",
                "strikethrough.docx", "tables.docx", "text-box.docx", "tiny-picture.docx",
                "underline.docx", "utf8-bom.docx"};
        for (String file : files) {
            DocDocument doc = reader.read(corpus(file));
            assertNotNull(doc, file);
            assertNotNull(doc.getBlocks(), file);
        }
    }

    @Test
    void emptyDocxYieldsNoBlocks() throws Exception {
        assertTrue(reader.read(corpus("empty.docx")).getBlocks().isEmpty());
    }

    @Test
    void nonEmptyCorpusYieldsText() throws Exception {
        for (String file : new String[]{"single-paragraph.docx", "tables.docx", "footnotes.docx",
                "strict-format.docx", "utf8-bom.docx"}) {
            assertFalse(allText(reader.read(corpus(file))).isBlank(), file + " should yield text");
        }
    }

    @Test
    void tablesCorpusHasTableBlock() throws Exception {
        boolean hasTable = reader.read(corpus("tables.docx")).getBlocks().stream()
                .anyMatch(b -> b instanceof DocBlock.Table t && t.rows().size() >= 2);
        assertTrue(hasTable, "tables.docx should contain a table with >= 2 rows");
    }

    @Test
    void simpleListCorpusHasListItems() throws Exception {
        boolean hasList = reader.read(corpus("simple-list.docx")).getBlocks().stream()
                .anyMatch(b -> b instanceof DocBlock.ListItem);
        assertTrue(hasList, "simple-list.docx should contain list items");
    }

    @Test
    void strikethroughCorpusHasStrikeSpan() throws Exception {
        boolean hasStrike = reader.read(corpus("strikethrough.docx")).getBlocks().stream()
                .filter(b -> b instanceof DocBlock.Paragraph)
                .flatMap(b -> ((DocBlock.Paragraph) b).spans().stream())
                .anyMatch(DocSpan::strike);
        assertTrue(hasStrike, "strikethrough.docx should yield a strike span");
    }

    @Test
    void footnotesCorpusYieldsNoteMarkerAndText() throws Exception {
        DocDocument doc = reader.read(corpus("footnotes.docx"));
        String text = allText(doc);
        assertTrue(text.contains("[^"), "should contain footnote reference marker");
    }

    @Test
    void commentsAreExtractedWithAuthor() throws Exception {
        DocDocument doc = reader.read(corpus("comments.docx"));
        List<String> quotes = doc.getBlocks().stream()
                .filter(b -> b instanceof DocBlock.Quote)
                .map(b -> spans(((DocBlock.Quote) b).spans()))
                .toList();
        assertEquals(2, quotes.size(), quotes.toString());
        assertTrue(quotes.get(0).contains("[批注 Michael Williamson]"), quotes.toString());
        assertTrue(quotes.get(0).contains("A tachyon walks into a bar."), quotes.toString());
        assertTrue(quotes.get(1).contains("Fin."), quotes.toString());
    }

    @Test
    void picturesCorpusYieldsImageBlocks() throws Exception {
        assertTrue(reader.read(corpus("tiny-picture.docx")).getBlocks().stream()
                .anyMatch(b -> b instanceof DocBlock.Image), "tiny-picture.docx should contain an image");
        // external-picture 引用的是外部 URL 图片，包内无字节，不应产出 Image 块。
        assertFalse(reader.read(corpus("external-picture.docx")).getBlocks().stream()
                .anyMatch(b -> b instanceof DocBlock.Image), "external-picture.docx has no embedded image data");
    }

    @Test
    void textBoxCorpusExtractsBoxContent() throws Exception {
        // text-box.docx 的正文只有文本框内容，文本框提取后应有文本产出。
        assertFalse(allText(reader.read(corpus("text-box.docx"))).isBlank(),
                "text-box.docx textbox content should be extracted");
    }

    // ---------- 合成特性单测 ----------

    private static String xml(String content) {
        return "<?xml version=\"1.0\"?>\n" + content;
    }

    private static byte[] zip(Map<String, String> entries) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(out)) {
            for (Map.Entry<String, String> e : entries.entrySet()) {
                zip.putNextEntry(new ZipEntry(e.getKey()));
                zip.write(e.getValue().getBytes(java.nio.charset.StandardCharsets.UTF_8));
                zip.closeEntry();
            }
        }
        return out.toByteArray();
    }

    @Test
    void vMergeProducesRowspan() throws Exception {
        byte[] docx = zip(Map.of(
                "[Content_Types].xml", xml("<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\"/>"),
                "word/document.xml", xml("""
                        <w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
                          <w:body>
                            <w:tbl>
                              <w:tr><w:tc><w:tcPr><w:vMerge w:val="restart"/></w:tcPr><w:p><w:r><w:t>合并</w:t></w:r></w:p></w:tc>
                                     <w:tc><w:p><w:r><w:t>B1</w:t></w:r></w:p></w:tc></w:tr>
                              <w:tr><w:tc><w:tcPr><w:vMerge/></w:tcPr><w:p><w:r><w:t>续</w:t></w:r></w:p></w:tc>
                                     <w:tc><w:p><w:r><w:t>B2</w:t></w:r></w:p></w:tc></w:tr>
                            </w:tbl>
                          </w:body>
                        </w:document>""")));
        DocDocument doc = reader.read(docx);
        DocBlock.Table table = (DocBlock.Table) doc.getBlocks().get(0);
        assertEquals(2, table.rows().size());
        assertEquals(2, table.rows().get(0).cells().size());
        assertEquals(2, table.rows().get(0).cells().get(0).rowspan());
        assertEquals(1, table.rows().get(1).cells().size());
        assertEquals("B2", spans(table.rows().get(1).cells().get(0).blocks().get(0)
                instanceof DocBlock.Paragraph p ? p.spans() : List.of()));
    }

    @Test
    void sdtContentIsUnwrapped() throws Exception {
        byte[] docx = zip(Map.of(
                "[Content_Types].xml", xml("<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\"/>"),
                "word/document.xml", xml("""
                        <w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
                          <w:body>
                            <w:sdt><w:sdtContent>
                              <w:p><w:r><w:t>内容控件里的文字</w:t></w:r></w:p>
                            </w:sdtContent></w:sdt>
                          </w:body>
                        </w:document>""")));
        assertTrue(allText(reader.read(docx)).contains("内容控件里的文字"));
    }

    @Test
    void outlineLvlDecidesHeading() throws Exception {
        byte[] docx = zip(Map.of(
                "[Content_Types].xml", xml("<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\"/>"),
                "word/document.xml", xml("""
                        <w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
                          <w:body>
                            <w:p><w:pPr><w:outlineLvl w:val="2"/></w:pPr><w:r><w:t>三级标题</w:t></w:r></w:p>
                          </w:body>
                        </w:document>""")));
        DocBlock first = reader.read(docx).getBlocks().get(0);
        assertInstanceOf(DocBlock.Heading.class, first);
        assertEquals(3, ((DocBlock.Heading) first).level());
    }

    @Test
    void stylesXmlNameDecidesHeadingForChineseWord() throws Exception {
        byte[] docx = zip(Map.of(
                "[Content_Types].xml", xml("<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\"/>"),
                "word/document.xml", xml("""
                        <w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
                          <w:body>
                            <w:p><w:pPr><w:pStyle w:val="1"/></w:pPr><w:r><w:t>中文标题</w:t></w:r></w:p>
                          </w:body>
                        </w:document>"""),
                "word/styles.xml", xml("""
                        <w:styles xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
                          <w:style w:type="paragraph" w:styleId="1">
                            <w:name w:val="heading 1"/>
                          </w:style>
                        </w:styles>""")));
        DocBlock first = reader.read(docx).getBlocks().get(0);
        assertInstanceOf(DocBlock.Heading.class, first);
        assertEquals(1, ((DocBlock.Heading) first).level());
    }

    @Test
    void multilevelNumberingRendersLvlText() throws Exception {
        byte[] docx = zip(Map.of(
                "[Content_Types].xml", xml("<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\"/>"),
                "word/document.xml", xml("""
                        <w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
                          <w:body>
                            <w:p><w:pPr><w:numPr><w:ilvl w:val="0"/><w:numId w:val="1"/></w:numPr></w:pPr><w:r><w:t>甲</w:t></w:r></w:p>
                            <w:p><w:pPr><w:numPr><w:ilvl w:val="1"/><w:numId w:val="1"/></w:numPr></w:pPr><w:r><w:t>乙</w:t></w:r></w:p>
                            <w:p><w:pPr><w:numPr><w:ilvl w:val="1"/><w:numId w:val="1"/></w:numPr></w:pPr><w:r><w:t>丙</w:t></w:r></w:p>
                            <w:p><w:pPr><w:numPr><w:ilvl w:val="0"/><w:numId w:val="1"/></w:numPr></w:pPr><w:r><w:t>丁</w:t></w:r></w:p>
                          </w:body>
                        </w:document>"""),
                "word/numbering.xml", xml("""
                        <w:numbering xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
                          <w:abstractNum w:abstractNumId="0">
                            <w:lvl w:ilvl="0"><w:numFmt w:val="decimal"/><w:lvlText w:val="%1."/><w:start w:val="1"/></w:lvl>
                            <w:lvl w:ilvl="1"><w:numFmt w:val="decimal"/><w:lvlText w:val="%1.%2"/><w:start w:val="1"/></w:lvl>
                          </w:abstractNum>
                          <w:num w:numId="1"><w:abstractNumId w:val="0"/></w:num>
                        </w:numbering>""")));
        String md = reader.read(docx).toMarkdown();
        assertTrue(md.contains("1. 甲"), md);
        assertTrue(md.contains("1.1 乙"), md);
        assertTrue(md.contains("1.2 丙"), md);
        assertTrue(md.contains("2. 丁"), md);
    }

    @Test
    void textboxContentExtractedFromPict() throws Exception {
        byte[] docx = zip(Map.of(
                "[Content_Types].xml", xml("<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\"/>"),
                "word/document.xml", xml("""
                        <w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"
                                    xmlns:v="urn:schemas-microsoft-com:vml">
                          <w:body>
                            <w:p><w:r><w:pict>
                              <v:shape><v:textbox>
                                <w:txbxContent><w:p><w:r><w:t>文本框正文</w:t></w:r></w:p></w:txbxContent>
                              </v:textbox></v:shape>
                            </w:pict></w:r></w:p>
                          </w:body>
                        </w:document>""")));
        assertTrue(allText(reader.read(docx)).contains("文本框正文"));
    }

    @Test
    void headerFooterExtractedAsQuotes() throws Exception {
        Map<String, String> entries = new HashMap<>();
        entries.put("[Content_Types].xml",
                xml("<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\"/>"));
        entries.put("word/document.xml", xml("""
                <w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"
                            xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
                  <w:body>
                    <w:p><w:r><w:t>正文</w:t></w:r></w:p>
                    <w:sectPr>
                      <w:headerReference w:type="default" r:id="rId9"/>
                    </w:sectPr>
                  </w:body>
                </w:document>"""));
        entries.put("word/_rels/document.xml.rels", xml("""
                <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
                  <Relationship Id="rId9" Target="header1.xml"/>
                </Relationships>"""));
        entries.put("word/header1.xml", xml("""
                <w:hdr xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
                  <w:p><w:r><w:t>页眉文字</w:t></w:r></w:p>
                </w:hdr>"""));
        String text = allText(reader.read(zip(entries)));
        assertTrue(text.contains("[页眉] 页眉文字"), text);
    }

    @Test
    void hyperlinkAnchorAndExternal() throws Exception {
        Map<String, String> entries = new HashMap<>();
        entries.put("[Content_Types].xml",
                xml("<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\"/>"));
        entries.put("word/document.xml", xml("""
                <w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"
                            xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
                  <w:body>
                    <w:p>
                      <w:hyperlink r:id="rId1"><w:r><w:t>外部</w:t></w:r></w:hyperlink>
                      <w:hyperlink w:anchor="sec1"><w:r><w:t>内部</w:t></w:r></w:hyperlink>
                    </w:p>
                  </w:body>
                </w:document>"""));
        entries.put("word/_rels/document.xml.rels", xml("""
                <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
                  <Relationship Id="rId1" Target="https://example.com/" TargetMode="External"/>
                </Relationships>"""));
        DocDocument doc = reader.read(zip(entries));
        List<DocSpan> spans = ((DocBlock.Paragraph) doc.getBlocks().get(0)).spans();
        assertEquals("https://example.com/", spans.get(0).link());
        assertEquals("#sec1", spans.get(1).link());
    }
}
