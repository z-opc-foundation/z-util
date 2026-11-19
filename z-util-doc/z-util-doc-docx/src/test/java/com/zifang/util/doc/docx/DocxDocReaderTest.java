package com.zifang.util.doc.docx;

import com.zifang.util.doc.DocBlock;
import com.zifang.util.doc.DocDocument;
import com.zifang.util.doc.DocFormat;
import com.zifang.util.doc.DocSpan;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DocxDocReaderTest {

    private final DocxDocReader reader = new DocxDocReader();

    @Test
    void formats() {
        assertEquals(DocFormat.DOCX, reader.format());
        assertFalse(reader.supports(new byte[]{1, 2, 3}));
    }

    @Test
    void paragraphsHeadingsAndStyles() throws Exception {
        byte[] docx = zip(Map.of(
                "[Content_Types].xml", xml("""
                        <?xml version="1.0"?><Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types"/>"""),
                "word/document.xml", xml("""
                        <?xml version="1.0"?>
                        <w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"
                                    xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
                          <w:body>
                            <w:p><w:pPr><w:pStyle w:val="Heading2"/></w:pPr><w:r><w:t>章节标题</w:t></w:r></w:p>
                            <w:p><w:r><w:t>普通</w:t></w:r><w:r><w:rPr><w:b/></w:rPr><w:t>粗</w:t></w:r>
                              <w:r><w:rPr><w:i w:val="true"/></w:rPr><w:t xml:space="preserve"> 斜 </w:t></w:r>
                              <w:r><w:rPr><w:strike w:val="0"/></w:rPr><w:t>不删</w:t></w:r></w:p>
                            <w:p><w:r><w:t>a</w:t></w:r><w:br/><w:r><w:t>b</w:t></w:r><w:r><w:tab/><w:t>c</w:t></w:r></w:p>
                          </w:body>
                        </w:document>""")));
        DocDocument doc = reader.read(docx);
        List<DocBlock> blocks = doc.getBlocks();
        DocBlock.Heading heading = (DocBlock.Heading) blocks.get(0);
        assertEquals(2, heading.level());
        assertEquals("章节标题", heading.spans().get(0).text());
        DocBlock.Paragraph para = (DocBlock.Paragraph) blocks.get(1);
        assertEquals(new DocSpan("粗", true, false, false, false, null), para.spans().get(1));
        assertFalse(para.spans().get(2).strike());
        assertTrue(doc.toMarkdown().contains("## 章节标题"));
        assertTrue(doc.toMarkdown().contains("**粗**"));
    }

    @Test
    void hyperlinkAndImage() throws Exception {
        byte[] png = {(byte) 0x89, 'P', 'N', 'G'};
        java.util.Map<String, Object> entries = new java.util.LinkedHashMap<>();
        entries.put("[Content_Types].xml", xml("<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\"/>"));
        entries.put("word/document.xml", xml("""
                        <?xml version="1.0"?>
                        <w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"
                                    xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"
                                    xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main">
                          <w:body>
                            <w:p><w:hyperlink r:id="rId1"><w:r><w:t>链接文字</w:t></w:r></w:hyperlink></w:p>
                            <w:p><w:r><w:drawing><a:blip r:embed="rId2"/></w:drawing></w:r></w:p>
                          </w:body>
                        </w:document>"""));
        entries.put("word/_rels/document.xml.rels", xml("""
                <?xml version="1.0"?>
                <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
                  <Relationship Id="rId1" Target="https://example.com/x"/>
                  <Relationship Id="rId2" Target="media/pic.png"/>
                </Relationships>"""));
        entries.put("word/media/pic.png", png);
        byte[] docx = zip(entries);
        DocDocument doc = reader.read(docx);
        DocBlock.Paragraph para = (DocBlock.Paragraph) doc.getBlocks().get(0);
        assertEquals("https://example.com/x", para.spans().get(0).link());
        DocBlock.Image image = (DocBlock.Image) doc.getBlocks().get(1);
        assertEquals("pic.png", image.name());
        assertEquals("image/png", image.mimeType());
        assertArrayEquals(png, image.data());
    }

    @Test
    void listItemsFromNumbering() throws Exception {
        byte[] docx = zip(Map.of(
                "[Content_Types].xml", xml("<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\"/>"),
                "word/document.xml", xml("""
                        <?xml version="1.0"?>
                        <w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
                          <w:body>
                            <w:p><w:pPr><w:numPr><w:ilvl w:val="0"/><w:numId w:val="1"/></w:numPr></w:pPr>
                              <w:r><w:t>有序项</w:t></w:r></w:p>
                            <w:p><w:pPr><w:numPr><w:ilvl w:val="1"/><w:numId w:val="2"/></w:numPr></w:pPr>
                              <w:r><w:t>无序子项</w:t></w:r></w:p>
                          </w:body>
                        </w:document>"""),
                "word/numbering.xml", xml("""
                        <?xml version="1.0"?>
                        <w:numbering xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
                          <w:abstractNum w:abstractNumId="0">
                            <w:lvl w:ilvl="0"><w:numFmt w:val="decimal"/></w:lvl>
                            <w:lvl w:ilvl="1"><w:numFmt w:val="bullet"/></w:lvl>
                          </w:abstractNum>
                          <w:num w:numId="1"><w:abstractNumId w:val="0"/></w:num>
                          <w:num w:numId="2"><w:abstractNumId w:val="0"/></w:num>
                        </w:numbering>""")));
        DocDocument doc = reader.read(docx);
        DocBlock.ListItem first = (DocBlock.ListItem) doc.getBlocks().get(0);
        DocBlock.ListItem second = (DocBlock.ListItem) doc.getBlocks().get(1);
        assertTrue(first.ordered());
        assertFalse(second.ordered());
        assertEquals(1, second.level());
        assertTrue(doc.toMarkdown().contains("1. 有序项"));
        assertTrue(doc.toMarkdown().contains("- 无序子项"));
    }

    @Test
    void tableColspanAndHeader() throws Exception {
        byte[] docx = zip(Map.of(
                "[Content_Types].xml", xml("<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\"/>"),
                "word/document.xml", xml("""
                        <?xml version="1.0"?>
                        <w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
                          <w:body>
                            <w:tbl>
                              <w:tr><w:tc><w:p><w:r><w:t>列A</w:t></w:r></w:p></w:tc><w:tc><w:p><w:r><w:t>列B</w:t></w:r></w:p></w:tc></w:tr>
                              <w:tr><w:tc><w:p><w:r><w:t>1</w:t></w:r></w:p></w:tc>
                                <w:tc><w:tcPr><w:gridSpan w:val="2"/></w:tcPr><w:p><w:r><w:t>跨列</w:t></w:r></w:p></w:tc></w:tr>
                            </w:tbl>
                          </w:body>
                        </w:document>""")));
        DocDocument doc = reader.read(docx);
        DocBlock.Table table = (DocBlock.Table) doc.getBlocks().get(0);
        assertEquals(2, table.rows().size());
        assertTrue(table.rows().get(0).header());
        assertEquals(2, table.rows().get(1).cells().get(1).colspan());
        assertTrue(doc.toMarkdown().contains("| 列A | 列B |"));
    }

    @Test
    void coreMetadata() throws Exception {
        byte[] docx = zip(Map.of(
                "[Content_Types].xml", xml("<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\"/>"),
                "word/document.xml", xml("""
                        <?xml version="1.0"?>
                        <w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
                          <w:body><w:p><w:r><w:t>正文</w:t></w:r></w:p></w:body>
                        </w:document>"""),
                "docProps/core.xml", xml("""
                        <?xml version="1.0"?>
                        <cp:coreProperties xmlns:cp="http://schemas.openxmlformats.org/package/2006/metadata/core-properties"
                                           xmlns:dc="http://purl.org/dc/elements/1.1/"
                                           xmlns:dcterms="http://purl.org/dc/terms/">
                          <dc:title>文档标题</dc:title>
                          <dc:creator>张三</dc:creator>
                          <dc:subject>主题词</dc:subject>
                          <cp:lastModifiedBy>李四</cp:lastModifiedBy>
                          <dcterms:created>2024-01-02T03:04:05Z</dcterms:created>
                        </cp:coreProperties>""")));
        DocDocument doc = reader.read(docx);
        assertEquals("文档标题", doc.getMeta().getTitle());
        assertEquals("张三", doc.getMeta().getAuthor());
        assertEquals("李四", doc.getMeta().getCreator());
        assertEquals("主题词", doc.getMeta().getSubject());
        assertEquals(2024, doc.getMeta().getCreationDate().getYear());
    }

    private String xml(String s) {
        return s;
    }

    private byte[] zip(java.util.Map<String, ?> entries) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(out)) {
            for (var e : entries.entrySet()) {
                zip.putNextEntry(new ZipEntry(e.getKey()));
                if (e.getValue() instanceof byte[] b) {
                    zip.write(b);
                } else {
                    zip.write(((String) e.getValue()).getBytes(StandardCharsets.UTF_8));
                }
                zip.closeEntry();
            }
        }
        return out.toByteArray();
    }
}
